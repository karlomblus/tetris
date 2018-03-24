package chati_leiutis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Klient {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 5000);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream());
             Scanner sc = new Scanner(System.in)) {
            System.out.println("Connected. Awaiting input...");
            while (true) {
                String msg = sc.nextLine();
                out.writeUTF(msg);
                String echo = in.readUTF();
                if(echo.equals("shutdown"))
                    break;
                System.out.println(echo);
                //TODO teha, et konstruktoris viime töö uue lõime peale kohe ja loome ka clienthreadi
            }
        }
    }
}

