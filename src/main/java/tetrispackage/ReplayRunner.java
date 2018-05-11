package tetrispackage;

import java.util.HashMap;
import java.util.List;

public class ReplayRunner implements Runnable {
    Tetromino replay;
    String commandString;

    public ReplayRunner(Tetromino replay,String commandString) {
        this.replay = replay;
        this.commandString = commandString;
    }

    private void beginReplay() throws InterruptedException {
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

    private void doCommand(String command) {
        switch (command) {
            case "RIGHT":
                replay.moveRight();
                break;
            case "LEFT":
                replay.moveLeft();
                break;
            case "UP":
                replay.rotateLeft();
                break;
            case "DOWN":
                replay.drop();
                break;
        }
    }

    @Override
    public void run() {
        try {
            beginReplay();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
