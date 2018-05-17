package tetrispackage;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class TetrisRectangle {
    private final int ruuduSuurus = 15;
    private final int mitukuubikutLaiuses = TetrisGraafika.getResoWidth() / ruuduSuurus;
    private final int mitukuubikutPikkuses = TetrisGraafika.getResoHeight() / ruuduSuurus;
    private Rectangle ristkülik[][] = new Rectangle[mitukuubikutPikkuses][mitukuubikutLaiuses];

    void fill(Group area) {
        for (int i = 0; i < mitukuubikutPikkuses; i++) {
            for (int j = 0; j < mitukuubikutLaiuses; j++) {
                ristkülik[i][j] = new Rectangle(j * ruuduSuurus, i * ruuduSuurus, ruuduSuurus, ruuduSuurus);
                ristkülik[i][j].setStroke(Color.LIGHTGRAY);
                area.getChildren().add(ristkülik[i][j]);  // ristkülik lisatakse juure alluvaks
            }
        }
    }

    public Rectangle[][] getRistkülik() {
        return ristkülik;
    }
}
