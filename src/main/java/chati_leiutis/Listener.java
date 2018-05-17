package chati_leiutis;

import javafx.application.Platform;
import javafx.util.Duration;

import static chati_leiutis.MessageID.*;

import java.io.DataInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class Listener extends Thread {
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

    public Listener(Socket socket, Klient client, DataInputStream in, BlockingQueue<Integer> tologinornot) {
        this.socket = socket;
        this.client = client;
        this.in = in;
        this.tologinornot = tologinornot;
    }

    public void handleIncomingInput(Integer type) throws Exception {
        switch (type) {
            case REGISTRATION:
                //registreerumise vastus: 1 -- OK, -1 == error
                int registrationreturnmessage = in.readInt();
                String regerrormessage = in.readUTF();
                System.out.println(regerrormessage);
                System.out.println(registrationreturnmessage);
                //todo teha midagi nende vastustega
                break;
            case LOGIN:
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
            case USERLIST:
                //keegi tuli chatti juurde?
                int newuser_id = in.readInt();
                System.out.println(newuser_id);
                String newuser_name = in.readUTF();
                System.out.println(newuser_name);
                Platform.runLater(() -> client.handleUserList(3, newuser_id, newuser_name));

                break;
            //todo panna need nimed listi, neid kasutada etc.
            case LOGOUT:
                //keegi lahkus lobbist
                int goneuser_id = in.readInt();
                String goneuser_name = in.readUTF();
                Platform.runLater(() -> client.handleUserList(4, goneuser_id, goneuser_name));

                break;
            case SENDMESSAGE:
                int sentuserID = in.readInt();
                String sentusername = in.readUTF();
                String sentmessage = in.readUTF();
                client.recieveMessage(sentuserID, sentusername, sentmessage);
                break;
            case GETRUNNINGGAMES:
                //saan käimas olevad mängud
                int gameID = in.readInt();
                String username1 = in.readUTF();
                String username2 = in.readUTF();
                break;
            //todo teha midagi, panna listi etc...
            case SENDCHALLENGE:
                //tulev challenge
                int challengerID = in.readInt();
                String challengerName = in.readUTF();
                //mängin soundi, kui keegi kutsub su mängu
                client.getGamenotificationsound().seek(new Duration(0));
                client.getGamenotificationsound().play();
                Platform.runLater(() -> client.showIncomingChallengeWindow(challengerID, challengerName));
                break;
            case CHALLENGERESPONSE:
                int OpponentID = in.readInt();
                int startinggameID = in.readInt();
                if (!client.isMpgameopen()) {
                    System.out.println("Starting game with " + OpponentID + " with a game ID of " + startinggameID);
                    //alustan mängu, andes kaasa vastase ID
                    Platform.runLater(() -> client.showMultiplayer(OpponentID));
                }
                //võeti challenge vastu

                break;
            case CHALLENGEREFUSE:
                //todo kui vastane keeldub kutsest
                in.readInt();
                String message = in.readUTF();
                client.recieveMessage(-1, "System", message + " keeldus!");
                client.challengewindow.close();

                break;
            case 105:
                //sissetulev mängu chat message
                in.readInt();
                String name = in.readUTF();
                String privatemessage = in.readUTF();
                client.getMultiplayerGame().getPrivateChat().addNewMessage(name, privatemessage);
                break;
            case 100:
                //Tulevad tiksud
                int tiksuID = in.readInt(); //tiksuID
                client.getMultiplayerGame().setTickValue(tiksuID);
                break;
            case 101:
                int nuputiskuID = in.readInt(); //tiksuID
                char nupuvajutus = in.readChar();
                int kellenupuvajutusID = in.readInt(); //Do I need this?
                client.getMultiplayerGame().setOpponentMoveTiksuID(nuputiskuID);
                client.getMultiplayerGame().setOpponentMoved(nupuvajutus);
                System.out.println("Sain nupu " + String.valueOf(nupuvajutus) + " tiksu id'ga " + nuputiskuID);
                break;
            case 102:
                client.getMultiplayerGame().getPrivateChat().opponentLeft();
                //TODO kui vastane sulgeb oma mp mängu/lahkub
            case 103:
                int kellele = in.readInt();
                char klots = in.readChar();
                if (kellele == client.getMultiplayerGame().getOpponentID()) {
                    System.out.println("Opponent got random Tetro " + String.valueOf(klots));
                } else {
                    System.out.println("I got random tetro " + String.valueOf(klots));
                }
                if (client.getMultiplayerGame().getOpponentID() == kellele) {
                    client.getMultiplayerGame().getOpponentTetromino().setRandomTetrominoMP(klots);
                    client.getMultiplayerGame().getOpponentTetromino().setNewRandomTetroReceived(true);
                } else {
                    client.getMultiplayerGame().getMyTetromino().setRandomTetrominoMP(klots);
                    client.getMultiplayerGame().getMyTetromino().setNewRandomTetroReceived(true);
                }
                break;
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
            } catch (SocketException e) {
                cont = false;
                client.getEkraan().appendText("Disconnected... please restart to reconnect.");
                client.getKonsool().setDisable(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        shutDown();
    }
}
