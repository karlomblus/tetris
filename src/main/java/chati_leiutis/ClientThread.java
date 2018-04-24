package chati_leiutis;

import javafx.application.Platform;
import tetrispackage.TetrisGraafikaMultiplayer;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ClientThread extends Thread {
    Socket socket;
    boolean cont = true;
    Klient client;
    DataInputStream in;
    BlockingQueue<Integer> tologinornot;

    public void shutDown() {
        try {
            in.close();
            socket.close();
            cont = false;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public ClientThread(Socket socket, Klient client, DataInputStream in, BlockingQueue<Integer> tologinornot) throws Exception {
        this.socket = socket;
        this.client = client;
        this.in = in;
        this.tologinornot = tologinornot;
    }

    public void handleIncomingInput(Integer type) throws Exception {
        if (type != 100) {
            System.out.println("input tüüp on " + type);
        }

        switch (type) {
            case 1:
                //registreerumise vastus: 1 -- OK, -1 == error
                int registrationreturnmessage = in.readInt();
                String regerrormessage = in.readUTF();
                System.out.println(regerrormessage);
                System.out.println(registrationreturnmessage);
                //todo teha midagi nende vastustega
                break;
            case 2:
                try {
                    //sisselogimise vastus: 1 -- OK, -1 == error
                    int loginreturnmessage = in.readInt();
                    String loginerrormessage = in.readUTF();
                    System.out.println(loginerrormessage);
                    System.out.println(loginreturnmessage);
                    if (loginreturnmessage == 1) {
                        tologinornot.put(1);
                    }
                    if (loginreturnmessage == -1) {
                        System.out.println(loginerrormessage);
                        tologinornot.put(-1);
                    }
                    //todo nendega midagi teha
                    break;
                } catch (Exception e) {
                    tologinornot.put(0);
                    break;
                }
            case 3:
                //keegi tuli chatti juurde?
                int newuser_id = in.readInt();
                System.out.println(newuser_id);
                String newuser_name = in.readUTF();
                System.out.println(newuser_name);
                Platform.runLater(() -> client.handleUserList(3, newuser_id, newuser_name));

                break;
            //todo panna need nimed listi, neid kasutada etc.
            case 4:
                //keegi lahkus lobbist
                int goneuser_id = in.readInt();
                String goneuser_name = in.readUTF();
                Platform.runLater(() -> client.handleUserList(4, goneuser_id, goneuser_name));

                break;
            case 5:
                int sentuserID = in.readInt();
                String sentusername = in.readUTF();
                String sentmessage = in.readUTF();
                client.recieveMessage(sentuserID, sentusername, sentmessage);
                break;
            case 6:
                //saan käimas olevad mängud
                int gameID = in.readInt();
                String username1 = in.readUTF();
                String username2 = in.readUTF();
                break;
            //todo teha midagi, panna listi etc...
            case 7:
                //tulev challenge
                int challengerID = in.readInt();
                String challengerName = in.readUTF();
                Platform.runLater(() -> client.showIncomingChallengeWindow(challengerID, challengerName));
                break;
            case 8:
                int OpponentID = in.readInt();
                int startinggameID = in.readInt();
                if (!client.isMpgameopen()) {
                    System.out.println("Starting game with " + OpponentID + " with a game ID of " + startinggameID);
                    //alustan mängu, andes kaasa vastase ID
                    Platform.runLater(() -> client.showMultiplayer(OpponentID));
                }
                client.setMpgameopen(true);
                //võeti challenge vastu

                break;
            case 9:
                //todo kui vastane keeldub kutsest
                in.readInt();
                String message = in.readUTF();
                client.recieveMessage(-1, "System", message + " keeldus!");

                break;
            case 105:
                //sissetulev mängu chat message
                in.readInt();
                String name = in.readUTF();
                String privatemessage = in.readUTF();
                client.getMultiplayerGame().addNewMessage(name, privatemessage);
                break;
            case 100:
                //Tulevad tiksud
                int tiskuID = in.readInt(); //tiksuID
                client.getMultiplayerGame().setTickValue(tiskuID);
                break;
            case 101:
                int nuputiskuID = in.readInt(); //tiksuID
                char nupuvajutus = in.readChar();
                int kellenupuvajutusID = in.readInt();
                client.getMultiplayerGame().setOpponentMoved(nupuvajutus);
                System.out.println("Sain nupu " + nupuvajutus + " tiksu id'ga " + nuputiskuID);
            case 103:
                int kellele = in.readInt();
                char klots = in.readChar();
                client.getMultiplayerGame().setRandomTetro(klots);
                client.getMultiplayerGame().setRandomTetroReceived(true);

            default:
                if (tologinornot.size() == 0)
                    tologinornot.put(0);
                break;
        }
    }

    @Override
    public void run() {
        while (cont) {
            try {
                int incmsg = in.readInt();
                this.handleIncomingInput(incmsg);
            } catch (Exception e) {
                cont = false;
                System.out.println("Socket kinni/ootasin muud sisendit serverilt");
                //throw new RuntimeException(e);
            }
        }
        shutDown();
    }
}

//TODO teha client class lõime peale, et üks lõim kuulab serverit, teine ootan konsoolilt sisendeid