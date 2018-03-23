package chati_leiutis;

import javax.imageio.IIOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class ChatServer implements Runnable {
    private Socket socket;
    private ServerSocket ss;
    private DataInputStream in;

    //konstruktoris loon serversocketi ja käivitan lõime
    public ChatServer(int port) throws Exception {
        try {
            ss = new ServerSocket(port);
            Thread chatthread = new Thread(this);
            chatthread.start();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void run() {
        boolean threaddone = false;
        while (!threaddone) {
            System.out.println("Waiting for a client...");
            try {
                Socket socket = ss.accept();
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                System.out.println("Waiting input...");
                boolean done = false;

                while (!done) {
                    String msg = in.readUTF();
                    System.out.println(msg);
                    out.writeUTF(msg);
                    done = msg.equals("close");
                    if (msg.equals("shutdown")) {
                        threaddone = true;
                        done = true;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ChatServer server = new ChatServer(5000);

    }
}