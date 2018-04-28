package tetrispackage;

import java.util.HashMap;

public class ReplayRunner implements Runnable {
    TetrisReplay replay;

    public ReplayRunner(TetrisReplay replay) {
        this.replay = replay;
    }

    private void beginReplay() throws InterruptedException {
        HashMap<Integer, String> commands = new HashMap<>();
        commands.put(100, "LEFT");
        commands.put(150, "LEFT");
        commands.put(200, "LEFT");
        commands.put(350, "LEFT");
        commands.put(400, "RIGHT");
        commands.put(450, "RIGHT");
        commands.put(460, "RIGHT");
        commands.put(470, "RIGHT");
        for (Integer time : commands.keySet()) {
            doCommand(commands.get(time));
            System.out.println("Did the command--------"+commands.get(time));
            Thread.sleep(time);
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
