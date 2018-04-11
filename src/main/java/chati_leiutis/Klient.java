package chati_leiutis;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import tetrispackage.TetrisGraafika;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Klient extends Application {
    private BlockingQueue<Integer> toLoginorNot = new ArrayBlockingQueue<>(5);
    private boolean challengeOpen = false;
    private boolean appRunning = true;
    private boolean loggedIN = false;
    private boolean lobbyOpen = false;
    private String nimi;
    private HashMap<Integer, String> online_users = new HashMap<>();
    private ObservableList<String> observableUsers;
    private Socket connection;
    private DataOutputStream out;
    private TextArea ekraan;
    private ListView<String> userListView = new ListView<>();
    private TextField konsool;
    private PasswordField loginpasswordfield;
    private TextField loginnamefield;
    private PasswordField regpasswordfield;
    private TextField regnamefield;
    private ClientThread listener;

    Stage loginwindow;

    public Klient() {
    }

    public Klient(DataOutputStream out) {
        this.out = out;
    }


    public void showRegistration() {
        Stage newStage = new Stage();
        VBox comp = new VBox();
        Label namelabel = new Label("Enter your credentials below:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name here...");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password here");
        regnamefield = nameField;
        regpasswordfield = passwordField;

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
        loginwindow = newStage;

        BorderPane border = new BorderPane();
        Button singleplayerbutton = new Button("Singleplayer");
        singleplayerbutton.setFont(new Font(20));
        border.setBottom(singleplayerbutton);
        //Singeplayeri käivitamine
        singleplayerbutton.setOnMouseClicked((MouseEvent) -> {

            try {
                TetrisGraafika tetris = new TetrisGraafika();
                Stage lava = new Stage();
                tetris.start(lava);

            } catch (Exception e) {
                throw new RuntimeException(e);

            }
        });

        VBox comp = new VBox();
        HBox nupudkõrvuti = new HBox();
        Label namelabel = new Label("Enter your credentials below:");
        Label errorlabel = new Label("");                       //siia läheb pärast errormessage
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name here...");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password here");
        loginnamefield = nameField;
        loginpasswordfield = passwordField;

        comp.getChildren().add(namelabel);
        comp.getChildren().add(nameField);

        Button login_nupp = new Button("Log in");

        //login event- ootame blockqueuest vastust,vastavalt sellele tegutseme
        login_nupp.setOnMouseClicked((event) -> {
            nimi = nameField.getText();
            try {
                if(connection == null|| connection.isClosed() ){
                    errorlabel.setText("Error, connection error. Please restart.");
                }
                else{
                    sendSomething(2);
                    int loginvastus = toLoginorNot.take();
                    switch (loginvastus) {
                        case 1:
                            loggedIN = true;
                            newStage.close();
                            break;
                        case -1:
                            errorlabel.setText("Error. Please try again.");
                            break;
                        case 0:
                            errorlabel.setText("Unexpected input, something went wrong.");

                    }}
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Button registreerimis_nupp = new Button("Register new user");
        registreerimis_nupp.setOnMouseClicked((event) -> {
            showRegistration();
        });

        //kui ristist kinni panna, jätan seisma
        newStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Closing application...");
                appRunning = false;
                loggedIN = false;
                //listener.shutDown();   kas seda rida on ikka vaja?
                Platform.exit();
            }
        });
        comp.getChildren().add(passwordField);
        nupudkõrvuti.getChildren().addAll(login_nupp, registreerimis_nupp);
        comp.getChildren().add(nupudkõrvuti);
        comp.getChildren().add(errorlabel);

        border.setCenter(comp);
        border.setBottom(singleplayerbutton);
        Scene stageScene = new Scene(border, 300, 300);

        newStage.setScene(stageScene);
        newStage.showAndWait();
    }

    public void showLobby() throws Exception {

        //säti suurus
        int w = 900;
        int h = 800;
        Stage primaryStage = new Stage();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Disconnecting...");
                loggedIN = false;
                lobbyOpen = false;
                try {
                    sendSomething(4);

                    //connecter.getOut().close();
                } catch (IOException e) {
                    System.out.println("Socket juba kinni, jätkan välja logimist");
                }
            }
        });
        Group juur = new Group();
        TextArea messagearea = new TextArea();
        ekraan = messagearea;
        observableUsers = FXCollections.observableArrayList(online_users.values());
        userListView.setItems(observableUsers);
        userListView.setPrefSize((w / 4), (h / 4.5) * 3);

        Font labelfont = new Font(16);
        Label userlabel = new Label("Users");
        userlabel.setFont(labelfont);
        Label chatlabel = new Label("Messages");
        chatlabel.setFont(labelfont);

        //muudan aknad mitteklikitavaks
        messagearea.setEditable(false);
        messagearea.setWrapText(true);
        messagearea.setPrefSize((w / 4) * 3, (h / 4.5) * 3);
        messagearea.setPromptText("Messages...");

        //panen userid paika
        sendSomething(3);

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
        Button multiplayerbtn = new Button("Multiplayer");
        multiplayerbtn.setOnMouseClicked((MouseEvent) -> {
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
        stackPane.getChildren().add(multiplayerbtn);
        stackPane.setAlignment(multiplayerbtn, Pos.BOTTOM_RIGHT);
        stackPane.setAlignment(singleplayerbtn, Pos.BOTTOM_CENTER);


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
                        loggedIN = false;
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

        vbox.getChildren().addAll(userlabel, userListView);
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

    public void sendSomething(Integer type) throws IOException {
        System.out.println("Saatsin " + type);
        //vastavalt prokokollile:
        switch (type) {
            case 1:
                out.writeInt(type);
                String regname = regnamefield.getText();
                String regpass = regpasswordfield.getText();
                logIn_or_Register(regname, regpass);
                break;
            case 2:
                out.writeInt(type);
                String loginname = loginnamefield.getText();
                String loginpass = loginpasswordfield.getText();
                logIn_or_Register(loginname, loginpass);
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

    public void logIn_or_Register(String nimi, String parool) throws IOException {
        //nimi
        out.writeUTF(nimi);
        out.writeUTF(parool);
    }

    public void recieveMessage(int userID, String username, String message) {
        ekraan.appendText(username + ">> " + message + "\n");
    }

    public void handleUserList(Integer type, Integer ID, String name) {
        if (loggedIN) {
            switch (type) {
                case 3:
                    online_users.put(ID, name);
                   // userListView.refresh();
                    break;
                case 4:
                    online_users.remove(ID, name);
                   // userListView.refresh();
                    break;
            }
            if (lobbyOpen) {
                observableUsers.clear();
                for (Integer id : online_users.keySet()) {
                    String nameofid = online_users.get(id);
                    observableUsers.add(nameofid);
                }
            }
        }
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


    @Override
    public void start(Stage primaryStage) throws Exception {
        //üritame käivitamisel ühenduse luua
        try (Socket socket = new Socket("tetris.carlnet.ee", 54321);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            this.connection = socket;
            this.out = output;
            System.out.println("Connection to tetris.carlnet.ee established...");

            listener = new ClientThread(connection, this, input, toLoginorNot);
            Thread clienthread = new Thread(listener);
            clienthread.start();
            loggedIN = true;

            //näitame loginekraani
            showLogIn();
            if (loggedIN) {
                lobbyOpen = true;
                showLobby();
            }
        }catch (Exception e){
            System.out.println("Error connecting to server.");
            loggedIN = false;
            showLogIn();
        }
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
