import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainTest extends javafx.application.Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ControlDrone.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Control drone");
        primaryStage.getIcons().add(new Image("/icon_arrionis.png"));
        primaryStage.setScene(new Scene(root, 1080, 720));
        ControllerWithMap controller = loader.getController();
        controller.initData(ControllerWithMap.BASIC_LAT_LONG, true);
        primaryStage.show();
    }
}
