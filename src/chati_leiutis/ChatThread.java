package chati_leiutis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatThread extends Thread {
    private Socket socket;
    private ChatServer server;

    public ChatThread(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        boolean threadDone = false;
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            while (!threadDone) {
                String msg = in.readUTF();
                System.out.println(msg);
                out.writeUTF(msg);
                //shutdown-käsk, lõim lõpetab töö
                if (msg.equals("shutdown")) {
                    threadDone = true;
                    System.out.println("Dropping client...");
                }
            }
            //sulgemine
            //TODO teha eraldi close.() meetod??
            if (socket != null)
                socket.close();
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}

