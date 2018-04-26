package tetrispackage;

import chati_leiutis.Klient;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class TetrisGraafikaMultiplayer {
    private int numberOfPlayers = 2;
    private final int resoWidth = 150 * 2;
    private final int resoHeight = 330;
    private final int ruuduSuurus = 15;
    private final int mitukuubikutLaiuses = resoWidth / ruuduSuurus / numberOfPlayers;
    private final int mitukuubikutPikkuses = resoHeight / ruuduSuurus;
    private Rectangle[][] ristkülik = new Rectangle[mitukuubikutPikkuses][mitukuubikutLaiuses];
    private Rectangle[][] ristkülik2 = new Rectangle[mitukuubikutPikkuses][mitukuubikutLaiuses];
    private IntegerProperty tickProperty = new SimpleIntegerProperty();
    public static final char UP = 0;
    public static final char DOWN = 1;
    public static final char LEFT = 2;
    public static final char RIGHT = 3;

    private Tetromino myTetromino;
    private Tetromino opponentTetromino;
    private Map<KeyCode, Boolean> myCurrentActiveKeys = new HashMap<>();
    private Klient client;


    //chati
    private TextArea chatWindow;
    private TextField writeArea;
    private Integer opponentID;
    private IntegerProperty opponentMoved = new SimpleIntegerProperty();

    //lisasin client, et kasutada Klient klassi meetodeid
    public void start(Stage peaLava, Klient client, Integer opponentID) {
        this.client = client;
        opponentMoved.setValue(-1);
        this.opponentID = opponentID;
        HBox hbox = new HBox(1);
        Group localTetrisArea = new Group(); // luuakse localTetrisArea
        Group opponentTetrisArea = new Group();
        //chati kood
        VBox mpChatVbox = new VBox();
        TextArea messagearea = new TextArea();
        chatWindow = messagearea;
        //muudan aknad mitteklikitavaks
        messagearea.setEditable(false);
        messagearea.setWrapText(true);
        messagearea.setPrefSize(140, 300);
        messagearea.setPromptText("Messages...");
        messagearea.setMouseTransparent(true);
        messagearea.setFocusTraversable(false);

        TextField writearea = new TextField();
        this.writeArea = writearea;
        writearea.setPromptText("Type your message here...");
        writearea.setPrefWidth(200);
        writearea.setFocusTraversable(false);


        writearea.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {

                try {
                    client.sendSomething(105);
                    chatWindow.appendText(client.getNimi() + ">> " + writearea.getText() + "\n");
                } catch (IOException error) {
                    messagearea.appendText("Socket kinni. Ei saanud saata.");
                }
                writearea.clear();
            }
        });

        mpChatVbox.getChildren().addAll(messagearea, writearea);

        //chati koodi lõpp
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

        myTetromino = new Tetromino(ristkülik);
        opponentTetromino = new Tetromino(ristkülik2);

//User navigates forward a page, update page changer object.
        tickProperty.addListener((ChangeListener) (o, oldVal, newVal) -> {
            if (myTetromino.isNewRandomTetroReceived()) {
                tickAndDrawForMe();
            }
            if (opponentTetromino.isNewRandomTetroReceived()) {
                tickAndDrawForOpponent();
            }
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
            }
            opponentMoved.setValue(-1);
        });


        //noded-e paigutamine
        hbox.getChildren().add(localTetrisArea);
        hbox.getChildren().add(opponentTetrisArea);
        hbox.getChildren().add(mpChatVbox);

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
            Platform.exit();
            //PlatformImpl.tkExit()
            //tickTime.stop();
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
                            messagearea.appendText("Socket closed. Keypress sending failed!");
                        }
                    }
                }
                if (myCurrentActiveKeys.containsKey(KeyCode.LEFT) && myCurrentActiveKeys.get(KeyCode.LEFT)) {
                    if (myTetromino.moveLeft()) {
                        try {
                            client.sendKeypress(tickProperty.getValue(), LEFT);
                        } catch (IOException error) {
                            messagearea.appendText("Socket closed. Keypress sending failed!");
                        }
                    }
                }
                if (myCurrentActiveKeys.containsKey(KeyCode.UP) && myCurrentActiveKeys.get(KeyCode.UP)) {
                    if (myTetromino.rotateLeft()) {
                        try {
                            client.sendKeypress(tickProperty.getValue(), UP);
                        } catch (IOException error) {
                            messagearea.appendText("Socket closed. Keypress sending failed!");
                        }
                    }
                }
                if (myCurrentActiveKeys.containsKey(KeyCode.DOWN) && myCurrentActiveKeys.get(KeyCode.DOWN)) {
                    try {
                        client.sendKeypress(tickProperty.getValue(), DOWN);
                    } catch (IOException error) {
                        messagearea.appendText("Socket closed. Keypress sending failed!");
                    }
                    myTetromino.drop();
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
                myTetromino.tick();
                myTetromino.isRowFilled();
                if (myTetromino.isDrawingAllowed()) {
                    myTetromino.draw("multiplayer");
                    if (myTetromino.getDrawingTurns() == 0) //drawing completed
                    {
                        myTetromino.setNewRandomTetroReceived(false);
                        try {
                            client.requestRandomTetro();
                            System.out.println("Requesting random tetro");
                        } catch (Exception error) {
                            System.out.println("Socket closed. Keypress sending failed!");
                        }
                    }
                }
            }
        }
    void tickAndDrawForOpponent() {
        if (!opponentTetromino.gameStateOver()) {
            opponentTetromino.tick();
            opponentTetromino.isRowFilled();
            if (opponentTetromino.isDrawingAllowed()) {
                opponentTetromino.draw("multiplayer");
                if (opponentTetromino.getDrawingTurns() == 0) //drawing completed
                {
                    opponentTetromino.setNewRandomTetroReceived(false);
                }
            }
        }
    }



    public void addNewMessage(String name, String message) {
        chatWindow.appendText(name + ">> " + message + "\n");
    }

    public String sendMessageandclearMP() {
        String toReturn = writeArea.getText();
        return toReturn;
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
}
