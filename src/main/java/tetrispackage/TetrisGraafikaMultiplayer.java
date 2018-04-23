package tetrispackage;

import chati_leiutis.Klient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
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
import javafx.util.Duration;
//import java.awt.*;
import java.io.IOException;
import java.util.*;

public class TetrisGraafikaMultiplayer {
    private int numberOfPlayers = 2;
    private final int resoWidth = 150 * 2;
    private final int resoHeight = 330;
    final int ruuduSuurus = 15;
    final int mitukuubikutLaiuses = resoWidth / ruuduSuurus / numberOfPlayers;
    final int mitukuubikutPikkuses = resoHeight / ruuduSuurus;
    private Rectangle[][] ristkülik = new Rectangle[mitukuubikutPikkuses][mitukuubikutLaiuses];
    private Rectangle[][] ristkülik2 = new Rectangle[mitukuubikutPikkuses][mitukuubikutLaiuses];
    private IntegerProperty tickProperty = new SimpleIntegerProperty();

    Tetromino tetromino;
    Tetromino tetromino2;
    private Map<KeyCode, Boolean> currentActiveKeys = new HashMap<>();

    //chati
    TextArea chatWindow;
    TextField writeArea;
    private Integer opponentID;

    //lisasin client, et kasutada Klient klassi meetodeid
    public void start(Stage peaLava, Klient client, Integer opponentID) {
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
                    chatWindow.appendText(client.getNimi()+ ">> " + writearea.getText() + "\n");
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

        tetromino = new Tetromino(ristkülik);
        tetromino2 = new Tetromino(ristkülik2);

//User navigates forward a page, update page changer object.
        tickProperty.addListener((ChangeListener) (o, oldVal, newVal) -> {
            //pageNavigator.setPage(pageNoProperty.doubleValue());
            doActions(tickProperty.getValue(), tetromino);
            doActions(tickProperty.getValue(), tetromino2);
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
            currentActiveKeys.put(event.getCode(), true);
            if (!tetromino.isDrawingAllowed() && !tetromino.gameStateOver()) {
                if (currentActiveKeys.containsKey(KeyCode.RIGHT) && currentActiveKeys.get(KeyCode.RIGHT)) {
                    tetromino.moveRight();
                }
                if (currentActiveKeys.containsKey(KeyCode.LEFT) && currentActiveKeys.get(KeyCode.LEFT)) {
                    tetromino.moveLeft();
                }
                if (currentActiveKeys.containsKey(KeyCode.UP) && currentActiveKeys.get(KeyCode.UP)) {
                    tetromino.rotateLeft();
                }
                if (currentActiveKeys.containsKey(KeyCode.SPACE) && currentActiveKeys.get(KeyCode.SPACE)) {
                    boolean keepticking = true;
                    do {
                        keepticking = tetromino.tick();
                    }
                    while (keepticking);
                }
                /*if (currentActiveKeys.containsKey(KeyCode.DOWN) && currentActiveKeys.get(KeyCode.DOWN)) {
                    tetromino.tick();
                }*/
            }


        });
        tetrisStseen.setOnKeyReleased(event ->
                currentActiveKeys.put(event.getCode(), false)
        );

        peaLava.setTitle("Tetris");  // lava tiitelribale pannakse tekst

        peaLava.setScene(tetrisStseen);  // lavale lisatakse stseen
        peaLava.show();  // lava tehakse nähtavaks
    }

    public void begin() {
        //launch();
    }
    void doActions(int tickReceived, Tetromino tetromino) {
            if (!tetromino.gameStateOver()) {
                tetromino.tick();
                tetromino.isRowFilled();
                if (tetromino.isDrawingAllowed()) {
                    tetromino.draw();
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
    public void setTickValue(int value){
        tickProperty.set(value);
    }
}
