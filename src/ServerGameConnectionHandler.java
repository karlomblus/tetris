import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeUTF("OK");
        login = false;
    } // createAccount


    private void doLogin(DataOutputStream dos, String username, String password) throws Exception {
        // todo: reaalne sisselogimine
        dos.writeInt(2);
        dos.writeInt(1);
        dos.writeUTF("OK");
        login = false;
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


        login = false;
    } // doLogin


    private void doLogout(DataOutputStream dos) throws Exception {
        // todo: reaalne
        connected = false;
    } // doLogout

    private void saveChatmessage(DataOutputStream dos, String message) throws Exception {
        //todo: reaalne

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

    } // getRunningGames

} //ServerGameConnectionHandler class
