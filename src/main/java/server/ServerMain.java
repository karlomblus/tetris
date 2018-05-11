package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {


    public static void main(String[] args) throws Exception {
        List<ServerGameConnectionHandler> players = new ArrayList<>();
        debug(2,"Server alustab");
        ServerSQL sql;
        sql = new ServerSQL();
        debug(3,"Mysql connected");


        Thread web = new Thread(new ServerWebMaster(3));
        web.start();



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

    public static void error(String msg, Exception e)  {
        System.out.println("Saime errori: " + msg);
        //todo: logime errori
        throw new RuntimeException(e);
    } // error


    public static void debug(String msg) {
        debug(3, msg);
    }

    public static void debug(int debuglevel, String msg) {
        System.out.println("DEBUG: " + msg);
        //todo: logime 
    } // error

}
