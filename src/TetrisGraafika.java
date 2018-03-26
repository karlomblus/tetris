
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.HashMap;


public class TetrisGraafika extends Application {

    @Override
    public void start(Stage peaLava) throws Exception {
        Group juur = new Group(); // luuakse juur
        int mitukuubikut = 50;
        HashMap<Integer, Color> rectToColor = new HashMap<>();
        Rectangle ristkülik[][] = new Rectangle[50][50];
        int ruuduSuurus = 10;

        int kuubikutLaiuses = mitukuubikut;
        int kuubikutPikkuses = mitukuubikut;
        for (int i = 0; i < kuubikutPikkuses; i++) {
            for (int j = 0; j < kuubikutLaiuses; j++) {
                ristkülik[i][j] = new Rectangle(j * ruuduSuurus, i * ruuduSuurus, ruuduSuurus, ruuduSuurus);
                ristkülik[i][j].setFill(Color.BLUE);
                rectToColor.put(50 * i + j, Color.BLUE); // 0 kuni 2499
                ristkülik[i][j].setStroke(Color.BLUE);
                juur.getChildren().add(ristkülik[i][j]);  // ristkülik lisatakse juure alluvaks
            }
        }



        Scene stseen1 = new Scene(juur, 500, 500, Color.SNOW);  // luuakse stseen

        peaLava.setTitle("Must ruut");  // lava tiitelribale pannakse tekst

        peaLava.setScene(stseen1);  // lavale lisatakse stseen

        peaLava.show();  // lava tehakse nähtavaks
    }


    public static void main(String[] args) {
        launch(args);
    }
}
