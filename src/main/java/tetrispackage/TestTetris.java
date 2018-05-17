package tetrispackage;

import javafx.application.Application;
import javafx.stage.Stage;

public class TestTetris extends Application {
    public void start(Stage stage) {
        Stage lava = new Stage();
        TetrisGraafika tetris = new TetrisGraafika();
        tetris.start(lava);
        tetris.begin();
    }

    public static void main(String[] args) {
        launch();
    }
}
