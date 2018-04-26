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

    private final int resoWidth = 150;
    private final int resoHeight = 330;
    private final int ruuduSuurus = 15;
    private final int mitukuubikutLaiuses = resoWidth / ruuduSuurus;
    private final int mitukuubikutPikkuses = resoHeight / ruuduSuurus;
    private Rectangle ristkülik[][] = new Rectangle[mitukuubikutPikkuses][mitukuubikutLaiuses];
    private Tetromino tetromino;
    private Map<KeyCode, Boolean> currentActiveKeys = new HashMap<>();

    public void start(Stage peaLava) {
        Group juur = new Group(); // luuakse juur
        for (int i = 0; i < mitukuubikutPikkuses; i++) {
            for (int j = 0; j < mitukuubikutLaiuses; j++) {
                ristkülik[i][j] = new Rectangle(j * ruuduSuurus, i * ruuduSuurus, ruuduSuurus, ruuduSuurus);
                ristkülik[i][j].setStroke(Color.LIGHTGRAY);
                juur.getChildren().add(ristkülik[i][j]);  // ristkülik lisatakse juure alluvaks
            }
        }
        tetromino = new Tetromino(ristkülik);
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
                if (currentActiveKeys.containsKey(KeyCode.SPACE) && currentActiveKeys.get(KeyCode.SPACE)) {
                    boolean keepticking = true;
                    do {
                        keepticking = tetromino.tick();
                    }
                    while (keepticking);
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
}
