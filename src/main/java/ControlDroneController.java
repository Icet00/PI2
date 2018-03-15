public class ControlDroneController extends ControllerWithMap {
    public void toFligthScene() {
        withParameters = true;
        setStageMapForThisScene("Flight", "/Flight.fxml");
    }
}
