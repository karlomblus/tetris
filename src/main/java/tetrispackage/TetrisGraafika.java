package tetrispackage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class TetrisGraafika {

    private static final int resoWidth = 150;
    private static final int resoHeight = 330;
    private Tetromino tetromino;
    private Map<KeyCode, Boolean> currentActiveKeys = new HashMap<>();

    public void start(Stage peaLava) {
        Group juur = new Group(); // luuakse juur
        TetrisRectangle tetrisRect = new TetrisRectangle();
        tetrisRect.fill(juur);
        tetromino = new Tetromino(tetrisRect.getRistkülik());
        Timeline tickTime = new Timeline(new KeyFrame(Duration.seconds(0.2), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!tetromino.gameStateOver()) {
                    tetromino.tick();
                    if (tetromino.isDrawingAllowed()) {
                        tetromino.draw("singleplayer");
                        if (tetromino.getDrawingTurns() == 0){
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

        Scene tetrisStseen = new Scene(juur, resoWidth, resoHeight, Color.SNOW);  // luuakse stseen
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

        peaLava.setTitle("Tetris");  // lava tiitelribale pannakse tekst

        peaLava.setScene(tetrisStseen);  // lavale lisatakse stseen
        peaLava.show();  // lava tehakse nähtavaks
    }

    public void begin() {
        //launch();
    }

    public static int getResoWidth() {
        return resoWidth;
    }

    public static int getResoHeight() {
        return resoHeight;
    }
}
