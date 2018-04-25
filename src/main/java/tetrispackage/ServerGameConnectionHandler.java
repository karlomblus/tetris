package tetrispackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Random;

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
                    case 101:
                        receiveGamerMove(dis.readInt(), dis.readChar());
                        break;
                    case 102:
                        game.removeUserFromGame(this);
                        break;
                    case 103:
                        game.sendNewTetromino(userid);
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
                // todo: salvestame sessioonitabelisse (seda võiks kasutada web)
                dos.writeInt(1);
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
            dos.writeInt(2);
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
            if (andmebaasist[0].length() == 0) {
                dos.writeInt(-1);
                dos.writeUTF("Sellist kasutajanime ei ole"); // väidetavalt pole turvaline eraldi infot anda, aga regamisprotsessis saab kasutajanime eksisteerimist niikuinii kontrollida
                ServerMain.debug(5, "dologin: Kasutajanime " + username + " ei ole.");
                return;
            }
            boolean passwordMatch = ServerPasswordCrypto.verifyUserPassword(password, andmebaasist[1]);
            if (passwordMatch) {
                dos.writeInt(1);
                dos.writeUTF("OK");
                ServerMain.debug(5, "dologin: Kasutajanimi " + username + " OK, loggedin.");
                userid = Integer.parseInt(andmebaasist[0]);
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

            // todo: kui OK, siis salvestame sessioonitabelisse


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

        // fakeme lisadatat, et testides asi tühi poleks
        dos.writeInt(3);
        dos.writeInt(998);
        dos.writeUTF("Fake");

    } // getUserList


    private void doLogout(DataOutputStream dos) throws Exception {
        // while lõpetatakse ära, socketi sulgemisel võetakse ta ka sessioonilistist maha
        connected = false;
if (game!=null) {
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
        //todo: salvestame sql-i

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
        // todo: reaalne
        dos.writeInt(6);
        dos.writeInt(2);
        dos.writeUTF("Malle");
        dos.writeUTF("Kalle");
        dos.writeInt(6);
        dos.writeInt(7);
        dos.writeUTF("Jüri");
        dos.writeUTF("Mari");

        // tuleks teha list kus on sees mängivad pooled ja mängu ID

    } // getRunningGames


    public int getInvitedUID() {
        return invitedUID;
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
                invitedUID=0;            // mõlemal
                ServerGameData game = new ServerGameData(this, player);
                this.game=game;
                player.game=game;
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
            if (player.getUserid() == rejectTo && player.getInvitedUID() == rejectTo) {

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

    public void setInvitedUID(int invitedUID) {
        this.invitedUID = invitedUID;
    }

    public String toString() {
        return "\n" + userid + ": Nimi " + username + " " + (login?"LOGIN":"") + " invite: " + invitedUID + " opponent: " + opponentID;
    }


} //ServerGameConnectionHandler class

