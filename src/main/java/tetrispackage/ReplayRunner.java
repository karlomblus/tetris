package tetrispackage;

import java.util.HashMap;
import java.util.List;

public class ReplayRunner implements Runnable {
    TetrisReplay replay;
    String commandString;

    public ReplayRunner(TetrisReplay replay,String commandString) {
        this.replay = replay;
        this.commandString = commandString;
    }

    private void beginReplay() throws InterruptedException {
        /*
        HashMap<Integer, String> commands = new HashMap<>();
        commands.put(100, "LEFT");
        commands.put(150, "LEFT");
        commands.put(200, "LEFT");
        commands.put(350, "LEFT");
        commands.put(400, "RIGHT");
        commands.put(450, "RIGHT");
        commands.put(460, "RIGHT");
        commands.put(470, "RIGHT");
        */
        String commandString = "1000,RIGHT;150,RIGHT;50,LEFT;200,RIGHT;100,LEFT;300,RIGHT;100,LEFT;500,LEFT";

        //tükitan sobivalt commandide sõne ja täidan käsud vastavad delayga.
        String[] commands = commandString.split(";");
        for (String command:commands) {
            String[] parts = command.split(",");
            int time = Integer.parseInt(parts[0]);
            String moveToReplay = parts[1];
            Thread.sleep(time);
            doCommand(moveToReplay);
            System.out.println("Did the command--------"+moveToReplay);

        }
    }

    private void doCommand(String command) throws InterruptedException {
        switch (command) {
            case "RIGHT":
                replay.getTetromino().moveRight();
                break;
            case "LEFT":
                replay.getTetromino().moveLeft();
                break;
        }
    }
        /*
        Timer timer = new java.util.Timer();
        timer.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        tetromino.moveRight();
                    }
                },
                delay
        );
    }*/

    @Override
    public void run() {
        try {
            beginReplay();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
