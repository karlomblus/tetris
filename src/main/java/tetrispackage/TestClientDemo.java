package tetrispackage;

import chati_leiutis.MessageID;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class TestClientDemo {
    public static void main(String[] args) throws Exception {

        System.out.println("Ühendume serveriga");
        Socket socket = new Socket("tetris.carlnet.ee", 54321);
        try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {


            System.out.println("Logime sisse õige parooliga");
            dos.writeInt(MessageID.LOGIN);
            dos.writeUTF("UusUsername");
            dos.writeUTF("UusParool");
            loesisendit(dis);


            // dos.writeInt(10);  // küsime vaadata 10-t viimat mängu
            // dos.writeInt(0);
            // dos.writeInt(10);

<<<<<<< HEAD
            dos.writeInt(11);  // küsime vaadata mängu 3
            dos.writeInt(3);
=======

//            dos.writeInt(104);  // küsime vaadata mängu 3
            //          dos.writeInt(MessageID.USERLIST);
>>>>>>> 66f8419dd55c0a847f57cd7503dad307cd64845a

/*
            Thread.sleep(5000);
            loesisendit(dis);
            loesisendit(dis);
            loesisendit(dis);
            loesisendit(dis);
            System.out.println("saadame 2-le kutse");
            dos.writeInt(7); // saadame 2-l kutse
            dos.writeInt(2);
            loesisendit(dis);
            loesisendit(dis);
*/

            /*
            System.out.println("Ütleme chatis tere");
            dos.writeInt(5);
            dos.writeUTF("Tere kes te kõik siin olete (esimene kasutaja)");
            loesisendit(dis);
            Thread.sleep(500);

            dos.writeInt(5);
            dos.writeUTF("Tere2 kes te kõik siin olete (esimene kasutaja)");
            loesisendit(dis);
            Thread.sleep(500);

            dos.writeInt(5);
            dos.writeUTF("Tere3 kes te kõik siin olete (esimene kasutaja)");
            loesisendit(dis);
            Thread.sleep(500);
            dos.writeInt(5);
            dos.writeUTF("Tere4 kes te kõik siin olete (esimene kasutaja)");
            loesisendit(dis);
            Thread.sleep(500);

            System.out.println("Küsime olemasolevaid mänge, et neid kuskil kuvada");
            dos.writeInt(6);
*/


            loesisendit(dis);

            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);
                loesisendit(dis);
            }


            Thread.sleep(20000); // ootame serveri vastuse ära enne kui toru maha viskame; ei ole hea viis
            loesisendit(dis); // väga halb lahendus, hetkel kiiruga/demoks

            System.out.println("Ütleme viisakalt bye");
            dos.writeInt(MessageID.LOGOUT);

        } finally {
            socket.close();
        }


    }

    private static void loesisendit(DataInputStream dis) throws Exception {
        System.out.println("Loeme serveri vastust");

        while (dis.available() > 0) {  // seda konstruktsiooni ei soovitatud vist üldse kasutada
            int servervastus = dis.readInt();
            switch (servervastus) {
                case 1:
                    System.out.println("Uue konto regamise vastus: status: " + dis.readInt() + ", message: " + dis.readUTF());
                    break;
                case 2:
                    System.out.println("Sisselogimise vastus: status: " + dis.readInt() + ", message: " + dis.readUTF());
                    break;
                case 3:
                    System.out.println("Userlist: uid: " + dis.readInt() + ", name: " + dis.readUTF());
                    break;
                case 5:
                    System.out.println("Chatmessage: uid " + dis.readInt() + ", user: " + dis.readUTF() + ", message: " + dis.readUTF());
                    break;
                case 6:
                    System.out.println("Running games: id: " + dis.readInt() + ", players: " + dis.readUTF() + " ja " + dis.readUTF());
                    break;
                case 7:
                    System.out.println("Sain kutse: id: " + dis.readInt() + ", name: " + dis.readUTF());
                    break;
                case 8:
                    System.out.println("algas mäng useriga: " + dis.readInt() + ", gameid: " + dis.readInt());
                    break;
                case 10:
                    System.out.println("mängdeulogi: " + dis.readUTF());
                    break;
                case 11:
                    System.out.println("server andis mängulogi mängule: " + dis.readInt());
                    System.out.println("user1: " + dis.readInt() + " " + dis.readUTF());
                    System.out.println("user2: " + dis.readInt() + " " + dis.readUTF());
                    System.out.println("logi: " + dis.readUTF());
                    break;
                case 100:
                    System.out.println("server tegi tiks: " + dis.readInt());
                    break;
                default:
                    System.out.println("Ma ei tea mis server ütles (" + servervastus + ") ja ei oska mitte midagi teha");

            }
        }
    } // loesisendit

}
