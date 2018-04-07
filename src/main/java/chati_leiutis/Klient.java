package chati_leiutis;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import tetrispackage.TetrisGraafika;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Klient extends Application {
    boolean connected = false;
    Socket connection;
    DataOutputStream out;
    TextArea ekraan;
    static String nimi;
    TextField konsool;
    ClientThread listener;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //todo socketite tegemise võiks vist logini juurde viia
        try (Socket socket = new Socket("tetris.carlnet.ee", 54321);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            this.connection = socket;
            this.out = output;
            System.out.println("Connection to tetris.calnet.ee established...");

            listener = new ClientThread(socket, this, input);
            Thread clienthread = new Thread(listener);
            clienthread.start();
            connected = true;
            showLogIn();
            showLobby();
        }
    }

    public Klient() {
    }

    public Klient(DataOutputStream out) {
        this.out = out;
    }

    public void sendSomething(Integer type) throws IOException {
        if (connected) {
            //temporary names/pswrd for testing
            String tempnimi = "Ingo";
            String temppassword = "ingopass";
            int tempid = 7;

            out.flush();
            System.out.println("Saatsin " + type);
            //vastavalt prokokollile:
            switch (type) {
                case 1:
                    out.writeInt(type);
                    logIn_or_Register(tempnimi, temppassword);
                    break;
                case 2:
                    out.writeInt(type);
                    logIn_or_Register(tempnimi, temppassword);
                    break;
                case 3:
                    out.writeInt(type);
                    //jääme ootama userlisti
                    break;
                case 4:
                    out.writeInt(type);
                    //väljalogimine
                    break;
                case 5:
                    out.writeInt(type);
                    sendAndClearField(konsool);
                    break;
                case 6:
                    out.writeInt(type);
                    //ootame tagasi käivate mängude listi
                    break;
                default:
                    // ei tee midagi
            }
        }
    }

    public void logIn_or_Register(String username, String password) throws IOException {
        out.writeUTF(username);
        System.out.println("saatsin nime");
        out.writeUTF(password);
        System.out.println("saatsin pswrd");
    }

    public void recieveMessage(int userID, String username, String message) {
            ekraan.appendText(username + ">> " + message + "\n");
    }

    public void showLobby() throws Exception {
        //säti suurus
        int w = 900;
        int h = 800;
        Stage primaryStage = new Stage();

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

        messagearea.setPrefSize((w / 4) * 3, (h / 4.5) * 3);
        userlist.setPrefSize((w / 4) - 2, (h / 4.5) * 3);

        userlist.setPromptText("Users");
        messagearea.setPromptText("Messages...");

        TextField messagefield = new TextField();
        messagefield.setPromptText("Enter message here...");
        messagefield.setFont(labelfont);
        messagefield.setPrefWidth((w / 3.5) * 3);
        konsool = messagefield;

        //pilt

        Image chatImage = new Image("file:\\C:\\Users\\Ingo\\IdeaProjects\\OOPprojekt\\tetris\\src\\main\\resources\\Tetris.png", 850, 200, true, false);
        ImageView pilt = new ImageView(chatImage);

        //TODO pole kindel kas selline lahendus on okei, aga töötab hetkel.
        Button singleplayerbtn = new Button("Singleplayer");
        TetrisGraafika tetris = new TetrisGraafika();
        singleplayerbtn.setOnMouseClicked((MouseEvent) -> {

            try {
                Stage lava = new Stage();
                tetris.start(lava);

            } catch (Exception e) {
                throw new RuntimeException(e);

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
                try {
                    sendSomething(5);
                } catch (Exception e2) {
                    throw new RuntimeException(e2);
                }
            }
        });

        messagefield.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                //kui kirjutame logout, siis logime välja
                try {
                    if (messagefield.getText().equals("logout")) {
                        sendSomething(4);
                        messagefield.clear();
                        ekraan.appendText("You have been disconnected...");
                        listener.shutDown();
                        out.close();
                        connected = false;
                    } else
                        sendSomething(5);
                } catch (Exception e3) {
                    throw new RuntimeException(e3);
                }
            }
        });
        VBox outervbox = new VBox();
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        VBox vbox2 = new VBox();
        HBox hbox2 = new HBox();

        vbox.getChildren().addAll(userlabel, userlist);
        vbox2.getChildren().addAll(chatlabel, messagearea);
        hbox.getChildren().addAll(vbox, vbox2);
        hbox.setSpacing(2);
        hbox2.getChildren().addAll(messagefield, sendbtn);
        outervbox.getChildren().addAll(hbox, hbox2, border);
        juur.getChildren().add(outervbox);

        Scene lava = new Scene(juur, w, h);
        primaryStage.setResizable(false);
        primaryStage.setScene(lava);
        primaryStage.setTitle("Client");
        primaryStage.centerOnScreen();
        primaryStage.showAndWait();
    }

    public void showRegistration() {
        Stage newStage = new Stage();
        VBox comp = new VBox();
        Label namelabel = new Label("Enter your credentials below:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name here...");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password here");

        comp.getChildren().add(namelabel);
        comp.getChildren().add(nameField);
        Button registernupp = new Button("Register and close");
        registernupp.setOnMouseClicked((event) -> {
            try {
                //registreering
                sendSomething(1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            newStage.close();
        });
        Scene stageScene = new Scene(comp, 250, 270);
        comp.getChildren().add(passwordField);
        comp.getChildren().add(registernupp);
        newStage.setScene(stageScene);
        newStage.show();
    }

    public void showLogIn() {
        Stage newStage = new Stage();
        VBox comp = new VBox();
        HBox nupudkõrvuti = new HBox();
        Label namelabel = new Label("Enter your credentials below:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name here...");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password here");

        comp.getChildren().add(namelabel);
        comp.getChildren().add(nameField);
        Button login_nupp = new Button("Log in");
        login_nupp.setOnMouseClicked((event) -> {
            nimi = nameField.getText();
            try {
                //logime sisse
                sendSomething(2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            newStage.close();
        });

        Button registreerimis_nupp = new Button("Register new user");
        registreerimis_nupp.setOnMouseClicked((event) -> {
            showRegistration();
        });

        Scene stageScene = new Scene(comp, 300, 300);
        comp.getChildren().add(passwordField);
        nupudkõrvuti.getChildren().addAll(login_nupp, registreerimis_nupp);
        comp.getChildren().add(nupudkõrvuti);
        newStage.setScene(stageScene);
        newStage.showAndWait();
    }

    public void sendMessage(String msg) throws Exception {
        out.writeUTF(msg);
        out.flush();
    }

    public void sendAndClearField(TextField ekraan) {
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
