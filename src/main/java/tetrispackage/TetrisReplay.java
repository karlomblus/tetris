package tetrispackage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class TetrisReplay {
    private static final int resoWidth = 150;
    private static final int resoHeight = 330;
    private Tetromino player1tetromino;
    private Tetromino player2tetromino;
    private Map<KeyCode, Boolean> currentActiveKeys = new HashMap<>();

    public void start(Stage peaLava, String name, String commandString1, String commandString2) {
        //mängijate nimed...
        String nimi = name.split("  ")[0];
        String nimi1 = nimi.split("-")[0];
        String nimi2 = nimi.split("-")[1];
        Label namelabel1 = new Label(nimi1);
        Label namelabel2 = new Label(nimi2);

        Group player1 = new Group();
        Group player2 = new Group();
        VBox player1box = new VBox();
        VBox player2box = new VBox();

        TetrisRectangle player1tetrisRect = new TetrisRectangle();
        player1tetrisRect.fill(player1);
        player1tetromino = new Tetromino(player1tetrisRect.getRistkülik());

        TetrisRectangle player2tetrisRect = new TetrisRectangle();
        player2tetrisRect.fill(player2);
        player2tetromino = new Tetromino(player2tetrisRect.getRistkülik());

        player1box.getChildren().addAll(namelabel1,player1);
        player2box.getChildren().addAll(namelabel2,player2);
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.getChildren().addAll(player1box,player2box);

        Timeline tickTime = new Timeline(new KeyFrame(Duration.seconds(0.2), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!player1tetromino.gameStateOver()) {
                    player1tetromino.tick();
                    if (player1tetromino.isDrawingAllowed()) {
                        player1tetromino.draw("singleplayer");
                        if (player1tetromino.getDrawingTurns() == 0) {
                            System.out.println("FINISHED DRAWING");
                        }
                    }
                }
            }
        }));

        Timeline tickTime2 = new Timeline(new KeyFrame(Duration.seconds(0.2), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!player2tetromino.gameStateOver()) {
                    player2tetromino.tick();
                    if (player2tetromino.isDrawingAllowed()) {
                        player2tetromino.draw("singleplayer");
                        if (player2tetromino.getDrawingTurns() == 0) {
                            System.out.println("FINISHED DRAWING");
                        }
                    }
                }
            }
        }));


        peaLava.setOnShowing(event -> { //Do only once
            //draw('I');
        });
        peaLava.setOnCloseRequest((we) -> {
            System.out.println("Tetris stage closed!");
            Platform.exit();
            tickTime.stop();
        });
        tickTime.setCycleCount(Timeline.INDEFINITE);
        tickTime.play();

        tickTime2.setCycleCount(Timeline.INDEFINITE);
        tickTime2.play();


        Scene stseen = new Scene(hbox, resoWidth * 2 + 10, 350);
        peaLava.setScene(stseen);
        peaLava.show();

        Thread player1replaythread = new Thread(new ReplayRunner(player1tetromino, commandString1));
        Thread player2replaythread = new Thread(new ReplayRunner(player2tetromino, commandString2));
        player1replaythread.start();
        player2replaythread.start();

    }

    public static int getResoWidth() {
        return resoWidth;
    }

    public Tetromino getPlayer1tetromino() {
        return player1tetromino;
    }

    public Tetromino getPlayer2tetromino() {
        return player2tetromino;
    }

    public static int getResoHeight() {
        return resoHeight;
    }
}
