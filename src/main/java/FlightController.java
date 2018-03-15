public class FlightController extends ControllerWithMap {
    public void toControlScene() {
        this.withParameters = true;
        setStageMapForThisScene("Control the drone", "/ControlDrone.fxml");
    }

    public void toConfigureScene() {
        withParameters = false;
        setStageMapForThisScene("Configure", "/Configure.fxml");
    }
}
