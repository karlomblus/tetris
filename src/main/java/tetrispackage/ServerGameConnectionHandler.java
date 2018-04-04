package tetrispackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ServerGameConnectionHandler implements Runnable {

    // siin tegeleme sissetulnud mängu ühendustega
    // esialgu faken mingi kasutajaskonna ja sisu

    Socket socket;
    boolean connected = true; // kui see maha keeratakse, siis on sessioon läbi
    boolean login = true;  // kasutaja on logimisfaasis

    ServerGameConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // kood, mida soovime paralleelselt käivitada

        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

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
            System.out.println("Teine pool sulges ootamatult socketi, teeme siis sama");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                System.out.println("Sulgeme socketi");
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

            String hashedPassword = tetrispackage.PasswordCrypto.generateSecurePassword(password);
            int result = ServerMain.sql.insert("insert into users (id,username,password) values (0,?,?)", username, hashedPassword);

            if (result > 0) {
                // todo: salvestame sessioonitabelisse (seda võiks kasutada web)
                dos.writeInt(1);
                dos.writeUTF("OK, kasutaja loodud, oled sisselogitud");
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
            String andmebaasist = ServerMain.sql.getstring("select password from users where username = ?", username);
            if (andmebaasist.length() ==0) {
                dos.writeInt(-1);
                dos.writeUTF("Sellist kasutajanime ei ole"); // väidetavalt pole turvaline eraldi infot anda, aga regamisprotsessis saab kasutajanime eksisteerimist niikuinii kontrollida
                return;
            }
            boolean passwordMatch = PasswordCrypto.verifyUserPassword(password, andmebaasist);
            if (passwordMatch) {
                dos.writeInt(1);
                dos.writeUTF("OK");
                login = false;
            } else {
                dos.writeInt(-1);
                dos.writeUTF("Vale parool");
            }

            // todo: kui OK, siis salvestame sessioonitabelisse


        } // sync
    } // doLogin


    private void getUserList(DataOutputStream dos) throws Exception {
        // todo: reaalne userlist. Hetkel fakeme data
        dos.writeInt(3);
        dos.writeInt(1);
        dos.writeUTF("Juhan");
        dos.writeInt(3);
        dos.writeInt(2);
        dos.writeUTF("Kalle");
        dos.writeInt(3);
        dos.writeInt(3);
        dos.writeUTF("Malle");
        dos.writeInt(3);
        dos.writeInt(4);
        dos.writeUTF("Theo");
        dos.writeInt(3);
        dos.writeInt(5);
        dos.writeUTF("Karl");
        dos.writeInt(3);
        dos.writeInt(8);
        dos.writeUTF("Jüri");
        dos.writeInt(3);
        dos.writeInt(12);
        dos.writeUTF("Mari");

        // variant1:  loeme mälust ette kasutajate listi.  vist on mõtekam
        // variant2: loeme sql-ist listi ette (teadmata, kas see on õige)

        login = false;
    } // getUserList


    private void doLogout(DataOutputStream dos) throws Exception {
        // todo: reaalne
        // kustutame kasutaja sessioonilistist
        // eemaldame ta sisseloginud kasutajate listist
        connected = false;
    } // doLogout

    private void saveChatmessage(DataOutputStream dos, String message) throws Exception {
        //todo: salvestame sql-i
        // saadame kõigile seesolijatele välja

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


} //ServerGameConnectionHandler class
