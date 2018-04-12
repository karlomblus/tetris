package tetrispackage;

import javafx.application.Application;
import javafx.stage.Stage;

public class TetrisGraafikaMultiplayerTest extends Application {
    public void start(Stage stage){
        Stage lava = new Stage();
        TetrisGraafikaMultiplayer tetris = new TetrisGraafikaMultiplayer();
        tetris.start(lava);
        tetris.begin();
    }
    public static void main(String[] args) {
        launch();
    }
}

