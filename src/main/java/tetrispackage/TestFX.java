package tetrispackage;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestFX extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage peaLava) throws Exception {

        TetrisGraafika tetris = new TetrisGraafika();


        Button nupp1 = new Button("Run");
        nupp1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.out.println("nupuvajutus");

                    Stage stage = new Stage();

                    tetris.start(stage);
                


            }
        });

        BorderPane piiriPaan = new BorderPane();
        Label tekst = new Label("Mää");
        piiriPaan.setBottom(tekst); // alaserva

        piiriPaan.setBottom(nupp1);

        Scene stseen1 = new Scene(piiriPaan, 500, 80, Color.SNOW);
        peaLava.setScene(stseen1);
        peaLava.show();

    } // start


}



