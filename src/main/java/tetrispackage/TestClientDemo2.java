package tetrispackage;


import chati_leiutis.MessageID;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class TestClientDemo2 {
    public static void main(String[] args) throws Exception {

        System.out.println("Ühendume serveriga");
        Socket socket = new Socket("127.0.0.1", 54321);
        try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {


            System.out.println("Logime sisse õige parooliga");
            dos.writeInt(2);
            dos.writeUTF("TeineTest");
            dos.writeUTF("UusParool");


            loesisendit(dis);

            System.out.println("Küsime userlisti");
            dos.writeInt(3);

            loesisendit(dis);

            System.out.println("saadame kutse 1-le");
            dos.writeInt(7); // saadame 1-l kutse
            dos.writeInt(1);




/*
            // kirjutame chatti
            dos.writeInt(5);
            dos.writeUTF("TeineTest tahab ka midagi öelda");
            dos.writeInt(5);
            dos.writeUTF("TeineTest Ütleb veel midagi");
*/

            /*

            System.out.println("Ütleme chatis tere");
            dos.writeInt(5);
            dos.writeUTF("Tere kes te kõik siin olete");

            loesisendit(dis); // väga halb lahendus, hetkel kiiruga/demoks

            System.out.println("Küsime olemasolevaid mänge, et neid kuskil kuvada");
            dos.writeInt(6);

            loesisendit(dis); // väga halb lahendus, hetkel kiiruga/demoks
*/


            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);
                loesisendit(dis);
            }

            Thread.sleep(20000); // jääme natueseks ajaks passima
            loesisendit(dis); // väga halb lahendus, hetkel kiiruga/demoks

            System.out.println("Logout");
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
                case 100:
                    System.out.println("server tegi tiks: " + dis.readInt());
                    break;
                default:
                    System.out.println("Ma ei tea mis server ütles (" + servervastus + ") ja ei oska mitte midagi teha");

            }
        }
    } // loesisendit

}
