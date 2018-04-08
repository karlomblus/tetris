package chati_leiutis;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
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
            socket.close();
            in.close();
            cont = false;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public ClientThread(Socket socket, Klient client, DataInputStream in,BlockingQueue<Integer> tologinornot) throws Exception{
        this.socket = socket;
        this.client = client;
        this.in = in;
        this.tologinornot = tologinornot;
    }

    public void handleIncomingInput(Integer type) throws Exception {
        System.out.println("input tüüp on " + type);
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
                try{
                //sisselogimise vastus: 1 -- OK, -1 == error
                int loginreturnmessage = in.readInt();
                String loginerrormessage = in.readUTF();
                System.out.println(loginerrormessage);
                System.out.println(loginreturnmessage);
                if(loginreturnmessage == 1){
                    tologinornot.put(1);
                }
                if(loginreturnmessage == -1){
                    System.out.println(loginerrormessage);
                    tologinornot.put(-1);
                }
                //todo nendega midagi teha
                break;}
                catch (Exception e){
                    tologinornot.put(0);
                }
            case 3:
                //keegi tuli chatti juurde?
                int newuser_id = in.readInt();
                System.out.println(newuser_id);
                String newuser_name = in.readUTF();
                System.out.println( newuser_name);
                client.handleUserList(3,newuser_id,newuser_name);
                break;
                //todo panna need nimed listi, neid kasutada etc.
            case 4:
                //keegi lahkus lobbist
                int goneuser_id = in.readInt();
                String goneuser_name = in.readUTF();
                client.handleUserList(4,goneuser_id,goneuser_name);
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
            default:
                tologinornot.put(0);
                break;
        }
    }

    @Override
    public void run() {
        while (cont) {
                try {
                    int incmsg = in.readInt();
                    System.out.println(incmsg);
                    this.handleIncomingInput(incmsg);
            } catch (Exception e) {
                cont = false;
                    System.exit(-1);
                throw new RuntimeException(e);
            }
        }
        shutDown();
        System.out.println("Error, viskame cliendi ka kinni");
        System.exit(-1);
    }
}

//TODO teha client class lõime peale, et üks lõim kuulab serverit, teine ootan konsoolilt sisendeid