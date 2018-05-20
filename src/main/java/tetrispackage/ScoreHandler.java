package tetrispackage;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ScoreHandler {
    private VBox scoreArea = new VBox();
    private Text myScoreText = new Text("");
    private Text opponentScoreText = new Text("");

    public ScoreHandler(Tetromino myTetro) {
        addMyScoreUpdator(myTetro);
    }

    public ScoreHandler(Tetromino myTetro, Tetromino opponentTetro) {
        this(myTetro);
        addOpponentScoreUpdator(opponentTetro);
    }

    public VBox getScoreArea() {
        return scoreArea;
    }

    private void addMyScoreUpdator(Tetromino tetro) {
        scoreArea.getChildren().add(myScoreText);
        myScoreText.textProperty().setValue("My score: " + tetro.getRowsCleared().getValue().toString());
        tetro.getRowsCleared().addListener((o, oldVal, newVal) -> {
            myScoreText.textProperty().setValue("My score: " + newVal.toString());
        });
    }

    private void addOpponentScoreUpdator(Tetromino tetro) {
        scoreArea.getChildren().add(opponentScoreText);
        opponentScoreText.textProperty().setValue("Opponent score: " + tetro.getRowsCleared().getValue().toString());
        tetro.getRowsCleared().addListener((o, oldVal, newVal) -> {
            opponentScoreText.textProperty().setValue("Opponent score: " + newVal.toString());
        });
    }
}
