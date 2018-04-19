package tetrispackage;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
        try {
            tickid++;
            for (ServerGameConnectionHandler player : players) {
                DataOutputStream dos = player.getDos();
                synchronized (dos) {
                    dos.writeInt(100);
                    dos.writeInt(tickid);
                } // sync
            } // iter
        } catch (Exception e) {
            ServerMain.error("Tiksu saatmisel läks midagi valesti",e); // antakse error edasi, logitakse ja lennatakse õhku
        }

    } // tiksJuhtus


} // class
