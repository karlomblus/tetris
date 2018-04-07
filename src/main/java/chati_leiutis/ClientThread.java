package chati_leiutis;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class ClientThread extends Thread {
    Socket socket;
    boolean cont = true;
    Klient client;
    DataInputStream in;

    public void setCont(boolean cont) {
        this.cont = cont;
    }

    public void shutDown() {
        try {
            socket.close();
            in.close();
            cont = false;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public ClientThread(Socket socket, Klient client, DataInputStream in) throws Exception {
        this.socket = socket;
        this.client = client;
        this.in = in;
    }

    public void handleIncomingInput(Integer type) throws IOException {
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

                //sisselogimise vastus: 1 -- OK, -1 == error
                int loginreturnmessage = in.readInt();
                String loginerrormessage = in.readUTF();
                System.out.println(loginerrormessage);
                System.out.println(loginreturnmessage);
                //todo nendega midagi teha
                break;
            case 3:
                //keegi tuli chatti juurde?
                int newuser_id = in.readInt();
                String newuser_name = in.readUTF();
                break;
                //todo panna need nimed listi, neid kasutada etc.
            case 4:
                //keegi lahkus lobbist
                int goneuser_id = in.readInt();
                String goneuser_name = in.readUTF();
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
        }
    }

    @Override
    public void run() {
        while (cont) {
                try {
                    int incmsg = in.readInt();
                    System.out.println(incmsg);
                    this.handleIncomingInput(incmsg);
            } catch (IOException e) {
                System.out.println("closing thread");
                cont = false;
                shutDown();
            }
        }
        shutDown();
    }
}

//TODO teha client class lõime peale, et üks lõim kuulab serverit, teine ootan konsoolilt sisendeid