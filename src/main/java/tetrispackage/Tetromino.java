package tetrispackage;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.*;

public class Tetromino {
    private Map<Integer, Character> rectToStatus = new HashMap<>();
    private int drawingTurns = 2;
    private boolean allowTetrominoDrawing = true;
    private Color activeTetrominoColor;
    private Rectangle[][] ruudud;
    private boolean allActiveToPassive = false;
    private  boolean allFallingToPassive = false;
    private char tetrominoType;
    private int tetrominoRotationTracker = 0;
    private boolean gameOver = false;
    private int [][] activeTetrominoMatrix;
    private char randomTetrominoMP = 'Z';
    private char randomTetrominoSP = 'Z';
    private boolean newRandomTetroReceived = true;

    public Tetromino(Rectangle[][] ristkülik) {
        ruudud = ristkülik;
        for (int i = 0; i < ruudud.length; i++) {
            for (int j = 0; j < ruudud[i].length; j++) {
                setRectStatusAt(i, j, 'B'); //B for background
            }
        }
    }

    public char getRectStatusAt(int i, int j) {
        return rectToStatus.get(ruudud[i].length * i + j);
    }

    public void setRectStatusAt(int i, int j, char status) {
        rectToStatus.put(ruudud[i].length * i + j, status);
        if (status == 'B') {
            ruudud[i][j].setFill(Color.BLANCHEDALMOND);
            ruudud[i][j].setStroke(Color.WHITE);
        } else if (status == 'A') {
            ruudud[i][j].setFill(activeTetrominoColor);
            ruudud[i][j].setStroke(Color.BLACK);
        }
    }

    boolean moveRight() {
        List<Integer> activeRectCoordI = new ArrayList<>();
        List<Integer> activeRectCoordJ = new ArrayList<>();
        boolean movingRightPossible = true;
        for (int i = ruudud.length - 1; i >= 0; i--) {
            for (int j = ruudud[i].length - 1; j >= 0; j--) {
                if (getRectStatusAt(i, j) == 'A') {
                    activeRectCoordI.add(i); //remember where the active blocks are
                    activeRectCoordJ.add(j);
                    if (j + 1 >= ruudud[i].length || getRectStatusAt(i, j + 1) == 'P') {
                        movingRightPossible = false;
                    }
                }
            }
        }
        if (movingRightPossible) {
            for (int i = 0; i < activeRectCoordI.size(); i++) {
                setRectStatusAt(activeRectCoordI.get(i), activeRectCoordJ.get(i) + 1, 'A');
                setRectStatusAt(activeRectCoordI.get(i), activeRectCoordJ.get(i), 'B');
            }
            return true;
        }
        return false;
    }

    boolean moveLeft() {
        List<Integer> activeRectCoordI = new ArrayList<>();
        List<Integer> activeRectCoordJ = new ArrayList<>();
        boolean movingLeftPossible = true;
        for (int i = ruudud.length - 1; i >= 0; i--) {
            for (int j = 0; j < ruudud[i].length; j++) {
                if (getRectStatusAt(i, j) == 'A') {
                    activeRectCoordI.add(i); //remember where the active blocks are
                    activeRectCoordJ.add(j);
                    if (j - 1 < 0 || getRectStatusAt(i, j - 1) == 'P') {
                        movingLeftPossible = false;
                    }
                }
            }
        }
        if (movingLeftPossible) {
            for (int i = 0; i < activeRectCoordI.size(); i++) {
                setRectStatusAt(activeRectCoordI.get(i), activeRectCoordJ.get(i) - 1, 'A');
                setRectStatusAt(activeRectCoordI.get(i), activeRectCoordJ.get(i), 'B');
            }
            return true;
        }
        return false;
    }
    boolean isRowFilled(){
        boolean isFilled = true;
        int filledRowNumber = 0;
        int foundPassiveBlocksinRow = 0;
        for (int i = 0; i < ruudud.length; i++) {
            for (int j = 0; j < ruudud[0].length; j++) {
                if (getRectStatusAt(i, j) == 'P'){
                    foundPassiveBlocksinRow += 1;
                }
            }
            if (foundPassiveBlocksinRow == ruudud[0].length){
                isFilled = true;
                filledRowNumber = i;
                break;
            }
            else{
                foundPassiveBlocksinRow = 0;
                isFilled = false;
            }
        }
        if (isFilled == true){
           // System.out.println("Filled row found!AAAAAAAAAAAAAAAAAAAAAA");
            //System.out.println("Filled row: " + filledRowNumber);
            for (int i = 0; i < ruudud[0].length; i++) {
                setRectStatusAt(filledRowNumber, i, 'B');
            }
            setAllAboveToFalling(filledRowNumber);
        }
        return isFilled;
    }
    void setAllAboveToFalling (int deletedRow){
        for (int i = 0; i < deletedRow; i++) {
            for (int j = 0; j < ruudud[0].length; j++) {
                if (getRectStatusAt(i, j) == 'P')
                setRectStatusAt(i ,j, 'F');
            }
        }
    }


    private void transponeeri(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = i; j < matrix[i].length; j++) {
                int ajutine = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = ajutine;
            }
        }
        System.out.println("Transponeeritud:");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j]);
            }
            System.out.println();
        }
    }

    private void vahetaRead(int[][] matrix) {
        for (int i = 0; i < matrix.length / 2; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                int ajutine = matrix[i][j];
                matrix[i][j] = matrix[(matrix.length - 1) - i][j];
                matrix[(matrix.length - 1) - i][j] = ajutine;
            }
        }
        System.out.println("Read vahetatud:");
        for (int k = 0; k < matrix.length; k++) {
            for (int l = 0; l < matrix[k].length; l++) {
                System.out.print(matrix[k][l]);
            }
            System.out.println();
        }
    }

    boolean tick() { //returns false if Active blocks reached bottom
        List<Integer> activeRectCoordI = new ArrayList<>();
        List<Integer> activeRectCoordJ = new ArrayList<>();
        List<Integer> fallingRectCoordI = new ArrayList<>();
        List<Integer> fallingRectCoordJ = new ArrayList<>();
        boolean keepTicking = true;
        for (int i = ruudud.length - 1; i >= 0; i--) {  //Find all active blocks and check if all active blocks need to be set to passive
            for (int j = ruudud[i].length - 1; j >= 0; j--) {
                if (getRectStatusAt(i, j) == 'A') {
                    activeRectCoordI.add(i); //remember where the active blocks are
                    activeRectCoordJ.add(j);
                    if (i + 1 == ruudud.length || getRectStatusAt(i + 1, j) == 'P') {
                        allActiveToPassive = true;
                    }
                }
                if (getRectStatusAt(i, j) == 'F') {
                    fallingRectCoordI.add(i); //remember where the falling blocks are
                    fallingRectCoordJ.add(j);
                    if (i + 1 == ruudud.length || getRectStatusAt(i + 1, j) == 'P') {
                        allFallingToPassive = true;
                    }
                }
            }
        }
        if (allActiveToPassive) {
           // System.out.println("All active to passive!");
            for (int k = 0; k < ruudud.length; k++) {
                for (int l = 0; l < ruudud[k].length; l++) {
                    if (getRectStatusAt(k, l) == 'A') {
                        setRectStatusAt(k, l, 'P');
                    }
                }
            }
            for (int i = 0; i < ruudud[0].length; i++) {
                if (activeRectCoordI.get(0) == 0){
                    gameOver = true;
                }
            }
            if (gameOver){
                System.out.println("Game over!");
                return false;
            }
            drawingTurns = 2;
            setDrawingPermission(true);

            allActiveToPassive = false;
            keepTicking = false;
        } else if (!allActiveToPassive) {
            for (int i = 0; i < activeRectCoordI.size(); i++) {
                setRectStatusAt(activeRectCoordI.get(i) + 1, activeRectCoordJ.get(i), 'A');
                setRectStatusAt(activeRectCoordI.get(i), activeRectCoordJ.get(i), 'B');
            }
        }
        if (allFallingToPassive){
           // System.out.println("All falling to passive!");
            for (int k = 0; k < ruudud.length; k++) {
                for (int l = 0; l < ruudud[k].length; l++) {
                    if (getRectStatusAt(k, l) == 'F') {
                        setRectStatusAt(k, l, 'P');
                    }
                }
            }
            allFallingToPassive = false;
        }
        else if (!allFallingToPassive) {
            for (int i = 0; i < fallingRectCoordI.size(); i++) {
                setRectStatusAt(fallingRectCoordI.get(i) + 1, fallingRectCoordJ.get(i), 'F');
                ruudud[fallingRectCoordI.get(i)+1][fallingRectCoordJ.get(i)].setFill
                        (ruudud[fallingRectCoordI.get(i)][fallingRectCoordJ.get(i)].getFill());
                ruudud[fallingRectCoordI.get(i)+1][fallingRectCoordJ.get(i)].setStroke
                        (ruudud[fallingRectCoordI.get(i)][fallingRectCoordJ.get(i)].getStroke());
                setRectStatusAt(fallingRectCoordI.get(i), fallingRectCoordJ.get(i), 'B');
            }
        }
        return keepTicking;
    }

    //Tetriminos I O T J L S Z
    void draw(String gamemode) {
        if (gamemode.equals("singeplayer")) {
            this.tetrominoType = randomTetrominoSP;
        }
        else if (gamemode.equals("multiplayer")){
            this.tetrominoType = randomTetrominoMP;
        }

        drawingTurns -= 1;
        tetrominoRotationTracker = 0;
        int j = (ruudud[0].length / 2) - 2;
        if (tetrominoType == 'I') {
            int i = 0;
            activeTetrominoColor = Color.CYAN;
            for (int l = 0; l < 4; l++) {
                setRectStatusAt(i, j + l, 'A');
            }
            drawingTurns -= 1;
        } else if (tetrominoType == 'O') {
            activeTetrominoColor = Color.YELLOW;
            setRectStatusAt(0, j, 'A');
            setRectStatusAt(0, j + 1, 'A');
        } else if (tetrominoType == 'Z') {
            if (drawingTurns == 1) {
                activeTetrominoColor = Color.RED;
                setRectStatusAt(0, j + 1, 'A');
                setRectStatusAt(0, j + 2, 'A');
            } else {
                setRectStatusAt(0, j, 'A');
                setRectStatusAt(0, j + 1, 'A');
            }
        } else if (tetrominoType == 'S') {
            activeTetrominoColor = Color.LIME;
            if (drawingTurns == 1) {
                setRectStatusAt(0, j, 'A');
                setRectStatusAt(0, j + 1, 'A');
            } else {
                setRectStatusAt(0, j + 1, 'A');
                setRectStatusAt(0, j + 2, 'A');
            }
        } else if (tetrominoType == 'T') {
            activeTetrominoColor = Color.PURPLE;
            if (drawingTurns == 1) {
                setRectStatusAt(0, j, 'A');
                setRectStatusAt(0, j + 1, 'A');
                setRectStatusAt(0, j + 2, 'A');

            } else {
                setRectStatusAt(0, j + 1, 'A');
            }
        }
        else if (tetrominoType == 'L') {
            activeTetrominoColor = Color.ORANGE;
            if (drawingTurns == 0) {
                setRectStatusAt(0, j + 2, 'A');

            } else {
                setRectStatusAt(0, j, 'A');
                setRectStatusAt(0, j + 1, 'A');
                setRectStatusAt(0, j + 2, 'A');
            }
        }
        else if (tetrominoType == 'J') {
            activeTetrominoColor = Color.BLUE;
            if (drawingTurns == 0) {
                setRectStatusAt(0, j, 'A');

            } else {
                setRectStatusAt(0, j, 'A');
                setRectStatusAt(0, j + 1, 'A');
                setRectStatusAt(0, j + 2, 'A');
            }
        }
        if (drawingTurns == 0) {
            //System.out.println("TetriminoDrawing false!");
            setDrawingPermission(false);
        }
    }

    public void setDrawingPermission(boolean allowTetrominoDrawing) {
        if (allowTetrominoDrawing == true){ //if something set drawing to true, make a new random tetromino
            char[] possibleTetrominos = {'I', 'O', 'Z', 'S', 'T', 'J', 'L'};
            Random rand = new Random();
            randomTetrominoSP = possibleTetrominos[rand.nextInt(possibleTetrominos.length)];
        }
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
    void rotate(){
        List<Integer> activeRectCoordI = new ArrayList<>();
        List<Integer> activeRectCoordJ = new ArrayList<>();
        for (int i = 0; i < ruudud.length; i++) {  //Find all active blocks
            for (int j = 0; j < ruudud[i].length; j++) {
                if (getRectStatusAt(i, j) == 'A') {
                    activeRectCoordI.add(i); //remember where the active blocks are
                    activeRectCoordJ.add(j);
                }
            }
        }
        int minI = activeRectCoordI.get(0);
        int minJ = activeRectCoordJ.get(0);
        for (int i = 1; i < activeRectCoordI.size(); i++) {
            if (activeRectCoordI.get(i) < minI){
                minI = activeRectCoordI.get(i);
            }
            if (activeRectCoordJ.get(i) < minJ){
                minJ = activeRectCoordJ.get(i);
            }
        }

        if (tetrominoRotationTracker == 0){
            activeTetrominoMatrix = new int[3][3];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    activeTetrominoMatrix[i][j] = 0;
                }
            }
            for (int i = 0; i < activeRectCoordI.size(); i++) {
                activeTetrominoMatrix[activeRectCoordI.get(i) - minI][activeRectCoordJ.get(i) - minJ] = 1;
                setRectStatusAt(activeRectCoordI.get(i), activeRectCoordJ.get(i), 'B');
            }
            transponeeri(activeTetrominoMatrix);
            vahetaRead(activeTetrominoMatrix);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (activeTetrominoMatrix[i][j] == 1){
                        setRectStatusAt(i + minI, j + minJ, 'A');
                    }
                }
            }
            tetrominoRotationTracker += 1;
        }
        else if (tetrominoRotationTracker == 1){
            for (int i = 0; i < activeRectCoordI.size(); i++) {
                setRectStatusAt(activeRectCoordI.get(i), activeRectCoordJ.get(i), 'B');
            }
            transponeeri(activeTetrominoMatrix);
            vahetaRead(activeTetrominoMatrix);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (activeTetrominoMatrix[i][j] == 1){
                        setRectStatusAt(i + minI, j + minJ, 'A');
                    }
                }
            }
            tetrominoRotationTracker += 1;
        }
        else if (tetrominoRotationTracker == 2){
            for (int i = 0; i < activeRectCoordI.size(); i++) {
                setRectStatusAt(activeRectCoordI.get(i), activeRectCoordJ.get(i), 'B');
            }
            transponeeri(activeTetrominoMatrix);
            vahetaRead(activeTetrominoMatrix);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (activeTetrominoMatrix[i][j] == 1){
                        setRectStatusAt(i + minI - 1, j + minJ, 'A');
                    }
                }
            }
            tetrominoRotationTracker += 1;
        }
        else if (tetrominoRotationTracker == 3){
            for (int i = 0; i < activeRectCoordI.size(); i++) {
                setRectStatusAt(activeRectCoordI.get(i), activeRectCoordJ.get(i), 'B');
            }
            transponeeri(activeTetrominoMatrix);
            vahetaRead(activeTetrominoMatrix);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (activeTetrominoMatrix[i][j] == 1){
                        setRectStatusAt(i + minI, j + minJ - 1, 'A');
                    }
                }
            }
            tetrominoRotationTracker += 1;
        }
        if (tetrominoRotationTracker == 4){
            tetrominoRotationTracker = 0;
        }
    }
    boolean rotateLeft() {
        if (tetrominoType == 'O'){
            return false;
        }
        List<Integer> activeRectCoordI = new ArrayList<>();
        List<Integer> activeRectCoordJ = new ArrayList<>();
        for (int i = 0; i < ruudud.length; i++) {  //Find all active blocks
            for (int j = 0; j < ruudud[i].length; j++) {
                if (getRectStatusAt(i, j) == 'A') {
                    activeRectCoordI.add(i); //remember where the active blocks are
                    activeRectCoordJ.add(j);
                }
            }
        }

        List<Integer> changedActiveRectCoordI = new ArrayList<>();
        List<Integer> changedActiveRectCoordJ = new ArrayList<>();
        boolean canRotate = true;
       //System.out.println("Rotate left triggered");
        if (tetrominoType == 'S'){
            if (tetrominoRotationTracker == 0) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) + 2);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0));
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1) - 2);
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3));
            }
            else if (tetrominoRotationTracker == 1){
                changedActiveRectCoordI.add(activeRectCoordI.get(0) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) + 2);
                changedActiveRectCoordI.add(activeRectCoordI.get(1) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3));
            }
            else if (tetrominoRotationTracker == 2){
                changedActiveRectCoordI.add(activeRectCoordI.get(0));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0));
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2) + 2);
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 2);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3));
            }
            else if (tetrominoRotationTracker == 3){
                changedActiveRectCoordI.add(activeRectCoordI.get(0));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0));
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) - 2);
            }
        }
        if (tetrominoType == 'Z') {
            if (tetrominoRotationTracker == 0) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0));
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) - 2);
            } else if (tetrominoRotationTracker == 1) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) + 2);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0));
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) + 2);
            } else if (tetrominoRotationTracker == 2) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) + 2);
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3));
            } else if (tetrominoRotationTracker == 3) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) - 2);
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 2);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3));
            }
        }
        if (tetrominoType == 'T') {
            if (tetrominoRotationTracker == 0) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0));
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) - 1);
            } else if (tetrominoRotationTracker == 1) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) + 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3));
            } else if (tetrominoRotationTracker == 2) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) + 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3));
            } else if (tetrominoRotationTracker == 3) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0));
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) - 1);
            }
        }
        if (tetrominoType == 'L') {
            if (tetrominoRotationTracker == 0) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) - 2);
                changedActiveRectCoordI.add(activeRectCoordI.get(1) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1) + 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) - 1);
            } else if (tetrominoRotationTracker == 1) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) + 2);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0));
                changedActiveRectCoordI.add(activeRectCoordI.get(1) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1) - 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) + 1);
            } else if (tetrominoRotationTracker == 2) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) + 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) + 1);
            } else if (tetrominoRotationTracker == 3) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) + 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2) - 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3));
            }
        }
        if (tetrominoType == 'J') {
            if (tetrominoRotationTracker == 0) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) + 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(1) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) - 1);
            } else if (tetrominoRotationTracker == 1) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) - 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2) + 2);
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) + 1);
            } else if (tetrominoRotationTracker == 2) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) + 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 2);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) - 1);
            } else if (tetrominoRotationTracker == 3) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) - 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(1) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1) - 2);
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) + 1);
            }
        }
        else if (tetrominoType == 'I') {
            if (tetrominoRotationTracker == 0) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) + 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2) - 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(3) + 2);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) - 2);
            }
            else if (tetrominoRotationTracker == 1){
                changedActiveRectCoordI.add(activeRectCoordI.get(0) + 2);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) - 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(1) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1));
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2) + 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) + 2);

            }
            else if (tetrominoRotationTracker == 2) {
                changedActiveRectCoordI.add(activeRectCoordI.get(0) - 2);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) + 2);
                changedActiveRectCoordI.add(activeRectCoordI.get(1) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1) + 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(2));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) - 1);
            }
            else if (tetrominoRotationTracker == 3){
                changedActiveRectCoordI.add(activeRectCoordI.get(0) + 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(0) - 2);
                changedActiveRectCoordI.add(activeRectCoordI.get(1));
                changedActiveRectCoordJ.add(activeRectCoordJ.get(1) - 1);
                changedActiveRectCoordI.add(activeRectCoordI.get(2) - 1);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(2));
                changedActiveRectCoordI.add(activeRectCoordI.get(3) - 2);
                changedActiveRectCoordJ.add(activeRectCoordJ.get(3) + 1);
            }
        }
        for (int i = 0; i < changedActiveRectCoordI.size(); i++) {
            if (changedActiveRectCoordI.get(i) < 0 || changedActiveRectCoordJ.get(i) < 0
                    || changedActiveRectCoordI.get(i) >= ruudud.length ||
                    changedActiveRectCoordJ.get(i) >= ruudud[0].length ||
                    getRectStatusAt(changedActiveRectCoordI.get(i),changedActiveRectCoordJ.get(i)) == 'P'){
                canRotate = false;
            }
        }
       // System.out.println("Can rotate: " + canRotate);
        if (canRotate) {
            tetrominoRotationTracker += 1;
            if (tetrominoRotationTracker == 4){
                tetrominoRotationTracker = 0;
            }
            for (int i = 0; i < activeRectCoordI.size(); i++) {
                setRectStatusAt(activeRectCoordI.get(i), activeRectCoordJ.get(i), 'B');
            }
            for (int i = 0; i < changedActiveRectCoordI.size(); i++) {
                setRectStatusAt(changedActiveRectCoordI.get(i), changedActiveRectCoordJ.get(i), 'A');
            }
        }
        return canRotate;
    }
    boolean gameStateOver(){
        return gameOver;
    }
    void drop (){
        boolean keepticking = true;
        do {
            keepticking = tick();
        }
        while (keepticking);
    }

    public char getRandomTetrominoMP() {
        return randomTetrominoMP;
    }

    public void setRandomTetrominoMP(char randomTetrominoMP) {
        this.randomTetrominoMP = randomTetrominoMP;
    }

    public boolean isNewRandomTetroReceived() {
        return newRandomTetroReceived;
    }

    public void setNewRandomTetroReceived(boolean newRandomTetroReceived) {
        this.newRandomTetroReceived = newRandomTetroReceived;
    }
}
