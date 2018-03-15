import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Application extends javafx.application.Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Configure.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Configure");
        primaryStage.getIcons().add(new Image("/icon_arrionis.png"));
        primaryStage.setScene(new Scene(root, 1080, 720));
        ConfigureController controller = loader.getController();
        controller.initData(new double[][]{{},{},{}}, false);
        primaryStage.show();
    }
}
