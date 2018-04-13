package chati_leiutis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


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

    //eemaldan ID alusel kliendi serverite nimekirjast
    public synchronized void remove(int Id) {
        for (int i = 0; i < clients.size(); i++) {
            if ((clients.get(i).getId()) == Id) {
                clients.remove(i);
                System.out.println("Dropped client with ID: " + Id);
            }
        }
    }


    private int findClientIndex(int Id) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getId() == Id)
                return i;
        }
        return -1;
    }

    //meetod, mida kutsume välja ChatThreadi run tsüklis, saates kõikidele clientidele loetud sõnumi
    public synchronized void toSend(int Id, String message) {
        if (message.equals("logout")) {
            clients.get(findClientIndex(Id)).sendMessage("logout");
        } else {
            for (int i = 0; i < clients.size(); i++) {
                clients.get(i).sendMessage(message);
            }
        }
    }

    //uue serveri lõime loomine
    public void addThread(Socket socket) throws Exception {
        System.out.println("Client accepted ----- " + socket);
        ChatThread client = new ChatThread(this, socket);
        clients.add(client);
        Thread clienthread = new Thread(client);
        clienthread.start();
    }

}