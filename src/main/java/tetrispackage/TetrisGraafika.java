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
import java.util.HashMap;
import java.util.Map;


public class TetrisGraafika extends Application {
    private final int resoWidth = 450;
    private final int resoHeight = 600;
    final int ruuduSuurus = 15;
    final int mitukuubikutLaiuses = resoWidth / ruuduSuurus;
    final int mitukuubikutPikkuses = resoHeight / ruuduSuurus;
    Rectangle ristkülik[][] = new Rectangle[mitukuubikutPikkuses][mitukuubikutLaiuses];
    private Map<Integer, Color> rectToColor = new HashMap<>();
    private int tetrominoDrawingAllowance = 2;
    private boolean allowTetrominoDrawing = true;

    private Color getRectColorAt(int i, int j){
        return rectToColor.get(mitukuubikutLaiuses * i + j);
    }
    private Color setRectColorAt(int i, int j, Color color){
        ristkülik[i][j].setFill(color);
        return rectToColor.put(mitukuubikutLaiuses * i + j, color);
    }

    @Override
    public void start(Stage peaLava) throws Exception {
        Group juur = new Group(); // luuakse juur
        for (int i = 0; i < mitukuubikutPikkuses; i++) {
            for (int j = 0; j < mitukuubikutLaiuses; j++) {
                ristkülik[i][j] = new Rectangle(j * ruuduSuurus, i * ruuduSuurus, ruuduSuurus, ruuduSuurus);
                setRectColorAt(i, j, Color.BLUE);
                ristkülik[i][j].setStroke(Color.RED);
                juur.getChildren().add(ristkülik[i][j]);  // ristkülik lisatakse juure alluvaks
            }
        }
        Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                tick();
                if (allowTetrominoDrawing){
                    drawTetromino('Z');
                }
            }
        }));
        peaLava.setOnShowing(event -> { //Do only once
            //drawTetromino('I');
        });
        fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
        fiveSecondsWonder.play();
        Scene stseen1 = new Scene(juur, resoWidth, resoHeight, Color.SNOW);  // luuakse stseen
        peaLava.setTitle("Tetris");  // lava tiitelribale pannakse tekst
        peaLava.setScene(stseen1);  // lavale lisatakse stseen
        peaLava.show();  // lava tehakse nähtavaks

    }
    //Tetriminos I O T J L S Z
    void drawTetromino(char tetrominoType){
        if (tetrominoType == 'I') {
            int i = 0;
            int j = 0;
            setRectColorAt(i, j, Color.RED);
            setRectColorAt(i, j+1, Color.RED);
            setRectColorAt(i, j+2, Color.RED);
            setRectColorAt(i, j+3, Color.RED);
            tetrominoDrawingAllowance -= 1;
        }
        else if (tetrominoType == 'O'){
            setRectColorAt(0, 0, Color.RED);
            setRectColorAt(0, 1, Color.RED);
        }
        else if (tetrominoType == 'Z'){
            if (tetrominoDrawingAllowance == 1) {
                setRectColorAt(0, 1, Color.RED);
                setRectColorAt(0, 2, Color.RED);
            }
            else{
                setRectColorAt(0, 0, Color.RED);
                setRectColorAt(0, 1, Color.RED);
            }
        }
        else if (tetrominoType == 'S'){
            if (tetrominoDrawingAllowance == 1) {
                setRectColorAt(0, 0, Color.RED);
                setRectColorAt(0, 1, Color.RED);
            }
            else{
                setRectColorAt(0, 1, Color.RED);
                setRectColorAt(0, 2, Color.RED);
            }
        }
    }
    void tick() {
        System.out.println("Ticked");
        if (tetrominoDrawingAllowance == 0){
            System.out.println("TetriminoDrawing false!");
            allowTetrominoDrawing = false;
        }
        else if (tetrominoDrawingAllowance == -4){//Esialgu lubab uut iga nelja sekundi tagamt
            allowTetrominoDrawing = true;
            tetrominoDrawingAllowance = 2;
        }
        tetrominoDrawingAllowance -= 1;
        for (int i = mitukuubikutPikkuses - 1; i >= 0; i--) {
            for (int j = mitukuubikutLaiuses - 1; j >= 0; j--) {
                if (getRectColorAt(i, j).equals(Color.RED)) {
                    if (i + 1 < mitukuubikutPikkuses){
                        setRectColorAt(i+1, j, Color.RED);
                    }
                    setRectColorAt(i, j, Color.BLUE);
                }
            }
        }

    }
    public static void main(String[] args) {
        launch(args);
    }
}
