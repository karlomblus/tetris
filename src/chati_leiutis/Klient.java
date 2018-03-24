package chati_leiutis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Klient {
    public boolean cont = true;

    public void recieveMessage(String message) {
        if (message.equals("logout")){
            cont = false;
        System.exit(0);}//todo väga halb, midagi paremat peab kindlasti välja mõtlema)
        else {
            System.out.println(message);
        }
    }

    public static void main(String[] args) throws Exception {
        Klient client = new Klient();
        try (Socket socket = new Socket("localhost", 5000);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             Scanner sc = new Scanner(System.in)) {

            Thread clienthread = new Thread(new ClientThread(socket, client));
            clienthread.start();
            System.out.println("Connected. Awaiting input...");

            while (client.cont) {
                String msg = sc.nextLine();
                out.writeUTF(msg);
                out.flush();
                //TODO teha, et konstruktoris viime töö uue lõime peale kohe ja loome ka clienthreadi
            }
        }
    }
}

