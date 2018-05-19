package chati_leiutis;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import tetrispackage.TetrisGraafika;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static chati_leiutis.MessageID.LOGIN;

public class LoginWindow {
    private PasswordField loginpasswordfield;
    private TextField loginnamefield;
    private BlockingQueue<Integer> toLoginorNot = new ArrayBlockingQueue<>(5);
    Stage lava;

    public LoginWindow(Stage stage) {
        this.lava = stage;
    }

    public PasswordField getLoginpasswordfield() {
        return loginpasswordfield;
    }

    public TextField getLoginnamefield() {
        return loginnamefield;
    }

    public void start(Stage primaryStage, Klient client) {
        Stage newStage = new Stage();

        //viitan selle loginwindowi cliendile
        client.setLogin(this);

        BorderPane border = new BorderPane();
        Button singleplayerbutton = new Button("Singleplayer");
        singleplayerbutton.setFont(new Font(20));
        border.setBottom(singleplayerbutton);
        //Singeplayeri käivitamine
        singleplayerbutton.setOnMouseClicked((MouseEvent) -> {

            try {
                TetrisGraafika tetris = new TetrisGraafika();
                TetrisGraafika tetris2 = new TetrisGraafika();
                Stage lava = new Stage();
                tetris.start(lava);
                tetris2.start(lava);

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
        login_nupp.setDefaultButton(true);

        //login event- ootame blockqueuest vastust,vastavalt sellele tegutseme
        login_nupp.setOnAction(ev -> {
            try {
                if (client.getConnection() == null || client.getConnection().isClosed()) {
                    errorlabel.setText("Error, connection error. Please restart.");
                } else {
                    client.sendSomething(LOGIN);
                    //sätin cliendi siseseks nimeks proovitud nime
                    //nimi = loginnamefield.getText();
                    int loginvastus = toLoginorNot.take();
                    switch (loginvastus) {
                        case 1:
                            client.setLoggedIN(true);
                            //client.isLoggedIN() = true;
                            newStage.close();
                            break;
                        case -1:
                            errorlabel.setText("Error. Please try again.");
                            break;
                        case 0:
                            errorlabel.setText("Unexpected input, something went wrong.");

                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        loginnamefield.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                login_nupp.fire();
            }
        });

        Button registreerimis_nupp = new Button("Register new user");
        registreerimis_nupp.setOnMouseClicked((event) -> {
            client.showRegistration();
        });

        //kui ristist kinni panna, jätan seisma
        newStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Closing application...");
                client.setLoggedIN(false);
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
}
