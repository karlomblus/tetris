package tetrispackage;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tetromino {
    private Map<Integer, Character> rectToStatus = new HashMap<>();
    private int drawingTurns = 2;
    private boolean allowTetrominoDrawing = true;
    private Color activeTetrominoColor;
    Rectangle [][] ruudud;
    private boolean allActiveToPassive = false;

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
        rectToStatus.put(ruudud[i].length * i + j, status);
        if (status == 'B'){
            ruudud[i][j].setFill(Color.BLUE);
        }
        else if (status == 'A'){
            ruudud[i][j].setFill(activeTetrominoColor);
        }
    }
    void moveRight(){
        for (int i = ruudud.length - 1; i >= 0; i--) {
            for (int j = ruudud[i].length - 1; j >= 0; j--) {
                if (getRectStatusAt(i, j) == 'A') {
                    if (j + 1 < ruudud[i].length){
                        setRectStatusAt(i, j + 1, 'A');
                    }
                    setRectStatusAt(i, j, 'B');
                }
            }
        }
    }
    void moveLeft(){
        for (int i = ruudud.length - 1; i >= 0; i--) {
            for (int j = 0; j < ruudud[i].length; j++) {
                if (getRectStatusAt(i, j) == 'A') {
                    if (j - 1 >= 0){
                        setRectStatusAt(i, j - 1, 'A');
                    }
                    setRectStatusAt(i, j, 'B');
                }
            }
        }
    }
    void tick() {
        List<Integer> activeRectCoordI = new ArrayList<>();
        List<Integer> activeRectCoordJ = new ArrayList<>();
        for (int i = ruudud.length - 1; i >= 0; i--) {  //Find all active blocks and check if all active blocks need to be set to passive
            for (int j = ruudud[i].length - 1; j >= 0; j--) {
                if (getRectStatusAt(i, j) == 'A') {
                    activeRectCoordI.add(i); //remember where the active blocks are
                    activeRectCoordJ.add(j);
                    if (i + 1 == ruudud.length || getRectStatusAt(i+1, j) == 'P') {
                        allActiveToPassive = true;
                    }

                }
            }
        }
        if (allActiveToPassive){
            System.out.println("All active to passive!");
            for (int k = 0; k < ruudud.length; k++) {
                for (int l = 0; l < ruudud[k].length; l++) {
                    if (getRectStatusAt(k, l) == 'A') {
                        setRectStatusAt(k, l, 'P');
                    }
                }
            }
            drawingTurns = 2;
            allowTetrominoDrawing = true;
            allActiveToPassive = false;
        }
        else if (!allActiveToPassive){
            for (int i = 0; i < activeRectCoordI.size(); i++) {
                setRectStatusAt(activeRectCoordI.get(i) + 1, activeRectCoordJ.get(i), 'A');
                setRectStatusAt(activeRectCoordI.get(i), activeRectCoordJ.get(i), 'B');
            }



        }
    }
    //Tetriminos I O T J L S Z
    void draw(char tetrominoType){
        drawingTurns -= 1;
        if (tetrominoType == 'I') {
            int i = 0;
            activeTetrominoColor = Color.CYAN;
            for (int j = 0; j < 4; j++) {
                setRectStatusAt(i, j, 'A');
            }
            drawingTurns -= 1;
        }
        else if (tetrominoType == 'O'){
            activeTetrominoColor = Color.YELLOW;
            setRectStatusAt(0, 0, 'A');
            setRectStatusAt(0, 1, 'A');
        }
        else if (tetrominoType == 'Z'){
            if (drawingTurns == 1) {
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
            if (drawingTurns == 1) {
                setRectStatusAt(0, 0, 'A');
                setRectStatusAt(0, 1, 'A');
            }
            else{
                setRectStatusAt(0, 1, 'A');
                setRectStatusAt(0, 2, 'A');
            }
        }
        if (drawingTurns == 0) {
            System.out.println("TetriminoDrawing false!");
            setDrawingPermission(false);
        }
    }
    public void setDrawingPermission(boolean allowTetrominoDrawing) {
        this.allowTetrominoDrawing = allowTetrominoDrawing;
    }

    public boolean isDrawingAllowed() {
        return allowTetrominoDrawing;
    }

    public int getDrawingTurns() {
        return drawingTurns;
    }

    public void setDrawingTurns(int drawingTurns) {
        this.drawingTurns = drawingTurns;
    }
}
