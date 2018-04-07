package tetrispackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    private int status = 0;   // 1: lobbys  2: mängib 3: ???
    private int userid = 0;       // mängija userID
    private String username = "";

    ServerGameConnectionHandler(Socket socket, List<ServerGameConnectionHandler> players) {
        this.socket = socket;
        this.players = players;
    }

    public int getStatus() {
        return status;
    }

    public int getUserid() {
        return ;
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
                    default:
                        ServerMain.error("Tuli sisse tundmatu/lubamatu command: " + command);
                } // command switch
            } // while connected

        } catch (SocketException e) {
            // Kas siis võin ilma põhjuseta kinni püüda kui ma ei taha, et see edasi throwtakse ning tahan lihtsalt viisakalt olukorda lõpetada?
            ServerMain.debug("Teine pool sulges ootamatult socketi, teeme siis sama");
        } catch (Exception e) {
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
            String uid = ServerMain.sql.getstring("select id from users where username = ?", username);
            dos.writeInt(1);
            if (uid.length() > 0) {
                dos.writeInt(-1);
                dos.writeUTF("Valitud kasutajanimi on juba olemas");
                return;
            }


            String hashedPassword = ServerPasswordCrypto.generateSecurePassword(password);
            int result = ServerMain.sql.insert("insert into users (id,username,password) values (0,?,?)", username, hashedPassword);

            if (result > 0) {
                // todo: salvestame sessioonitabelisse (seda võiks kasutada web)
                dos.writeInt(1);
                dos.writeUTF("OK, kasutaja loodud, oled sisselogitud");
                userid = result;
                this.username = username;
                login = false;
            } else {
                dos.writeInt(-1);
                dos.writeUTF("Kasutaja lisamine andmebaasi ebaõnnestus");
            }
        } // sync
    } // createAccount


    private void doLogin(DataOutputStream dos, String username, String password) throws Exception {
        synchronized (dos) {
            dos.writeInt(2);
            String[] andmebaasist = ServerMain.sql.query(2, "select id,password from users where username = ?", username);
            if (andmebaasist[0].length() == 0) {
                dos.writeInt(-1);
                dos.writeUTF("Sellist kasutajanime ei ole"); // väidetavalt pole turvaline eraldi infot anda, aga regamisprotsessis saab kasutajanime eksisteerimist niikuinii kontrollida
                return;
            }
            boolean passwordMatch = ServerPasswordCrypto.verifyUserPassword(password, andmebaasist[1]);
            if (passwordMatch) {
                dos.writeInt(1);
                dos.writeUTF("OK");
                userid = Integer.parseInt(andmebaasist[0]);
                this.username = username;
                login = false;
            } else {
                dos.writeInt(-1);
                dos.writeUTF("Vale parool");
            }

            // todo: kui OK, siis salvestame sessioonitabelisse


        } // sync
    } // doLogin


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

    // todo:   mängu kutsumine  : saadame teisele kasutajale kutse. endale jätame meelde, et oleme kutsunud Y
    // todo    kutsele vastamine: sisuliselt sama kui kutse saatmine, aga teisel peab olema kutsutav minu,id.
    //         kui vastus õnnestub, siis mõlema staatuseks, et mängib.   lobby nimekirjast maha
    //                                                                   mängupaaride nimekirja sisse


    public String toString() {
        return username + ": Nimi " + username + " login: " + login + " status " + status;
    }

} //ServerGameConnectionHandler class
