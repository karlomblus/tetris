package chati_leiutis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class KlientConnHandler {
    BlockingQueue<Integer> toLoginorNot;
    Klient client;
    Listener listener;
    private DataOutputStream out;
    private DataInputStream in;
    private Socket connection;
    boolean loggedIn = false;
    boolean connected = false;

    public KlientConnHandler(Klient client) {
        this.client = client;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public Socket getConnection() {
        return connection;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public void setConnection(Socket connection) {
        this.connection = connection;
    }

    public void connect()throws Exception{
        try (Socket socket = new Socket("localhost", 54321);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            this.connection = socket;
            this.out = output;
            System.out.println("Connection to tetris.carlnet.ee established...");

            listener = new Listener(connection, client, input, toLoginorNot);
            Thread clienthread = new Thread(listener);
            clienthread.start();
  //          client.setOut(this.out);
            loggedIn = true;}
    }
}

