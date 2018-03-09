import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.directions.DirectionStatus;
import com.lynden.gmapsfx.service.directions.DirectionsRenderer;
import com.lynden.gmapsfx.service.directions.DirectionsResult;
import com.lynden.gmapsfx.service.directions.DirectionsServiceCallback;
import com.lynden.gmapsfx.service.elevation.ElevationResult;
import com.lynden.gmapsfx.service.elevation.ElevationServiceCallback;
import com.lynden.gmapsfx.service.elevation.ElevationStatus;
import com.lynden.gmapsfx.service.geocoding.GeocoderStatus;
import com.lynden.gmapsfx.service.geocoding.GeocodingResult;
import com.lynden.gmapsfx.service.geocoding.GeocodingServiceCallback;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import com.lynden.gmapsfx.util.MarkerImageFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.scene.web.WebEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class Controller extends Application implements MapComponentInitializedListener,
        ElevationServiceCallback, GeocodingServiceCallback, DirectionsServiceCallback {
    protected GoogleMapView mapComponent;
    protected GoogleMap map;
    protected DirectionsPane directions;

    @FXML
    public BorderPane mapAnchor;

    @FXML
    public MediaView video_front;

    @FXML
    public MediaView video_below;

    @FXML
    public MediaView video_below_thermal;

    @FXML
    public GridPane testpane;

    @FXML
    public AnchorPane anchorPaneTest;

    public Marker marker_start;

    public LatLong lat_long_marker_start;

    public Marker marker_finish;

    public LatLong lat_long_marker_finish;

    public Marker marker_drone;

    public double[] lat_long_drone;

    public LatLong[] line_start_to_finish;

    public boolean withParameters;

    public double[][] array_marker_lat_long;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Configure.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        primaryStage.setTitle("Configure");
        primaryStage.setScene(new Scene(root, 1080, 720));
        primaryStage.show();


    }

    public void setStageForThisScene(String title, String resource)
    {
        Stage stage = (Stage) mapAnchor.getScene().getWindow();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));

        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Controller controller = loader.getController();

        stage.setTitle(title);

        stage.setScene(new Scene(root, stage.getWidth()-18, stage.getHeight()-47));
        controller.initData(controller, array_marker_lat_long, withParameters);
        stage.show();

    }

    public void test()
    {
        System.out.println("Anchor pane : " + anchorPaneTest.getWidth() + " - " + anchorPaneTest.getHeight());
        System.out.println("Grid pane : " + testpane.getWidth() + " - " + testpane.getHeight());
    }

    public void Fligth() {
        withParameters = true;
        setStageForThisScene("Flight", "/Flight.fxml");
    }

    public void Configure() {
        withParameters = false;
        setStageForThisScene("Configure", "/Configure.fxml");
    }



    public void Control() {
        setStageForThisScene("Control the drone", "/ControlDrone.fxml");
    }

    public void initData(Controller controller, double[][] array_marker_lat_long, boolean withParameters)
    {
        controller.array_marker_lat_long = array_marker_lat_long;
        controller.withParameters = withParameters;

    }

    public void addDroneMarkerAndLineStartToFinish()
    {
        //Set drone in gmap
        MarkerOptions markerOptions3 = new MarkerOptions();
        LatLong lat_long = new LatLong(array_marker_lat_long[0][0], array_marker_lat_long[0][1]);
        markerOptions3.position(lat_long)
                .title("Drone marker")
                .icon("marker_drone.png")
                .visible(true);

        marker_drone = new Marker(markerOptions3);
        map.addMarker(marker_drone);

        line_start_to_finish = new LatLong[]{new LatLong(array_marker_lat_long[1][0], array_marker_lat_long[1][1]), new LatLong(array_marker_lat_long[2][0], array_marker_lat_long[2][1])};
        //Line configuration
        MVCArray mvc = new MVCArray(line_start_to_finish);

        PolylineOptions polyOpts = new PolylineOptions()
                .path(mvc)
                .strokeColor("red")
                .strokeWeight(2);

        Polyline line = new Polyline(polyOpts);
        map.addMapShape(line);
    }

     public void initialize(){
         mapComponent = new GoogleMapView();
         mapComponent.addMapInitializedListener(this);
         mapComponent.setDisableDoubleClick(true);
         mapComponent.getWebview().getEngine().setOnAlert((WebEvent<String> event) -> {
             //System.out.println("Event event: " + event);
         });
         mapAnchor.setCenter(mapComponent);

         mapComponent.addMapReadyListener(() -> {
             if(this.withParameters)
             {
                 Platform.runLater(this::addDroneMarkerAndLineStartToFinish);
             }

         });

         MediaPlayer player = new MediaPlayer( new Media(getClass().getResource("/video_all.mp4").toExternalForm()));

         video_front.setMediaPlayer(player);
         video_below.setMediaPlayer(player);
         video_below_thermal.setMediaPlayer(player);

         video_front.toFront();
         video_below.toFront();
         video_below_thermal.toFront();

         player.play();
    }

    @Override
    public void mapInitialized() {
        Thread t = new Thread( () -> {
            try {
                Thread.sleep(5000);
                //System.out.println("Calling showDirections from Java");
                Platform.runLater(() -> mapComponent.getMap().hideDirectionsPane());
            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        });
        t.start();

        array_marker_lat_long = new double[][] { new double[]{44.500000, -2.500000}, new double[]{44.000000, -3.000000}, new double[]{45.000000, -2.000000}};

        //Once the map has been loaded by the Webview, initialize the map details.
        LatLong center = new LatLong(array_marker_lat_long[0][0], array_marker_lat_long[0][1]);

        MapOptions options = new MapOptions();
        options.center(center)
                .zoom(8)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .mapType(MapTypeIdEnum.TERRAIN);

        map = mapComponent.createMap(options);



        /*Path option*/
        MarkerOptions markerOptions = new MarkerOptions();
        lat_long_marker_start = new LatLong(array_marker_lat_long[1][0], array_marker_lat_long[1][1]);
        markerOptions.position(lat_long_marker_start)
                .title("Finish marker")
                .icon("marker_finish.png")
                .visible(true);

        marker_finish = new Marker(markerOptions);

        MarkerOptions markerOptions2 = new MarkerOptions();
        lat_long_marker_finish = new LatLong(array_marker_lat_long[2][0], array_marker_lat_long[2][1]);
        markerOptions2.position(lat_long_marker_finish)
                .title("Start marker")
                .icon("marker_start.png")
                .visible(true);

        marker_start = new Marker(markerOptions2);

        map.addMarker(marker_start);
        map.addMarker(marker_finish);


    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "true");
        launch(args);
    }

    @Override
    public void elevationsReceived(ElevationResult[] results, ElevationStatus status) {
        if(status.equals(ElevationStatus.OK)){
            for(ElevationResult e : results){
                //System.out.println(" Elevation on "+ e.getLocation().toString() + " is " + e.getElevation());
            }
        }
    }

    @Override
    public void geocodedResultsReceived(GeocodingResult[] results, GeocoderStatus status) {
        if(status.equals(GeocoderStatus.OK)){
            for(GeocodingResult e : results){
                //System.out.println(e.getVariableName());
                //System.out.println("GEOCODE: " + e.getFormattedAddress() + "\n" + e.toString());
            }
        }
    }

    @Override
    public void directionsReceived(DirectionsResult results, DirectionStatus status) {
        if(status.equals(DirectionStatus.OK)){
            //System.out.println("OK");
        }
    }
}
