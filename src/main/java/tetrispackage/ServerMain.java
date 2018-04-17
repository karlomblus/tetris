package tetrispackage;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {


    public static void main(String[] args) throws Exception {
        List<ServerGameConnectionHandler> players = new ArrayList<>();
        System.out.println("Server alustab");
        ServerSQL sql;
        sql = new ServerSQL();
        System.out.println("Mysql connected");


        // plan: weebiserver täiesti eraldi
        // tekitame mingi hulga threade weebi ühendustega tegelemiseks
        // lisaks kuulan ka porti 80
        // kui tuleb ühendus, annan selle esimesele vabale threadile (ei hakka igakord uut tegema)

        // connectioni peale tekitan threadi ja ServerGameConnectionHandler tegeleb
        try (ServerSocket gameServerSocket = new ServerSocket(54321)) {

            while (true) {
                // wait for an incoming connection
                Socket gameSocket = gameServerSocket.accept();
                debug("Uus connection accepted");
                ServerGameConnectionHandler connection = new ServerGameConnectionHandler(gameSocket,players, sql);
                synchronized (players){
                    debug("Lisame mängija");
                    players.add(connection);
                    ServerMain.debug(6,"Kõik mängijad: "+ players);
                }
                Thread connectionHandler = new Thread(connection);
                connectionHandler.start();

            } // while
        } // try





    } // main

    public static void error(String msg) throws Exception {
        System.out.println("Saime errori: " + msg);
        //todo: logime errori
        throw new java.lang.Exception("custom error: " + msg);
    } // error


    public static void debug(String msg) {
        debug(3, msg);
    }

    public static void debug(int debuglevel, String msg) {
        System.out.println("DEBUG: " + msg);
        //todo: logime 
    } // error

}
