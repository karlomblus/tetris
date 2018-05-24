package server;

import chati_leiutis.MessageID;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerGameConnectionHandler implements Runnable {

    // siin tegeleme sissetulnud mängu ühendustega
    // esialgu faken mingi kasutajaskonna ja sisu

    DataOutputStream dos; // selle useri data output
    private Socket socket;
    private boolean connected = true; // kui see maha keeratakse, siis on sessioon läbi
    private boolean login = true;  // kasutaja on logimisfaasis
    private List<ServerGameConnectionHandler> players; // siin on kõik mängijad
    private int userid = 0;       // mängija userID
    private String username = "";
    private int invitedUID = 0; // id, keda ma olen mängima kutsunud.
    private int opponentID = 0; // kellega ta mängib
    private ServerSQL sql;
    private ServerGameData game;

    ServerGameConnectionHandler(Socket socket, List<ServerGameConnectionHandler> players, ServerSQL sql) {
        this.socket = socket;
        this.players = players;
        this.sql = sql;
    }


    public int getUserid() {
        return userid;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        // kood, mida soovime paralleelselt käivitada

        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            this.dos = dos;
            while (connected) {
                int command = dis.readInt();

                if (login) {
                    //ServerMain.debug(6, "kasutajalt (LOGIN) " + username + " tuli command: " + command);
                    switch (command) {
                        case 1: // uue konto registreerimine
                            createAccount(dos, dis.readUTF(), dis.readUTF());
                            break;
                        case 2: // sisselogimine
                            doLogin(dos, dis.readUTF(), dis.readUTF());
                            break;
                        default:
                            ServerMain.error("LOGIN: tundmatu/lubamatu command: " + command, null);
                    }
                    continue;
                } // login

                //ServerMain.debug(6, "kasutajalt " + username + " tuli command: " + command);
                switch (command) {
                    case 3: // get userlist
                        getUserList(dos);
                        break;
                    case 4: // do logout
                        doLogout(dos);
                        break;
                    case 5: // add lobby chat
                        saveChatmessage(dos, dis.readUTF());
                        break;
                    case 6: // get running games
                        getRunningGames(dos);
                        break;
                    case 7:
                        inviteToGame(dis.readInt());
                        break;
                    case 9:
                        rejectInviteToGame(dis.readInt());
                        break;
                    case 10:
                        sandGameslist(dis.readInt(), dis.readInt());
                        break;
                    case 11:
                        sendGameLog(dis.readInt());
                        break;
                    case 101:
                        receiveGamerMove(dis.readInt(), dis.readChar());
                        break;
                    case 102:
                        game.removeUserFromGame(this);
                        break;
                    case 103:
                        game.sendNewTetromino(userid);
                        break;
                    case 104:
                        playerAcknowledgeHisDefeat();
                        break;
                    case 105:
                        privateChatmessage(dis.readInt(), dis.readUTF());
                        break;
                    default:
                        ServerMain.error("Tuli sisse tundmatu/lubamatu command: " + command, null);
                } // command switch
            } // while connected

        } catch (SocketException | EOFException e) {
            // Kas siis võin ilma põhjuseta kinni püüda kui ma ei taha, et see edasi throwtakse ning tahan lihtsalt viisakalt olukorda lõpetada?
            ServerMain.debug("Teine pool sulges ootamatult socketi, teeme siis sama");
        } catch (Exception e) {
            ServerMain.debug(1, "Tuli sisse ootamatu exception, sureme maha");
            throw new RuntimeException(e);
        } finally {
            try {
                players.remove(this);
                ServerMain.debug("Sulgeme socketi");
                ServerMain.debug(6, "Allesjäänud mängijad: " + players);
                socket.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


    } // run()

    private void playerAcknowledgeHisDefeat() throws Exception {
        ServerMain.debug("Kasutaja saatis info enda kaotusest: " + this.getUsername());
        if (!game.isRunning()) {
            // mäng ei käi enam (punktidele)
            return;
        }
        sql.insert("insert into mangulogi (id, gameid,timestamp_sql ,timestampms,userid,tickid,tegevus) values (0,?,now(),?,0,?,? )", String.valueOf(this.game.getGameid()), String.valueOf(System.currentTimeMillis()), String.valueOf(this.game.getTickid()), "104");
        game.setRunning(false); // mäng ise jääb käima, aga punkte enam ei jagata

        for (ServerGameConnectionHandler player : players) {
            if (player.getUserid() != this.userid) {
                DataOutputStream dos2 = player.getDos();
                synchronized (dos2) {
                    dos2.writeInt(104);
                    dos2.writeInt(this.userid);
                    sql.query("update users  set points=points+1 where id = ? limit 1", String.valueOf(player.getUserid()));
                    ServerMain.debug(6, "Kasutaja " + player.getUsername() + " sai punkti");
                } // sync2
            }
        }
    }

    private void sandGameslist(int alates, int mitu) throws Exception {
        synchronized (dos) {
            dos.writeInt(10);
            Connection conn = sql.getConn();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            StringBuilder logi = new StringBuilder(10000000);
            try {


                String query = "select mangud.id as manguid,mangud.player1 as player1id,mangud.player2 as player2id,started,u1.username as player1name, u2.username as player2name  from mangud left join users as u1 on mangud.player1 = u1.id left join users as u2 on u2.id=mangud.player2 order by mangud.id desc limit ?,?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, alates);
                stmt.setInt(2, mitu);
                rs = stmt.executeQuery();

                // iterate through the java resultset
                while (rs.next()) {
                    logi.append(rs.getString("manguid") + "," + rs.getString("started") + "," + rs.getString("player1id") + "," + rs.getString("player2id") + "," + rs.getString("player1name") + "," + rs.getString("player2name") + "\n");
                }

            } finally {

                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }

            } // finally

            dos.writeUTF(logi.toString());


        } // sync
    }

    private void sendGameLog(int gameid) throws Exception {
        ServerMain.debug("Meilt küsitakse mängu ID: " + gameid);

        synchronized (dos) {
            String[] andmebaasist = sql.query(5, "select mangud.id as manguid,player1,u1.username as u1name,player2,u2.username as u2name from mangud left join users as u1 on u1.id=mangud.player1 left join users as u2 on u2.id=mangud.player2 where mangud.id = ?", String.valueOf(gameid));
            // kui baasist mängu ei saanud, siis ei tagasta mitte midagi
            if (andmebaasist[0] == null || andmebaasist[0].length() == 0) {
                ServerMain.debug("ei leidnud mängu");
                return;
            }
            dos.writeInt(MessageID.GETREPLAYDATA);
            dos.writeInt(Integer.parseInt(andmebaasist[0])); // mangu id
            dos.writeInt(Integer.parseInt(andmebaasist[1])); // player 1 ID
            dos.writeUTF(andmebaasist[2]); // player 1 name
            dos.writeInt(Integer.parseInt(andmebaasist[3])); // player 2 ID
            dos.writeUTF(andmebaasist[4]); // player 2 name

            Connection conn = sql.getConn();
            ResultSet rs = null;
            PreparedStatement stmt = null;
            StringBuilder logi = new StringBuilder(10000000);
            try {


                String query = "SELECT timestampms,userid,tickid,tegevus FROM mangulogi where gameid = ? order by id asc";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, gameid);
                rs = stmt.executeQuery();

                // iterate through the java resultset
                while (rs.next()) {
                    logi.append(rs.getString("timestampms") + "," + rs.getString("userid") + "," + rs.getString("tickid") + "," + rs.getString("tegevus") + "\n");
                }

            } finally {

                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }

            } // finally

            dos.writeInt(logi.length());
            dos.writeBytes(logi.toString());


        } // sync
    }


    private void createAccount(DataOutputStream dos, String username, String password) throws Exception {

        synchronized (dos) {
            dos.writeInt(1);
            if (username == null || username.length() < 2) {
                dos.writeInt(-1);
                dos.writeUTF("Kasutajanimi liiga lühike: " + username.length());
                ServerMain.debug(5, "createaccount: Kasutajanimi " + username + " liiga lühike.");
                return;
            }
            if (password == null || password.length() < 3) {
                dos.writeInt(-1);
                dos.writeUTF("Parool liiga lühike");
                ServerMain.debug(5, "createaccount: Kasutaja " + username + " parool puudu.");
                return;
            }

            String uid = sql.getstring("select id from users where username = ?", username);

            if (uid.length() > 0) {
                dos.writeInt(-1);
                dos.writeUTF("Valitud kasutajanimi on juba olemas");
                ServerMain.debug(5, "createaccount: Kasutajanimi " + username + " on juba olemas.");
                return;
            }


            String hashedPassword = ServerPasswordCrypto.generateSecurePassword(password);
            int result = sql.insert("insert into users (id,username,password) values (0,?,?)", username, hashedPassword);

            if (result > 0) {

                dos.writeInt(MessageID.REGISTRATION);
                dos.writeUTF("OK, kasutaja loodud, Ingo tahab, et logiksid eraldi sisse");
                ServerMain.debug(5, "createaccount: Kasutajanimi " + username + " loodud.");
                //userid = result;
                //this.username = username;
                //login = false;
                //sendLoginmessageToAll();
            } else {
                dos.writeInt(-1);
                dos.writeUTF("Kasutaja lisamine andmebaasi ebaõnnestus");
                ServerMain.debug(5, "createaccount: Kasutajanimi " + username + " lisamine baasi ebaõnnestus.");
            }
        } // sync
    } // createAccount


    private void doLogin(DataOutputStream dos, String username, String password) throws Exception {
        synchronized (dos) {
            dos.writeInt(MessageID.LOGIN);
            if (username == null || username.length() < 2) {
                dos.writeInt(-1);
                dos.writeUTF("Kasutajanimi liiga lühike");
                ServerMain.debug(5, "dologin: Kasutajanimi " + username + " liiga lühike.");
                return;
            }
            if (password == null || password.length() < 1) {
                dos.writeInt(-1);
                dos.writeUTF("Parool liiga lühike");
                ServerMain.debug(5, "dologin: Kasutajan " + username + " parool puudu.");
                return;
            }
            String[] andmebaasist = sql.query(2, "select id,password from users where username = ?", username);
            if ( andmebaasist[0]==null ||  andmebaasist[0].length() == 0) {
                dos.writeInt(-1);
                dos.writeUTF("Sellist kasutajanime ei ole"); // väidetavalt pole turvaline eraldi infot anda, aga regamisprotsessis saab kasutajanime eksisteerimist niikuinii kontrollida
                ServerMain.debug(5, "dologin: Kasutajanime " + username + " ei ole.");
                return;
            }
            boolean passwordMatch = ServerPasswordCrypto.verifyUserPassword(password, andmebaasist[1]);
            if (passwordMatch) {
                dos.writeInt(1);
                dos.writeUTF("OK");
                userid = Integer.parseInt(andmebaasist[0]);
                ServerMain.debug(5, "dologin: Kasutajanimi " + username + ", id: " + userid + " OK, loggedin.");
                this.username = username;
                login = false;

                // kui sama kasutaja on sees, viskame välja
                for (ServerGameConnectionHandler player : players) {
                    if (player != this && player.userid == this.userid) {
                        DataOutputStream dos2 = player.getDos();
                        synchronized (dos2) {
                            ServerMain.debug("Kasutaja " + username + " on juba sees, kickime.");
                            dos2.writeInt(4);
                            dos2.writeInt(player.userid);
                            dos2.writeUTF(player.username);
                            player.connected = false;
                            player.socket.close();
                        } // sync
                    }
                } // iter

                sendLoginmessageToAll();
                ServerMain.debug(6, "Kõik mängijad: " + players);

            } else {
                dos.writeInt(-1);
                dos.writeUTF("Vale parool");
                ServerMain.debug(5, "dologin: Kasutajanimi " + username + " parool on vale.");
            }


        } // sync
    } // doLogin

    // saadab kõigile teate, et see kasujaja logis sisse
    private void sendLoginmessageToAll() throws IOException {
        for (ServerGameConnectionHandler player : players) {
            DataOutputStream dos2 = player.getDos();
            synchronized (dos2) {
                if (!player.isLogin()) { // kõigile sisseloginutele
                    dos2.writeInt(3);
                    dos2.writeInt(userid);
                    dos2.writeUTF(username);
                }
            } // sync
        } // iter
    }


    private void getUserList(DataOutputStream dos) throws Exception {

        for (ServerGameConnectionHandler player : players) {
            dos.writeInt(3);
            dos.writeInt(player.getUserid());
            dos.writeUTF(player.getUsername());

        }


    } // getUserList


    private void doLogout(DataOutputStream dos) throws Exception {
        // while lõpetatakse ära, socketi sulgemisel võetakse ta ka sessioonilistist maha
        connected = false;
        if (game != null) {
            game.removeUserFromGame(this);
        }
        ServerMain.debug(5, "dologout: " + username);
        for (ServerGameConnectionHandler player : players) {
            DataOutputStream dos2 = player.getDos();
            synchronized (dos2) {
                if (!player.isLogin() && player != this) { // kõigile sisseloginutele peale enda
                    dos2.writeInt(4);
                    dos2.writeInt(userid);
                    dos2.writeUTF(username);
                }
            } // sync
        } // iter

    } // doLogout

    public DataOutputStream getDos() {
        return dos;
    }

    public boolean isLogin() {
        return login;
    }

    private void saveChatmessage(DataOutputStream dos, String message) throws Exception {

        sql.insert("INSERT INTO  lobbychat ( id,uid,message,aeg ) VALUES ( '0', ?,  ?,now() )", String.valueOf(userid), message);

        ServerMain.debug(8, "lobbychatmessage " + username + ": " + message);
        for (ServerGameConnectionHandler player : players) {
            DataOutputStream dos2 = player.getDos();
            synchronized (dos2) {
                if (!player.isLogin()) { // kui kasutaja on juba sisse loginud
                    dos2.writeInt(5);

                    dos2.writeInt(userid);
                    dos2.writeUTF(username);
                    dos2.writeUTF(message);
                    //ServerMain.debug(9, "lobbychatmessage " + username + " -> " + player.getUsername() + " : " + message);
                }
            } // sync
        } // iter

    } // saveChatmessage

    private void getRunningGames(DataOutputStream dos) throws Exception {
        Set<ServerGameData> mangud = new HashSet<>();
        // lisame kõik mängud seti sisse
        for (ServerGameConnectionHandler player : players) {
            if (player.opponentID > 0 && player.login == false && player.game != null && player.game.isRunning()) {
                mangud.add(player.game);
            }
        }
        synchronized (dos) {
            for (ServerGameData mang : mangud) {
                dos.writeInt(6);
                dos.writeInt(mang.getGameid());
                if (mang.getPlayers().get(0) != null) dos.writeUTF(mang.getPlayers().get(0).username);
                if (mang.getPlayers().size() > 1) {
                    dos.writeUTF(mang.getPlayers().get(1).username);
                } else dos.writeUTF(""); // pole teist mängijat selles mängus enam
            }
        } // sync


    } // getRunningGames


    public int getInvitedUID() {
        return invitedUID;
    }

    public void setInvitedUID(int invitedUID) {
        this.invitedUID = invitedUID;
    }

    public void setOpponentID(int opponentID) {
        this.opponentID = opponentID;
    }

    private void inviteToGame(int invitedUID) throws Exception {
        ServerMain.debug(5, "invitetogame: " + username + " kutsub " + invitedUID + " mängima");

        if (invitedUID == 0) {
            this.invitedUID = 0;
            return;
        }
        if (this.userid == invitedUID) {
            ServerMain.debug(6, "Kutsus iseennast. ignome");
            this.invitedUID = 0;
            synchronized (dos) {
                dos.writeInt(9);
                dos.writeInt(userid);
                dos.writeUTF(username);
            }

            return;
        }

        for (ServerGameConnectionHandler player : players) {
            // leidsime mängija, ta tahab minuga ka mängida. anname mõlemale teada
            if (player.getUserid() == invitedUID && player.getInvitedUID() == userid) {
                // see mängija kutsus mind ka mängima, seega aksepteerime mängu ja anname mõlemale teada
                ServerMain.debug(6, "inviteToGame: " + username + " aksepteerib " + player.getUsername());
                opponentID = player.getUserid();
                player.setOpponentID(userid);
                player.setInvitedUID(0); // võtame kutse maha peale accepti
                invitedUID = 0;            // mõlemal
                ServerGameData game = new ServerGameData(this, player, sql);
                this.game = game;
                player.game = game;

                //System.out.println("hakkame start käsku saatma mängijale " + player.getUsername());
                DataOutputStream dos2 = player.getDos();
                synchronized (dos2) {
                    dos2.writeInt(8);
                    dos2.writeInt(userid);
                    dos2.writeInt(game.getGameid());
                } // sync
                synchronized (dos) {
                    dos.writeInt(8);
                    dos.writeInt(player.getUserid());
                    dos.writeInt(game.getGameid());
                } // sync


                game.start();
            }
            // leidsime mängija, anname talle teada
            else if (player.getUserid() == invitedUID) {
                ServerMain.debug(6, "inviteToGame: " + username + " saadab kutse " + player.getUsername());
                this.invitedUID = invitedUID;
                DataOutputStream dos2 = player.getDos();
                synchronized (dos2) {
                    dos2.writeInt(7);
                    dos2.writeInt(userid);
                    dos2.writeUTF(username);
                } // sync teade teisele
            } //
        } // for kõik mängijad
    } // inviteToGame

    private void rejectInviteToGame(int rejectTo) {
        ServerMain.debug(5, "rejectInviteToGame: " + username + " saadab ID " + rejectTo + " pikalt");
        for (ServerGameConnectionHandler player : players) {
            // leidsime mängija, ja tal on veel kehtiv kutse mulle
            if (player.getUserid() == rejectTo && player.getInvitedUID() == userid) {

                ServerMain.debug(6, "rejectInviteToGame: " + username + " rejectib " + player.getUsername());

                DataOutputStream dos2 = player.getDos();
                synchronized (dos2) {
                    try {
                        dos2.writeInt(9);
                        dos2.writeUTF(username);
                        player.setInvitedUID(0); // lülitame teisel kutse minule välja, et peale pikaltsaatmist saaksime teda kutsuda mitte tema kutset acceptida
                    } catch (Exception ignored) {
                    } // kui see teade kohele ei lähe, siis on see täiesti tähtsusetu
                } // sync teade teisele
            }

        } // for kõik mängijad
    } // rejectInviteToGame

    private void privateChatmessage(int toID, String message) {
        ServerMain.debug(5, "privateChatmessage: " + username + " priv to " + toID);
        for (ServerGameConnectionHandler player : players) {
            // leidsime mängija, ja tal on veel kehtiv kutse mulle
            if (player.getUserid() == toID) {
                DataOutputStream dos2 = player.getDos();
                synchronized (dos2) {
                    try {
                        dos2.writeInt(105);
                        dos2.writeInt(userid);
                        dos2.writeUTF(username);
                        dos2.writeUTF(message);
                    } catch (Exception ignored) {
                    } // kui see teade kohele ei lähe, siis on see täiesti tähtsusetu
                } // sync teade teisele
            }
        }
    } // privateChatmessage

    private void receiveGamerMove(int tickID, char action) throws Exception {
        for (ServerGameConnectionHandler player : players) {
            if (player.getUserid() == opponentID && !player.isLogin()) {
                ServerMain.debug(6, "move " + userid + "->" + player.userid + ": " + tickID + ":" + action);
                DataOutputStream dos2 = player.getDos();
                synchronized (dos2) {
                    dos2.writeInt(101);
                    dos2.writeInt(tickID);
                    dos2.writeChar(action);
                    dos2.writeInt(userid);
                } // sync teade teisele
            }
        }
    } // receiveGamerMove

    public String toString() {
        return "\n" + userid + ": Nimi " + username + " " + (login ? "LOGIN" : "") + " invite: " + invitedUID + " opponent: " + opponentID;
    }


} //ServerGameConnectionHandler class

