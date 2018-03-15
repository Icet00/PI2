import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class ControlDroneController extends ControllerWithMap {

    public void toFligthScene() {
        withParameters = true;
        setStageMapForThisScene("Flight", "/Flight.fxml");
    }

    public void test()
    {

    }
}
