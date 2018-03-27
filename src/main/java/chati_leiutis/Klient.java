package main.java.chati_leiutis;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Klient implements Runnable {
    public boolean cont = true;
    String name;

    public Klient(String name) {
        this.name = name;
    }

    public void recieveMessage(String message) {
        if (message.equals("logout")) {
            cont = false;
            System.exit(0);
        }//todo väga halb, midagi paremat peab kindlasti välja mõtlema)
        else {
            System.out.println(message+name);
        }
    }

    public static void main(String[] args) throws Exception {
        Klient client = new Klient("ingotester");
        Thread lõim1 = new Thread(client);
        Thread lõim2 = new Thread(new Klient("jaanus"));
        Thread lõim3 = new Thread(new Klient("malle"));
        lõim1.start();
        lõim2.start();
        System.out.println("lõimet alanud");
        //TODO teha, et konstruktoris viime töö uue lõime peale kohe ja loome ka clienthreadi
    }


    @Override
    public void run() {
        try {
            Socket socket = new Socket("localhost", 5000);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            Scanner sc = new Scanner(System.in);

            Thread clienthread = new Thread(new ClientThread(socket, this));
            clienthread.start();
            System.out.println("Connected. Awaiting input...");

            while (cont) {
                String msg = sc.nextLine();
                out.writeUTF(msg);
                out.flush();
                //TODO teha, et konstruktoris viime töö uue lõime peale kohe ja loome ka clienthreadi
            }

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}

