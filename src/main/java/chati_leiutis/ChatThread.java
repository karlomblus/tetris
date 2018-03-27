package main.java.chati_leiutis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatThread implements Runnable {
    private Socket socket;
    private ChatServer server;
    //mingi suvalise cliendi ID, default -1
    private int Id = -1;
    private DataInputStream in;
    private DataOutputStream out;

    public ChatThread(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        boolean threadDone = false;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            while (!threadDone) {
                String msg = in.readUTF();
                System.out.println(msg);
                server.toSend(Id,msg);

                //shutdown-käsk, lõim lõpetab töö
                if (msg.equals("logout")) {
                    threadDone = true;
                    System.out.println("Dropping client...");
                    server.remove(Id);
                }
            }
            //sulgemine
            close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public int getId() {
        return Id;
    }

    public void sendMessage(String message){
        try{
            out.writeUTF(message);
            out.flush();
        }catch (IOException e){
            throw new RuntimeException();
        }
    }
    public void close() throws IOException {
        if (socket != null)
            socket.close();
        if (in != null)
            in.close();
        if (out != null)
            out.close();
    }

}