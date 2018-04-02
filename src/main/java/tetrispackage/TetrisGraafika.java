package tetrispackage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;


public class TetrisGraafika extends Application {
    private final int resoWidth = 450;
    private final int resoHeight = 600;
    final int ruuduSuurus = 15;
    final int mitukuubikutLaiuses = resoWidth / ruuduSuurus;
    final int mitukuubikutPikkuses = resoHeight / ruuduSuurus;
    private Rectangle ristkülik[][] = new Rectangle[mitukuubikutPikkuses][mitukuubikutLaiuses];
    Tetromino tetromino;

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
        tetromino  = new Tetromino(ristkülik);
        Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                tick();
                if (tetromino.isDrawingAllowed()){
                    tetromino.draw('S');
                }
            }
        }));
        peaLava.setOnShowing(event -> { //Do only once
            //draw('I');
        });
        fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
        fiveSecondsWonder.play();
        Scene stseen1 = new Scene(juur, resoWidth, resoHeight, Color.SNOW);  // luuakse stseen
        peaLava.setTitle("Tetris");  // lava tiitelribale pannakse tekst
        peaLava.setScene(stseen1);  // lavale lisatakse stseen
        peaLava.show();  // lava tehakse nähtavaks

    }

    void tick() {
        System.out.println("Ticked");
        if (tetromino.tetrominoDrawingAllowance == 0){
            System.out.println("TetriminoDrawing false!");
            tetromino.allowTetrominoDrawing = false;
        }
        else if (tetromino.tetrominoDrawingAllowance == -4){//Esialgu lubab uut iga nelja sekundi tagamt
            tetromino.allowTetrominoDrawing = true;
            tetromino.tetrominoDrawingAllowance = 2;
        }
        tetromino.tetrominoDrawingAllowance -= 1;
        for (int i = mitukuubikutPikkuses - 1; i >= 0; i--) {
            for (int j = mitukuubikutLaiuses - 1; j >= 0; j--) {
                if (tetromino.getRectStatusAt(i, j) == 'A') {
                    System.out.println("Found active");
                    if (i + 1 < mitukuubikutPikkuses){
                        tetromino.setRectStatusAt(i + 1, j, 'A');
                    }
                    tetromino.setRectStatusAt(i, j, 'B');
                }
            }
        }

    }
    public static void main(String[] args) {
        launch(args);
    }
}
