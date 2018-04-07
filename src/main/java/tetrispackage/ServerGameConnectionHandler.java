package tetrispackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

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

    ServerGameConnectionHandler(Socket socket, List<ServerGameConnectionHandler> players) {
        this.socket = socket;
        this.players = players;
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
                    ServerMain.debug(6, "kasutajalt (LOGIN) " + username + " tuli command: " + command);
                    switch (command) {
                        case 1: // uue konto registreerimine
                            createAccount(dos, dis.readUTF(), dis.readUTF());
                            break;
                        case 2: // sisselogimine
                            doLogin(dos, dis.readUTF(), dis.readUTF());
                            break;
                        default:
                            ServerMain.error("LOGIN: tundmatu/lubamatu command: " + command);
                    }
                    continue;
                } // login

                ServerMain.debug(6, "kasutajalt " + username + " tuli command: " + command);
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
                    default:
                        ServerMain.error("Tuli sisse tundmatu/lubamatu command: " + command);
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
        // todo: reaalne konto loomine. kui OK, siis logime kasutaja sisse
        // kontrollime, kas sql-is on nimi juba olemas
        // kui ei ole, siis lisame. õnnestumisel märgime logimisperioodi lõppenuks


        synchronized (dos) {
            if (username.length() < 2) {
                dos.writeInt(-1);
                dos.writeUTF("Kasutajanimi liiga lühike");
                ServerMain.debug(5, "createaccount: Kasutajanimi " + username + " liiga lühike.");
                return;
            }
            String uid = ServerMain.sql.getstring("select id from users where username = ?", username);
            dos.writeInt(1);
            if (uid.length() > 0) {
                dos.writeInt(-1);
                dos.writeUTF("Valitud kasutajanimi on juba olemas");
                ServerMain.debug(5, "createaccount: Kasutajanimi " + username + " on juba olemas.");
                return;
            }


            String hashedPassword = ServerPasswordCrypto.generateSecurePassword(password);
            int result = ServerMain.sql.insert("insert into users (id,username,password) values (0,?,?)", username, hashedPassword);

            if (result > 0) {
                // todo: salvestame sessioonitabelisse (seda võiks kasutada web)
                dos.writeInt(1);
                dos.writeUTF("OK, kasutaja loodud, oled sisselogitud");
                ServerMain.debug(5, "createaccount: Kasutajanimi " + username + " loodud.");
                userid = result;
                this.username = username;
                login = false;
                sendLoginmessageToAll();
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
            if (username.length() < 2) {
                dos.writeInt(-1);
                dos.writeUTF("Kasutajanimi liiga lühike");
                ServerMain.debug(5, "dologin: Kasutajanimi " + username + " liiga lühike.");
                return;
            }
            String[] andmebaasist = ServerMain.sql.query(2, "select id,password from users where username = ?", username);
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
                sendLoginmessageToAll();

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
        dos.writeUTF("Fake1");
        dos.writeInt(3);
        dos.writeInt(999);
        dos.writeUTF("Fake2");
        dos.writeInt(3);
        dos.writeInt(1000);
        dos.writeUTF("Fake3");

    } // getUserList


    private void doLogout(DataOutputStream dos) throws Exception {
        // while lõpetatakse ära, socketi sulgemisel võetakse ta ka sessioonilistist maha
        connected = false;
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
        for (ServerGameConnectionHandler player : players) {
            // leidsime mängija, ta tahab minuga ka mängida. anname mõlemale teada
            if (player.getUserid() == invitedUID && player.getInvitedUID() == invitedUID) {
                // see mängija kutsus mind ka mängima, seega aksepteerime mängu ja anname mõlemale teada
                ServerMain.debug(6, "inviteToGame: " + username + " aksepteerib " + player.getUsername());
                synchronized (dos) {
                    dos.writeInt(8);
                    dos.writeInt(player.getUserid());
                    dos.writeInt(999); // todo: siia leida õige mänguID
                    opponentID = player.getUserid();
                } // sync  teade mulle
                DataOutputStream dos2 = player.getDos();
                synchronized (dos2) {
                    dos2.writeInt(8);
                    dos2.writeInt(userid);
                    dos2.writeInt(999); // todo: siia leida õige mänguID
                    player.setOpponentID(userid);
                } // sync teade teisele
            }
            // leidsime mängija, anname talle teada
            else if (player.getUserid() == invitedUID) {
                ServerMain.debug(6, "inviteToGame: " + username + " kutsub " + player.getUsername());
                this.invitedUID = invitedUID;
                DataOutputStream dos2 = player.getDos();
                synchronized (dos2) {
                    dos2.writeInt(7);
                    dos2.writeInt(userid);
                    dos2.writeUTF(username);
                } // sync teade teisele
            }
        }
    } // inviteToGame

    public String toString() {
        return "\n" + userid + ": Nimi " + username + " login: " + login +  " invite: " + invitedUID+ "opponent: " + opponentID;
    }

} //ServerGameConnectionHandler class
