package chati_leiutis;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientThread extends Thread {
    Socket socket;
    boolean cont = true;
    Klient client;
    DataInputStream in;

    private void shutDown() {
        try {
            socket.close();
            in.close();

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public ClientThread(Socket socket, Klient client,DataInputStream in) throws Exception {
        this.socket = socket;
        this.client = client;
        this.in = in;
    }

    public void handleIncomingInput(Integer type) throws IOException {
        System.out.println("input tüüp on "+type);
        switch (type) {
            case 1:
                //registreerumise vastus: 1 -- OK, -1 == error
                int registrationreturnmessage = in.readInt();
                String regerrormessage = in.readUTF();
                System.out.println(regerrormessage);
                System.out.println(registrationreturnmessage);
                //todo teha midagi nende vastustega
            case 2:

                //sisselogimise vastus: 1 -- OK, -1 == error
                int loginnreturnmessage = in.readInt();
                String loginerrormessage = in.readUTF();
                //todo nendega midagi teha
            case 3:
                //keegi tuli chatti juurde?
                int newuser_id = in.readInt();
                String newuser_name = in.readUTF();
                //todo panna need nimed listi, neid kasutada etc.
            case 4:
                //keegi lahkus lobbist
                int goneuser_id = in.readInt();
                String goneuser_name = in.readUTF();
            case 5:
                int sentuserID = in.readInt();
                String sentusername = in.readUTF();
                String sentmessage = in.readUTF();
                client.recieveMessage(sentuserID,sentusername,sentmessage);
            case 6:
                //saan käimas olevad mängud
                int gameID = in.readInt();
                String username1 = in.readUTF();
                String username2 = in.readUTF();
                //todo teha midagi, panna listi etc...
        }
    }

    @Override
    public void run() {
        while (cont) {
            try {
                //client.recieveMessage(in.readUTF());
                this.handleIncomingInput(in.readInt());
            } catch (IOException e) {
                throw new RuntimeException(e);
                //System.out.println("closing thread");
                //cont = false;
            }
        }
        shutDown();
    }
}

//TODO teha client class lõime peale, et üks lõim kuulab serverit, teine ootan konsoolilt sisendeid