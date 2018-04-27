package tetrispackage;

import chati_leiutis.Klient;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class TetrisGraafikaMultiplayer {
    private int numberOfPlayers = 2;
    private final int resoWidth = 150 * 2;
    private final int resoHeight = 330;
    private IntegerProperty tickProperty = new SimpleIntegerProperty();
    private int randomTetroRequestSent = 1;
    public static final char UP = 0;
    public static final char DOWN = 1;
    public static final char LEFT = 2;
    public static final char RIGHT = 3;
    private int syncproblem = 0;

    private Tetromino myTetromino;
    private Tetromino opponentTetromino;
    private Map<KeyCode, Boolean> myCurrentActiveKeys = new HashMap<>();
    private Klient client;

    //chati
    private Integer opponentID;
    private IntegerProperty opponentMoved = new SimpleIntegerProperty();
    private int opponentMoveTiksuID = 0;
    private PrivateChat privateChat;

    public TetrisGraafikaMultiplayer() {
        opponentMoved.setValue(-1);
    }

    //lisasin client, et kasutada Klient klassi meetodeid
    public void start(Stage peaLava, Klient client, Integer opponentID) {
        this.client = client;
        privateChat = new PrivateChat(client);
        this.opponentID = opponentID;
        HBox hbox = new HBox(10);
        Group localTetrisArea = new Group(); // luuakse localTetrisArea
        Group opponentTetrisArea = new Group();
        //chati kood

        //chati koodi lõpp
        TetrisRectangle localTetrisRect = new TetrisRectangle();
        localTetrisRect.fill(localTetrisArea);
        TetrisRectangle opponentTetrisRect = new TetrisRectangle();
        opponentTetrisRect.fill(opponentTetrisArea);

        myTetromino = new Tetromino(localTetrisRect.getRistkülik());
        opponentTetromino = new Tetromino(opponentTetrisRect.getRistkülik());

//User navigates forward a page, update page changer object.
        tickProperty.addListener((ChangeListener) (o, oldVal, newVal) -> {
            tickAndDrawForMe();
            tickAndDrawForOpponent();
        });
        opponentMoved.addListener((ChangeListener) (o, oldVal, newVal) -> {
            if (!opponentTetromino.isDrawingAllowed() && !opponentTetromino.gameStateOver()) {
                if (opponentMoved.getValue() == 2) {
                    opponentTetromino.moveLeft();
                } else if (opponentMoved.getValue() == 3) {
                    opponentTetromino.moveRight();
                } else if (opponentMoved.getValue() == 0) {
                    opponentTetromino.rotateLeft();
                } else if (opponentMoved.getValue() == 1) {
                    opponentTetromino.drop();
                }
                if (opponentMoveTiksuID != tickProperty.getValue()) {
                    System.out.println("OUT OF SYNC!!! ");
                    System.out.println("MoveTiks: " + opponentMoveTiksuID);
                    System.out.println("TickID " + tickProperty.getValue());
                    syncproblem = 1;
                }
            }
            opponentMoved.setValue(-1);
        });


        //noded-e paigutamine
        hbox.getChildren().add(localTetrisArea);
        hbox.getChildren().add(opponentTetrisArea);
        hbox.getChildren().add(privateChat.getChatArea());

        //kood selleks, et klikkides tetrise mängule deselectib chatirea.(Muidu ei saa klotse liigutada peale chattimist)
        localTetrisArea.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                hbox.requestFocus();
            }
        });
        opponentTetrisArea.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                hbox.requestFocus();
            }
        });

        peaLava.setOnCloseRequest((we) -> {
            System.out.println("Tetris stage closed!");
            //multiplayer läheb kinni
            client.setMpgameopen(false);
            try {
                client.sendSomething(102);
            } catch (IOException e) {
                System.out.println("Failed to send info about closing/exiting the MP game");
            }
            Platform.exit();
        });
        peaLava.setOnShowing(event -> { //Do only once
            //draw('I');
        });
        Scene tetrisStseen = new Scene(hbox, resoWidth + 140, resoHeight, Color.SNOW);  // luuakse stseen
        tetrisStseen.setOnKeyPressed(event -> {
            myCurrentActiveKeys.put(event.getCode(), true);
            if (!myTetromino.isDrawingAllowed() && !myTetromino.gameStateOver()) {
                if (myCurrentActiveKeys.containsKey(KeyCode.RIGHT) && myCurrentActiveKeys.get(KeyCode.RIGHT)) {
                    if (myTetromino.moveRight()) {
                        try {
                            client.sendKeypress(tickProperty.getValue(), RIGHT);
                        } catch (IOException error) {
                            privateChat.keyPressSendingFailed();
                        }
                    }
                }
                if (myCurrentActiveKeys.containsKey(KeyCode.LEFT) && myCurrentActiveKeys.get(KeyCode.LEFT)) {
                    if (myTetromino.moveLeft()) {
                        try {
                            client.sendKeypress(tickProperty.getValue(), LEFT);
                        } catch (IOException error) {
                            privateChat.keyPressSendingFailed();
                        }
                    }
                }
                if (myCurrentActiveKeys.containsKey(KeyCode.UP) && myCurrentActiveKeys.get(KeyCode.UP)) {
                    if (myTetromino.rotateLeft()) {
                        try {
                            client.sendKeypress(tickProperty.getValue(), UP);
                        } catch (IOException error) {
                            privateChat.keyPressSendingFailed();
                        }
                    }
                }
                if (myCurrentActiveKeys.containsKey(KeyCode.DOWN) && myCurrentActiveKeys.get(KeyCode.DOWN)) {
                    try {
                        client.sendKeypress(tickProperty.getValue(), DOWN);
                    } catch (IOException error) {
                        privateChat.keyPressSendingFailed();
                    }
                    myTetromino.drop();
                    myTetromino.setNewRandomTetroReceived(false);
                    randomTetroRequestSent = 0;
                }
            }
        });
        tetrisStseen.setOnKeyReleased(event ->
                myCurrentActiveKeys.put(event.getCode(), false)
        );

        peaLava.setTitle("Tetris");  // lava tiitelribale pannakse tekst

        peaLava.setScene(tetrisStseen);  // lavale lisatakse stseen
        peaLava.show();  // lava tehakse nähtavaks
    }

    public void begin() {
        //launch();
    }

    void tickAndDrawForMe() {
        if (!myTetromino.gameStateOver()) {
            if (myTetromino.isDrawingAllowed()) {
                if (randomTetroRequestSent == 0) //Only send one request, 1 by default
                {
                    randomTetroRequestSent = 1;
                    myTetromino.setNewRandomTetroReceived(false);  //When it is time to draw a new tetro, disallow ticking and drawing until received
                    try {
                        client.requestRandomTetro();
                        System.out.println("Requesting random tetro");
                    } catch (Exception error) {
                        System.out.println("Socket closed. Keypress sending failed!");
                    }
                }
            }
            if (myTetromino.isNewRandomTetroReceived()) {//wait until new randomtetro received
                if (myTetromino.isDrawingAllowed()) {
                    myTetromino.draw("multiplayer"); //after drawing, getDrawingTurns is no longer 2
                }
                myTetromino.tick();
                if (myTetromino.getDrawingTurns() == 2) {
                    randomTetroRequestSent = 0;
                }
            }

        }
    }

    void tickAndDrawForOpponent() {
        if (!opponentTetromino.gameStateOver()) {
            opponentTetromino.tick();
            if (opponentTetromino.isDrawingAllowed() && opponentTetromino.isNewRandomTetroReceived()) {
                opponentTetromino.draw("multiplayer");
                if (opponentTetromino.getDrawingTurns() == 0) {
                    opponentTetromino.setNewRandomTetroReceived(false);
                }
            }
        }
    }


    public Integer getOpponentID() {
        return opponentID;
    }

    public void setTickValue(int value) {
        tickProperty.set(value);
    }

    public void setOpponentMoved(int state) {
        this.opponentMoved.setValue(state);
    }

    public Tetromino getMyTetromino() {
        return myTetromino;
    }

    public Tetromino getOpponentTetromino() {
        return opponentTetromino;
    }

    public void setOpponentMoveTiksuID(int opponentMoveTiksuID) {
        this.opponentMoveTiksuID = opponentMoveTiksuID;
    }

    public PrivateChat getPrivateChat() {
        return privateChat;
    }
}
