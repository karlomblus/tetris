package main.java.tetrispackage;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

public class Tetromino {
    private Map<Integer, Character> rectToStatus = new HashMap<>();
    public int tetrominoDrawingAllowance = 2;
    public boolean allowTetrominoDrawing = true;
    private Color activeTetrominoColor;
    Rectangle [][] ruudud;

    public Tetromino(Rectangle [][] ristkülik){
        ruudud = ristkülik;
        for (int i = 0; i < ruudud.length; i++) {
            for (int j = 0; j < ruudud[i].length; j++) {
                setRectStatusAt(i, j, 'B'); //B for background
            }
        }
    }
    public char getRectStatusAt(int i, int j){
        return rectToStatus.get(ruudud[i].length * i + j);
    }
    public void setRectStatusAt(int i, int j, char status){
        System.out.println("Inside setRectStatusAt");
        rectToStatus.put(ruudud[i].length * i + j, status);
        if (status == 'B'){
            ruudud[i][j].setFill(Color.BLUE);
        }
        else if (status == 'A'){
            ruudud[i][j].setFill(activeTetrominoColor);
        }
    }
    //Tetriminos I O T J L S Z
    void draw(char tetrominoType){
        if (tetrominoType == 'I') {
            int i = 0;
            activeTetrominoColor = Color.CYAN;
            for (int j = 0; j < 4; j++) {
                setRectStatusAt(i, j, 'A');
            }
            tetrominoDrawingAllowance -= 1;
        }
        else if (tetrominoType == 'O'){
            activeTetrominoColor = Color.YELLOW;
            setRectStatusAt(0, 0, 'A');
            setRectStatusAt(0, 1, 'A');
        }
        else if (tetrominoType == 'Z'){
            if (tetrominoDrawingAllowance == 1) {
                activeTetrominoColor = Color.RED;
                setRectStatusAt(0, 1, 'A');
                setRectStatusAt(0, 2, 'A');
            }
            else{
                setRectStatusAt(0, 0, 'A');
                setRectStatusAt(0, 1, 'A');
            }
        }
        else if (tetrominoType == 'S'){
            activeTetrominoColor = Color.LIME;
            if (tetrominoDrawingAllowance == 1) {
                setRectStatusAt(0, 0, 'A');
                setRectStatusAt(0, 1, 'A');
            }
            else{
                setRectStatusAt(0, 1, 'A');
                setRectStatusAt(0, 2, 'A');
            }
        }
    }
    public void setAllowTetrominoDrawing(boolean allowTetrominoDrawing) {
        this.allowTetrominoDrawing = allowTetrominoDrawing;
    }

    public boolean isDrawingAllowed() {
        return allowTetrominoDrawing;
    }
}
