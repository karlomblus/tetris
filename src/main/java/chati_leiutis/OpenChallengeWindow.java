package chati_leiutis;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OpenChallengeWindow {
    Stage lava;

    public OpenChallengeWindow(Stage lava) {
        this.lava = lava;
    }

    public void close() {
        lava.close();
    }

    public void start(Stage primaryStage, String user, Klient client) {
        VBox comp = new VBox();
        comp.setAlignment(Pos.CENTER);
        Label messagelabel = new Label("Challenging " + user + ". Waiting for response...");

        Button closebutton = new Button("Close");
        closebutton.setOnMouseClicked((event) -> {
            try {
                client.setChallengeOpen(false);
                primaryStage.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            primaryStage.close();
        });

        comp.getChildren().addAll(messagelabel, closebutton);
        Scene stageScene = new Scene(comp, 250, 130);
        primaryStage.setScene(stageScene);
        primaryStage.show();
    }

}
