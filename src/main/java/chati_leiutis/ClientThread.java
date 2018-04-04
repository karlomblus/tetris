package main.java.chati_leiutis;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientThread extends Thread {
    Socket socket;
    boolean cont = true;
    Klient client;
    DataInputStream in;

    private void shutDown(){
        try{
        socket.close();
        in.close();

    }catch (Exception e){
            throw new RuntimeException();
        }
    }
    public ClientThread(Socket socket, Klient client) throws Exception {
        this.socket = socket;
        this.client = client;
        in = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        while (cont) {
            try {
                client.recieveMessage(in.readUTF());
            } catch (IOException e) {
                System.out.println("closing thread");
                cont=false;
            }
        }
        shutDown();
    }
}

//TODO teha client class lõime peale, et üks lõim kuulab serverit, teine ootan konsoolilt sisendeid