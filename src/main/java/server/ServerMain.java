package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {


    public static void main(String[] args) throws Exception {
        List<ServerGameConnectionHandler> players = new ArrayList<>();
        debug(2, "Server alustab");
        ServerSQL sql;
        sql = new ServerSQL();
        debug(3, "Mysql connected");


        Thread web = new Thread(new ServerWebMaster(3, sql));
        web.start();


        // connectioni peale tekitan threadi ja ServerGameConnectionHandler tegeleb
        try (ServerSocket gameServerSocket = new ServerSocket(54321)) {

            while (true) {
                // wait for an incoming connection
                Socket gameSocket = gameServerSocket.accept();
                debug("Uus connection accepted");
                ServerGameConnectionHandler connection = new ServerGameConnectionHandler(gameSocket, players, sql);
                synchronized (players) {
                    debug("Lisame mängija");
                    players.add(connection);
                    ServerMain.debug(6, "Kõik mängijad: " + players);
                }
                Thread connectionHandler = new Thread(connection);
                connectionHandler.start();

            } // while
        } // try


    } // main

    public static void error(String msg, Exception e) {
        System.out.println("Saime errori: " + msg);
        logtofile(msg);
        logtofile(e.getMessage());
        throw new RuntimeException(e);
    } // error


    public static void debug(String msg) {
        debug(3, msg);
    }

    public static void debug(int debuglevel, String msg) {
        System.out.println("DEBUG: " + msg);
        logtofile(msg);

    } // error

    public static void logtofile(String msg) {
        try {
            Files.write(Paths.get("debuglog.txt"), msg.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            //damn, ma ei lenda õhku kui ei suuda faili kirjutada
            System.out.println("DEBUG FILE WRITE ERRROR");
        }
    }

}
