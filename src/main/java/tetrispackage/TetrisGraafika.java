package main.java.tetrispackage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;


public class TetrisGraafika extends Application implements Runnable {
    private final int resoWidth = 450;
    private final int resoHeight = 600;
    final int ruuduSuurus = 15;
    final int mitukuubikutLaiuses = resoWidth / ruuduSuurus;
    final int mitukuubikutPikkuses = resoHeight / ruuduSuurus;
    private Rectangle ristkülik[][] = new Rectangle[mitukuubikutPikkuses][mitukuubikutLaiuses];
    Tetromino tetromino;

    @Override
    public void run() {
        launch();
    }
    private Map<KeyCode, Boolean> currentActiveKeys = new HashMap<>();

    @Override
    public void start(Stage peaLava) throws Exception {
        Group juur = new Group(); // luuakse juur
        for (int i = 0; i < mitukuubikutPikkuses; i++) {
            for (int j = 0; j < mitukuubikutLaiuses; j++) {
                ristkülik[i][j] = new Rectangle(j * ruuduSuurus, i * ruuduSuurus, ruuduSuurus, ruuduSuurus);
                ristkülik[i][j].setStroke(Color.RED);
                juur.getChildren().add(ristkülik[i][j]);  // ristkülik lisatakse juure alluvaks
            }
        }

        tetromino = new Tetromino(ristkülik);
        Timeline tickTime = new Timeline(new KeyFrame(Duration.seconds(0.1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tetromino.tick();
                if (tetromino.isDrawingAllowed()) {
                    tetromino.draw('S');
                }
            }
        }));

        peaLava.setOnShowing(event -> { //Do only once
            //draw('I');
        });

        tickTime.setCycleCount(Timeline.INDEFINITE);
        tickTime.play();
        Scene stseen1 = new Scene(juur, resoWidth, resoHeight, Color.SNOW);  // luuakse stseen
        stseen1.setOnKeyPressed(event -> {
            currentActiveKeys.put(event.getCode(), true);
            if (currentActiveKeys.containsKey(KeyCode.RIGHT) && currentActiveKeys.get(KeyCode.RIGHT)) {
                tetromino.moveRight();
            }
            if (currentActiveKeys.containsKey(KeyCode.LEFT) && currentActiveKeys.get(KeyCode.LEFT)) {
                tetromino.moveLeft();
            }

        });
        stseen1.setOnKeyReleased(event ->
                currentActiveKeys.put(event.getCode(), false)
        );

        peaLava.setTitle("Tetris");  // lava tiitelribale pannakse tekst
        peaLava.setScene(stseen1);  // lavale lisatakse stseen
        peaLava.show();  // lava tehakse nähtavaks

    }

    public static void main(String[] args) {
        //panin tetrisgraafika lõie peal töötama
        //launch(args);
        Thread lõim = new Thread(new TetrisGraafika());
        lõim.start();
    }
}