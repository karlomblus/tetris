package main.java.chati_leiutis;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import main.java.tetrispackage.TetrisGraafika;

import javax.swing.text.StyledEditorKit;
import java.io.DataOutputStream;
import java.net.Socket;

public class Klient extends Application {
    static boolean cont = true;
    DataOutputStream out;
    TextArea ekraan;
    static String nimi;

    @Override
    public void start(Stage primaryStage) throws Exception {
        showLogIn();
        showLobby();
        out.writeUTF("logout");
    }

    public Klient() {
    }

    public Klient(DataOutputStream out) {
        this.out = out;
    }

    public void recieveMessage(String message) {
        if (message.equals("logout")) {
            cont = false;
            System.exit(0);
        }//TODO halb?, midagi paremat peaks välja mõtlema)
        else {
            ekraan.appendText(nimi + "<< " + message + "\n");
        }
    }
    public void showLobby()throws Exception{
        //säti suurus
        int w = 900;
        int h = 800;
        Stage primaryStage = new Stage();
        Socket socket = new Socket("localhost", 5000);
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        this.out = output;

        Thread clienthread = new Thread(new ClientThread(socket, this));
        clienthread.start();

        Group juur = new Group();
        TextArea messagearea = new TextArea();
        TextArea userlist = new TextArea();
        this.ekraan = messagearea;

        Font labelfont = new Font(16);
        Label userlabel = new Label("Users");
        userlabel.setFont(labelfont);
        Label chatlabel = new Label("Messages");
        chatlabel.setFont(labelfont);

        messagearea.setEditable(false);
        userlist.setEditable(false);

        messagearea.setWrapText(true);
        userlist.setWrapText(true);

        messagearea.setPrefSize((w/4)*3, (h/4.5)*3);
        userlist.setPrefSize((w/4)-2, (h/4.5)*3);

        userlist.setPromptText("Users");
        messagearea.setPromptText("Messages...");

        TextField messagefield = new TextField();
        messagefield.setPromptText("Enter message here...");
        messagefield.setFont(labelfont);
        messagefield.setPrefWidth((w/3.5)*3);

        //pilt

        Image chatImage = new Image("file:\\C:\\Users\\Ingo\\IdeaProjects\\OOPprojekt\\tetris\\src\\main\\resources\\Tetris.png", 850, 200, true, false);
        ImageView pilt = new ImageView(chatImage);

        Button singleplayerbtn = new Button("Singpleplayer");
        singleplayerbtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Application.launch(TetrisGraafika.class);
            }
        });

        StackPane stackPane = new StackPane();
        BorderPane border = new BorderPane();

        border.setBottom(stackPane);
        stackPane.getChildren().add(pilt);
        stackPane.getChildren().add(singleplayerbtn);

        //send nupp
        Button sendbtn = new Button("Send");
        sendbtn.setFont(new Font(20));
        sendbtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                sendandclear(messagefield);
            }
        });

        messagefield.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendandclear(messagefield);
            }
        });
        VBox outervbox = new VBox();
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        VBox vbox2 = new VBox();
        HBox hbox2 = new HBox();

        vbox.getChildren().addAll(userlabel,userlist);
        vbox2.getChildren().addAll(chatlabel,messagearea);
        hbox.getChildren().addAll(vbox,vbox2);
        hbox.setSpacing(2);
        hbox2.getChildren().addAll(messagefield,sendbtn);
        outervbox.getChildren().addAll(hbox,hbox2,border);
        juur.getChildren().add(outervbox);

        Scene lava = new Scene(juur, w, h);
        primaryStage.setResizable(false);
        primaryStage.setScene(lava);
        primaryStage.setTitle("Client");
        primaryStage.centerOnScreen();
        primaryStage.showAndWait();
    }

    public static void showLogIn() {
        Stage newStage = new Stage();
        VBox comp = new VBox();
        Label namelabel = new Label("Enter your name below:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name here...");
        comp.getChildren().add(namelabel);
        comp.getChildren().add(nameField);
        Button niminupp = new Button("Log in");
        niminupp.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                nimi = nameField.getText();
                newStage.close();
            }
        });
        Scene stageScene = new Scene(comp, 300, 300);
        comp.getChildren().add(niminupp);
        newStage.setScene(stageScene);
        newStage.showAndWait();
    }

    public void sendandclear(TextField ekraan){
        try {
            out.writeUTF(ekraan.getText());
            out.flush();
            ekraan.clear();
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }
    public static void main(String[] args) throws Exception {
        launch(args);
    }

}
