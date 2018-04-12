package tetrispackage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

public class TetrisGraafikaMultiplayer {
    private int numberOfPlayers = 2;
    private final int resoWidth = 150 * 2;
    private final int resoHeight = 330;
    final int ruuduSuurus = 15;
    final int mitukuubikutLaiuses = resoWidth / ruuduSuurus / numberOfPlayers;
    final int mitukuubikutPikkuses = resoHeight / ruuduSuurus;
    private Rectangle [][] ristkülik = new Rectangle[mitukuubikutPikkuses][mitukuubikutLaiuses];
    private Rectangle [][] ristkülik2 = new Rectangle[mitukuubikutPikkuses][mitukuubikutLaiuses];

    Tetromino tetromino;
    Tetromino tetromino2;
    private Map<KeyCode, Boolean> currentActiveKeys = new HashMap<>();

    public void start(Stage peaLava) {
        HBox hbox = new HBox(1);
        Group localTetrisArea = new Group(); // luuakse localTetrisArea
        Group opponentTetrisArea = new Group();

        for (int i = 0; i < mitukuubikutPikkuses; i++) {
            for (int j = 0; j < mitukuubikutLaiuses; j++) {
                ristkülik[i][j] = new Rectangle(j * ruuduSuurus, i * ruuduSuurus, ruuduSuurus, ruuduSuurus);
                ristkülik[i][j].setStroke(Color.LIGHTGRAY);
                localTetrisArea.getChildren().add(ristkülik[i][j]);  // ristkülik lisatakse juure alluvaks
            }
        }
        for (int i = 0; i < mitukuubikutPikkuses; i++) {
            for (int j = 0; j < mitukuubikutLaiuses; j++) {
                ristkülik2[i][j] = new Rectangle(j * ruuduSuurus, i * ruuduSuurus, ruuduSuurus, ruuduSuurus);
                ristkülik2[i][j].setStroke(Color.LIGHTGRAY);
                opponentTetrisArea.getChildren().add(ristkülik2[i][j]);  // ristkülik lisatakse juure alluvaks
            }
        }
        tetromino = new Tetromino(ristkülik);
        tetromino2 = new Tetromino(ristkülik2);
        Timeline tickTime = createTimeline(Duration.seconds(0.2), tetromino);
        Timeline tickTime2 = createTimeline(Duration.seconds(0.2), tetromino2);
        tickTime2.setCycleCount(Timeline.INDEFINITE);
        tickTime2.play();
        tickTime.setCycleCount(Timeline.INDEFINITE);
        tickTime.play();

        hbox.getChildren().add(localTetrisArea);
        hbox.getChildren().add(opponentTetrisArea);

        peaLava.setOnCloseRequest((we) -> {
            System.out.println("Tetris stage closed!");
            Platform.exit();
            //PlatformImpl.tkExit()
            tickTime.stop();
        });
        peaLava.setOnShowing(event -> { //Do only once
            //draw('I');
        });
        Scene tetrisStseen = new Scene(hbox, resoWidth, resoHeight, Color.SNOW);  // luuakse stseen
        tetrisStseen.setOnKeyPressed(event -> {
            currentActiveKeys.put(event.getCode(), true);
            if (!tetromino.isDrawingAllowed() && !tetromino.gameStateOver()) {
                if (currentActiveKeys.containsKey(KeyCode.RIGHT) && currentActiveKeys.get(KeyCode.RIGHT)) {
                    tetromino.moveRight();
                }
                if (currentActiveKeys.containsKey(KeyCode.LEFT) && currentActiveKeys.get(KeyCode.LEFT)) {
                    tetromino.moveLeft();
                }
                if (currentActiveKeys.containsKey(KeyCode.UP) && currentActiveKeys.get(KeyCode.UP)) {
                    tetromino.rotateLeft();
                }
                if (currentActiveKeys.containsKey(KeyCode.SPACE) && currentActiveKeys.get(KeyCode.SPACE)) {
                    boolean keepticking = true;
                    do {
                        keepticking = tetromino.tick();
                    }
                    while (keepticking);
                }
                /*if (currentActiveKeys.containsKey(KeyCode.DOWN) && currentActiveKeys.get(KeyCode.DOWN)) {
                    tetromino.tick();
                }*/
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
    Timeline createTimeline(Duration durationSeconds, Tetromino tetromino){
        char[] possibleTetrominos = {'I', 'O', 'Z', 'S', 'T', 'J', 'L'};
        Random rand = new Random();
        Timeline tickTime = new Timeline(new KeyFrame(durationSeconds, new EventHandler<ActionEvent>() {
            char randomTetromino = 'S';

            @Override
            public void handle(ActionEvent event) {
                if (!tetromino.gameStateOver()) {
                    tetromino.tick();
                    tetromino.isRowFilled();
                    if (tetromino.isDrawingAllowed()) {
                        if (tetromino.getDrawingTurns() == 2) {
                            randomTetromino = possibleTetrominos[rand.nextInt(possibleTetrominos.length)];
                        }
                        tetromino.draw(randomTetromino);
                    }
                }
            }
        }));
        return tickTime;
    }
}
