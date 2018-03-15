import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;

public class ConfigureController extends ControllerWithMap {
    public double TO_MIDDLE_PICTURE_DIFFERENCE_X = -0.23;

    public int DELAY_ANIMATION_FADE = 5000;

    @FXML
    public GridPane anchorPaneTest;

    @FXML
    public Text apply_incorrect;

    @FXML
    public TabPane tab;

    boolean[] check_finished_and_ok = new boolean[]{false, false};

    public void test()
    {
        System.out.println("Anchor pane : " + anchorPaneTest.getWidth() + " - " + anchorPaneTest.getHeight());
        //System.out.println("Grid pane : " + testpane.getWidth() + " - " + testpane.getHeight());
        /*Stage stage = new Stage();
        System.out.println("Anchor pane : " + anchorPaneTest.getWidth() + " - " + anchorPaneTest.getHeight());
        System.out.println("Grid pane : " + testpane.getWidth() + " - " + testpane.getHeight());
        StlMeshImporter stlImporter = new StlMeshImporter();

        try {
            stlImporter.read(this.getClass().getResource("drone.stl"));
        }
        catch (ImportException e) {
            e.printStackTrace();
            return;
        }
        TriangleMesh mesh = stlImporter.getImport();
        stlImporter.close();
        MeshView meshView =new MeshView(mesh);
        Group root = new Group(meshView);
        Scene scene = new Scene(root, 1024, 800, true);
        Camera camera = new PerspectiveCamera();
        scene.setCamera(camera);
        stage.setScene(scene);
        stage.show();
        System.out.println("mesh: "+meshView.getBoundsInLocal().toString());
        double max = Math.max(meshView.getBoundsInLocal().getWidth(),
                Math.max(meshView.getBoundsInLocal().getHeight(),
                        meshView.getBoundsInLocal().getDepth()));
        camera.setTranslateZ(-3*max);
        meshView.setScaleX(3);
        meshView.setScaleY(3);
        meshView.setScaleZ(3);*/
    }

    boolean shouldShowResult = true;

    public void showResultOnce()
    {
        if(shouldShowResult)
        {
            showResult();
            shouldShowResult = false;
        }
    }

    public void showResult()
    {
        Text[][] tmp_array = new Text[][]{
                {new Text("GPS"), new Text("Radio"), new Text("Camera")},
                {new Text("Gyroscope"), new Text("Captor laser"), new Text("Thermometer"), new Text("Speaker")}
        };

        ImageView tmp_loading = new ImageView(new Image("/loading.gif", 32, 32, false, false));
        resultCommunicationSensor.add(tmp_loading, 1,0);
        ImageView tmp_loading_2 = new ImageView(new Image("/loading.gif", 32, 32, false, false));
        resultCommunicationSensor.add(tmp_loading_2, 3,0);
        ImageView[] img_loading = new ImageView[]{tmp_loading, tmp_loading_2};

        ImageView[][] tmp_img_array = new ImageView[][]{new ImageView[tmp_array[0].length], new ImageView[tmp_array[1].length]};
        for(int i = 0; i < tmp_array.length;i++)
        {
            for(int j =0 ; j <  tmp_array[i].length;j++)
            {
                //To font size 24
                tmp_array[i][j].setFont(new Font(tmp_array[i][j].getFont().getName(), 24));
                //Create the incorrect image
                ImageView tmp_img = new ImageView(new Image("/incorrect.png", 32, 32, false, false));
                //Add the image and the text
                resultCommunicationSensor.add(tmp_array[i][j], 2*i,j+1);
                resultCommunicationSensor.add(tmp_img, (2*i)+1,j+1);
                //Store the image in the array
                tmp_img_array[i][j]=tmp_img;
            }
        }

        timerChangeIncorrectToCorrect(0, 100, 1000, tmp_img_array, img_loading);
        timerChangeIncorrectToCorrect(1, 1500, 800, tmp_img_array, img_loading);

    }
    int[] interval = new int[2];

    public void timerChangeIncorrectToCorrect(int index, int delay, int period, ImageView[][] tmp_img_array, ImageView[] img_loading)
    {
        Timer timer = new Timer();
        interval[index] = 0;
        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                if (interval[index]+1 >= tmp_img_array[index].length)
                {
                    img_loading[index].setImage(new Image("/correct.png", 32, 32, false, false));
                    check_finished_and_ok[index] = true;
                    timer.cancel();
                }
                tmp_img_array[index][interval[index]].setImage(new Image("/correct.png", 32, 32, false, false));
                interval[index] = interval[index] +1;
            }
        }, delay, period);
    }

    public void toFligthScene() {
        withParameters = true;
        boolean correct_check_drone = false;
        if(check_finished_and_ok[0] && check_finished_and_ok[1])
        {
            correct_check_drone = true;
        }
        boolean correct_marker = false;
        if(getValueInArray(ControllerWithMap.MARKER.START.getValue()).length != 0 && getValueInArray(ControllerWithMap.MARKER.FINISH.getValue()).length != 0)
        {
            correct_marker = true;
        }

        if(correct_marker && correct_check_drone)
        {
            double middle_x = (getValueInArray(ControllerWithMap.MARKER.START.getValue())[0] + getValueInArray(ControllerWithMap.MARKER.FINISH.getValue())[0]) / 2;
            double middle_y = (getValueInArray(ControllerWithMap.MARKER.START.getValue())[1] + getValueInArray(ControllerWithMap.MARKER.FINISH.getValue())[1]) / 2;
            setValueInArray(ControllerWithMap.MARKER.DRONE.getValue(), middle_x+ TO_MIDDLE_PICTURE_DIFFERENCE_X, middle_y);
            setStageMapForThisScene("Flight", "/Flight.fxml");
        }
        else
        {
            String message_to_return = "";
            if(!correct_marker)
            {
                message_to_return += "Incorrect marker";
                if(!correct_check_drone)
                {
                    message_to_return+=" and ";
                }
            }
            if(!correct_check_drone)
            {
                message_to_return += "Incorrect verification of the drone";
                if(correct_marker)
                {
                    tab.getSelectionModel().select(1);
                }
            }
            apply_incorrect.setText(message_to_return);
            FadeTransition ft = new FadeTransition(Duration.millis(DELAY_ANIMATION_FADE), apply_incorrect);
            ft.setFromValue(1.0);
            ft.setToValue(0);
            ft.setCycleCount(1);
            ft.setAutoReverse(false);
            ft.play();
        }
    }

}
