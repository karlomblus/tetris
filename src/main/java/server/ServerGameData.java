package server;

import java.io.DataOutputStream;
import java.util.*;

public class ServerGameData {
    private Timer timer;
    private List<ServerGameConnectionHandler> players; // siin on kõik mängijad  (esialgu üle kahe neid pole, aga mõtleme ette)
    private int tickid = 0;
    private int gameid = 0; // mängu ID.
    private ServerSQL sql;
    private Map<Integer, List<Character>> klotsijada;
    private boolean running;


    public ServerGameData(ServerGameConnectionHandler player1, ServerGameConnectionHandler player2, ServerSQL sql) {
        this.sql = sql;
        players = new ArrayList<>();
        klotsijada = new HashMap<>();
        players.add(player1);
        players.add(player2);
        System.out.println("Lisasime mängija1 " + player1.getUserid() + ": " + player1.getUsername());
        System.out.println("Lisasime mängija2 " + player2.getUserid() + ": " + player2.getUsername());

        gameid = sql.insert("insert into mangud (id,player1,player2,started) values (0,?,?,now() )", String.valueOf(player1.getUserid()), String.valueOf(player2.getUserid()));
        ServerMain.debug(4, "Algatasime mängu ID-ga: " + gameid);

        klotsijada.put(player1.getUserid(), new LinkedList<>()); // kui natuke setter ümber teha, siis toetatakse suvaline hulk mängijaid
        klotsijada.put(player2.getUserid(), new LinkedList<>());

    }


    void start() {
        running = true;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                tiksJuhtus();
            }
        }, 300, 300);

    } // start


    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getTickid() {
        return tickid;
    }

    private void tiksJuhtus() {


        tickid++;
        sql.insert("insert into mangulogi (id, gameid,timestamp_sql ,timestampms,userid,tickid,tegevus) values (0,?,now(),?,0,?,0 )", String.valueOf(gameid), String.valueOf(System.currentTimeMillis()), String.valueOf(tickid));

        for (ServerGameConnectionHandler player : players) {
            try {
                DataOutputStream dos = player.getDos();
                synchronized (dos) {
                    dos.writeInt(100);
                    dos.writeInt(tickid);
                } // sync
            } catch (Exception e) { // kui üks mängija läheb katki, siis ei taha me õhku lennata vaid teine mängija jääb üksi mängima
                ServerMain.debug(1, "Tiksu saatmisel kasutajale " + player.getUsername() + " läks midagi valesti.");
                ServerMain.debug(5, e.toString());
                player.setOpponentID(0);
                players.remove(player); // teise mängija viskame minema
            }
        } // iter


    } // tiksJuhtus


    public void sendNewTetromino(int kellele) {
        ServerMain.debug(7, "sendNewTetromino: id " + kellele + " tellis uue tetromino");
        char randomTetromino;

        if (klotsijada.get(kellele).size() > 0) { // meil on selle useri jaoks juba random olemas
            randomTetromino = klotsijada.get(kellele).get(0);
            klotsijada.get(kellele).remove(0);
            System.out.println("tetromino listist: " + randomTetromino);
        } else { // sel useril pole enam klotse
            char[] possibleTetrominos = {'I', 'O', 'Z', 'S', 'T', 'J', 'L'};
            Random rand = new Random();
            randomTetromino = possibleTetrominos[rand.nextInt(possibleTetrominos.length)];
            System.out.println("tetromino uus: " + randomTetromino);
            // lisame loodud random klotsi ka kõigile teistele (eeldusel, et neid on kunagi rohkem kui 1)
            for (Map.Entry<Integer, List<Character>> integerListEntry : klotsijada.entrySet()) {
                if (integerListEntry.getKey() != kellele) {
                    integerListEntry.getValue().add(randomTetromino);
                }
            }
        }


        sql.insert("insert into mangulogi (id, gameid,timestamp_sql ,timestampms,userid,tickid,tegevus) values (0,?,now(),?,?,?,? )", String.valueOf(gameid), String.valueOf(System.currentTimeMillis()), String.valueOf(kellele), String.valueOf(tickid), String.valueOf(randomTetromino));
        for (ServerGameConnectionHandler player : players) {
            try {
                DataOutputStream dos = player.getDos();
                synchronized (dos) {
                    dos.writeInt(103);
                    dos.writeInt(kellele);
                    dos.writeChar(randomTetromino);
                } // sync
            } catch (Exception e) { // kui üks mängija läheb katki, siis ei taha me õhku lennata vaid teine mängija jääb üksi mängima
                ServerMain.debug(1, "Tetromino info saatmisel kasutajale " + player.getUsername() + " läks midagi valesti.");
                ServerMain.debug(5, e.toString());
                player.setOpponentID(0);
                players.remove(player); // teise mängija viskame minema
            }
        } // iter

    }  // sendNewTetromino


    public void removeUserFromGame(ServerGameConnectionHandler removeUser) {
        ServerMain.debug("Kasutaja saatis 102 ja lahkus mängust: " + removeUser.getUsername());
        players.remove(removeUser);
        removeUser.setOpponentID(0);

        sql.insert("insert into mangulogi (id, gameid,timestamp_sql ,timestampms,userid,tickid,tegevus) values (0,?,now(),?,0,?,? )", String.valueOf(this.getGameid()), String.valueOf(System.currentTimeMillis()), String.valueOf(this.getTickid()),"102");

        // ütleme teistele, et ta läks minema
        for (ServerGameConnectionHandler player : players) {
            try {
                DataOutputStream dos = player.getDos();
                synchronized (dos) {
                    if (running) {
                        sql.query("update users  set points=points+1 where id = ? limit 1",String.valueOf(player.getUserid()));
                        ServerMain.debug(6,"Kasutaja "+ player.getUsername()+ " sai punkti");
                    }
                    dos.writeInt(102);
                    dos.writeInt(player.getUserid());
                } // sync
            } catch (Exception e) { // kui üks mängija läheb katki, siis ei taha me õhku lennata vaid teine mängija jääb üksi mängima
                ServerMain.debug(1, "Lahkumisteate saatmisel kasutajale " + player.getUsername() + " läks midagi valesti.");
                ServerMain.debug(5, e.toString());
                player.setOpponentID(0); // viskame selle minema kellele ei saanud kirjutada
                players.remove(player);
            }
        } // iter
        running = false;
    } // removeUserFromGame


    public int getGameid() {
        return gameid;
    }
} // class
