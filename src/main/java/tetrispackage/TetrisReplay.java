package tetrispackage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
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
    private Tetromino tetromino;
    private Map<KeyCode, Boolean> currentActiveKeys = new HashMap<>();

    public void start(Stage peaLava,String commandString) {
        HBox hbox = new HBox(10);
        Group tetrisArea = new Group();
        TetrisRectangle tetrisRect = new TetrisRectangle();
        tetrisRect.fill(tetrisArea);
        tetromino = new Tetromino(tetrisRect.getRistkülik());
        ScoreHandler scoreHandler = new ScoreHandler(tetromino);
        hbox.getChildren().add(tetrisArea);
        hbox.getChildren().add(scoreHandler.getScoreArea());
        Timeline tickTime = new Timeline(new KeyFrame(Duration.seconds(0.2), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!tetromino.gameStateOver()) {
                    tetromino.tick();
                    if (tetromino.isDrawingAllowed()) {
                        tetromino.draw("singleplayer");
                        if (tetromino.getDrawingTurns() == 0) {
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

        Scene tetrisStseen = new Scene(hbox, resoWidth + 140, resoHeight, Color.SNOW);  // luuakse stseen
        /*
        tetrisStseen.setOnKeyPressed(event -> {
            currentActiveKeys.put(event.getCode(), true);
            if (!tetromino.isDrawingAllowed() && !tetromino.gameStateOver()) {
                if (currentActiveKeys.containsKey(KeyCode.RIGHT) && currentActiveKeys.get(KeyCode.RIGHT)) {
                    tetromino.moveRight();
                }
                if (currentActiveKeys.containsKey(KeyCode.LEFT) && currentActiveKeys.get(KeyCode.LEFT)) {
                    tetromino.moveLeft();
                }
                if (currentActiveKeys.containsKey(KeyCode.DOWN) && currentActiveKeys.get(KeyCode.DOWN)) {
                    tetromino.drop();
                }
                if (currentActiveKeys.containsKey(KeyCode.UP) && currentActiveKeys.get(KeyCode.UP)) {
                    tetromino.rotateLeft();
                }
            }
        });
        tetrisStseen.setOnKeyReleased(event ->
                currentActiveKeys.put(event.getCode(), false)
        );
        */
        peaLava.setTitle("Tetris");  // lava tiitelribale pannakse tekst

        peaLava.setScene(tetrisStseen);  // lavale lisatakse stseen
        peaLava.show();  // lava tehakse nähtavaks

        Thread replaythread  = new Thread(new ReplayRunner(this,commandString));
        replaythread.start();
    }

    public static int getResoWidth() {
        return resoWidth;
    }

    public Tetromino getTetromino() {
        return tetromino;
    }

    public static int getResoHeight() {
        return resoHeight;
    }
}