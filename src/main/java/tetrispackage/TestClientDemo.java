package tetrispackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class TestClientDemo {
    public static void main(String[] args) throws Exception {

        System.out.println("Ühendume serveriga");
        Socket socket = new Socket("tetris.carlnet.ee", 54321);
        try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            System.out.println("Loome uue konto");
            dos.writeInt(1); // konto loomise käsk
            dos.writeUTF("UusUsername");
            dos.writeUTF("UusParool");

            loesisendit(dis); // väga halb lahendus, hetkel kiiruga/demoks

            System.out.println("Küsime userlisti");
            dos.writeInt(3);

            loesisendit(dis); // väga halb lahendus, hetkel kiiruga/demoks

            System.out.println("Ütleme chatis tere");
            dos.writeInt(5);
            dos.writeUTF("Tere kes te kõik siin olete");

            loesisendit(dis); // väga halb lahendus, hetkel kiiruga/demoks

            System.out.println("Küsime olemasolevaid mänge, et neid kuskil kuvada");
            dos.writeInt(6);

            loesisendit(dis); // väga halb lahendus, hetkel kiiruga/demoks

            System.out.println("Ütleme viisakalt bye");
            dos.writeInt(4);

            Thread.sleep(1000); // ootame serveri vastuse ära enne kui toru maha viskame; ei ole hea viis
            loesisendit(dis); // väga halb lahendus, hetkel kiiruga/demoks

        } finally {
            socket.close();
        }


    }

    private static void loesisendit(DataInputStream dis) throws Exception {
        System.out.println("Loeme serveri vastust");
        while (dis.available() > 0) {  // seda konstruktsiooni ei soovitatud vist üldse kasutada
            switch (dis.readInt()) {
                case 1:
                    System.out.println("Uue konto regamise vastus: status: " + dis.readInt() + ", message: " + dis.readUTF());
                    break;
                case 3:
                    System.out.println("Userlist: uid: " + dis.readInt() + ", name: " + dis.readUTF());
                    break;
                case 6:
                    System.out.println("Running games: id: " + dis.readInt() + ", players: " + dis.readUTF()+ " ja " +dis.readUTF());
                    break;
                default:
                    System.out.println("Ma ei tea mis server ütles ja ei oska mitte midagi teha");

            }
        }
    } // loesisendit

}
