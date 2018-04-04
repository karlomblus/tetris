package tetrispackage;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String[] args) throws Exception {

System.out.println("Server alustab");

        // tekitame mingi hulga threade weebi ühendustega tegelemiseks

        // connectioni peale tekitan threadi ja ServerGameConnectionHandler tegeleb
        try (ServerSocket gameServerSocket = new ServerSocket(54321)) {

            while (true) {
                // wait for an incoming connection
                Socket gameSocket = gameServerSocket.accept();
                debug("Uus connection accepted");
                Thread connectionHandler = new Thread(new ServerGameConnectionHandler(gameSocket));
                connectionHandler.start();

            } // while
        } // try


        // lisaks kuulan ka porti 80
        // kui tuleb ühendus, annan selle esimesele vabale threadile (ei hakka igakord uut tegema)


    } // main

    public static void error(String msg) throws Exception {
        System.out.println("Saime errori: " + msg);
        //todo: logime errori
        throw new java.lang.Exception("custom error: " + msg);
    } // error


    public static void debug(String msg) throws Exception {
      debug(5,msg);
  }

    public static void debug(int debuglevel, String msg) throws Exception {
        System.out.println("DEBUG: " + msg);
        //todo: logime 
    } // error

}
