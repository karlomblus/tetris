package tetrispackage;

import java.io.DataOutputStream;
import java.util.*;

public class ServerGameData {
    private Timer timer;
    private List<ServerGameConnectionHandler> players; // siin on kõik mängijad  (esialgu üle kahe neid pole, aga mõtleme ette)
    private int tickid = 0;
    private int gameid = 0; // mängu ID.

    public ServerGameData(ServerGameConnectionHandler player1, ServerGameConnectionHandler player2) {
        players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        System.out.println("Lisasime mängija1 " + player1.getUserid()+": "+ player1.getUsername());
        System.out.println("Lisasime mängija2 " + player2.getUserid()+": "+ player2.getUsername());

        // todo: gameid tuleb mängu lisamisest mysql-i
        gameid=999;



    }


    void start() throws Exception{
        //System.out.println("mängijaid: " + players.size());
        for (ServerGameConnectionHandler player : players) {
            //System.out.println("hakkame start käsku saatma mängijale " + player.getUsername());
            DataOutputStream dos = player.getDos();
            synchronized (dos) {
                dos.writeInt(8);
                dos.writeInt(player.getUserid());
                dos.writeInt(gameid);
            } // sync
        } // iter


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                tiksJuhtus();
            }
        }, 300,300);


    } // start


    private void tiksJuhtus() {

            tickid++;
            for (ServerGameConnectionHandler player : players) {
                try {
                DataOutputStream dos = player.getDos();
                synchronized (dos) {
                    dos.writeInt(100);
                    dos.writeInt(tickid);
                } // sync
                } catch (Exception e) { // kui üks mängija läheb katki, siis ei taha me õhku lennata vaid teine mängija jääb üksi mängima
                    ServerMain.debug(1,"Tiksu saatmisel kasutajale "+player.getUsername()+" läks midagi valesti.");
                    ServerMain.debug(5,e.toString());
                    player.setOpponentID(0);
                    players.remove(player); // teise mängija viskame minema
                }
            } // iter


    } // tiksJuhtus


    public void sendNewTetromino(int kellele) {
        char[] possibleTetrominos = {'I', 'O', 'Z', 'S', 'T', 'J', 'L'};
        Random rand = new Random();
        char randomTetromino = possibleTetrominos[rand.nextInt(possibleTetrominos.length)];

        for (ServerGameConnectionHandler player : players) {
            try {
                DataOutputStream dos = player.getDos();
                synchronized (dos) {
                    dos.writeInt(103);
                    dos.writeInt(kellele);
                    dos.writeChar(randomTetromino);
                } // sync
            } catch (Exception e) { // kui üks mängija läheb katki, siis ei taha me õhku lennata vaid teine mängija jääb üksi mängima
                ServerMain.debug(1,"Tetromino info saatmisel kasutajale "+player.getUsername()+" läks midagi valesti.");
                ServerMain.debug(5,e.toString());
                player.setOpponentID(0);
                players.remove(player); // teise mängija viskame minema
            }
        } // iter

    }  // sendNewTetromino


    public void removeUserFromGame(ServerGameConnectionHandler removeUser) {
        players.remove(removeUser);
        removeUser.setOpponentID(0);
        // ütleme teistele, et ta läks minema
        for (ServerGameConnectionHandler player : players) {
            try {
                DataOutputStream dos = player.getDos();
                synchronized (dos) {
                    dos.writeInt(102);
                    dos.writeInt(player.getUserid());
                } // sync
            } catch (Exception e) { // kui üks mängija läheb katki, siis ei taha me õhku lennata vaid teine mängija jääb üksi mängima
                ServerMain.debug(1,"Lahkumisteate saatmisel kasutajale "+player.getUsername()+" läks midagi valesti.");
                ServerMain.debug(5,e.toString());
                player.setOpponentID(0); // viskame selle minema kellele ei saanud kirjutada
                players.remove(player);
            }
        } // iter
    } // removeUserFromGame
} // class
