package chati_leiutis;

import javax.imageio.IIOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class ChatServer implements Runnable {
    private ServerSocket ss;
    private ArrayList<ChatThread> clients = new ArrayList<>();

    //konstruktoris loon serversocketi ja käivitan lõime(vajadusel saab mitu serverit käima panna)
    public ChatServer(int port) throws Exception {
        try {
            System.out.println("Starting server on port: " + port);
            ss = new ServerSocket(port);
            Thread chatthread = new Thread(this);
            chatthread.start();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    //siin ootan kliente, ja saabumisel hakkan neid uue threadi peal serveerima
    @Override
    public void run() {
        System.out.println("Awaiting new clients...");
        while (true) {
            try {
                addThread(ss.accept());
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
    }

    //uue serveri lõime loomine
    public void addThread(Socket socket) throws Exception {
        System.out.println("Client accepted ----- " + socket);
        ChatThread client = new ChatThread(this, socket);
        Thread clienthread = new Thread(client);
        clienthread.start();
    }


    public static void main(String[] args) throws Exception {
        ChatServer server = new ChatServer(5000);

    }
}