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

    public Pane test;

    @FXML
    public GridPane testpane;

    @FXML
    public AnchorPane anchorPaneTest;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/Configure.fxml"));
        primaryStage.setTitle("Configure");
        primaryStage.setScene(new Scene(root, 1080, 720));
        primaryStage.show();
    }

    public void setStageForThisScene(String title, String resource)
    {
        Stage stage = (Stage) mapAnchor.getScene().getWindow();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource(resource));
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.setTitle(title);

        stage.setScene(new Scene(root, stage.getWidth()-18, stage.getHeight()-47));
        stage.show();
    }

    public void test()
    {
        System.out.println("Anchor pane : " + anchorPaneTest.getWidth() + " - " + anchorPaneTest.getHeight());
        System.out.println("Grid pane : " + testpane.getWidth() + " - " + testpane.getHeight());
    }

    public void Fligth() {
        setStageForThisScene("Flight", "/Flight.fxml");
    }

    public void Configure() {
        setStageForThisScene("Configure", "/Configure.fxml");
    }

    public void Control() {
        setStageForThisScene("Control the drone", "/ControlDrone.fxml");
    }

     public void initialize(){
         mapComponent = new GoogleMapView();
         mapComponent.addMapInitializedListener(this);
         mapComponent.setDisableDoubleClick(true);
         mapComponent.getWebview().getEngine().setOnAlert((WebEvent<String> event) -> {
             //   System.out.println("Event event: " + event);
         });
         System.out.println(mapComponent);
         mapAnchor.setCenter(mapComponent);

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
                System.out.println("Calling showDirections from Java");
                Platform.runLater(() -> mapComponent.getMap().hideDirectionsPane());
            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        });
        t.start();
        //Once the map has been loaded by the Webview, initialize the map details.
        LatLong center = new LatLong(45.961310, -1.396096);

        MapOptions options = new MapOptions();
        options.center(center)
                .zoom(9)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .mapType(MapTypeIdEnum.TERRAIN);

        map = mapComponent.createMap(options);

        map.setHeading(123.2);

        /*Path option*/
        MarkerOptions markerOptions = new MarkerOptions();
        LatLong markerLatLong = new LatLong(45.961310, -1.396096);
        markerOptions.position(markerLatLong)
                .title("My new Marker")
                .icon("mymarker.png")
                .animation(Animation.DROP)
                .visible(true);

        final Marker myMarker = new Marker(markerOptions);

        MarkerOptions markerOptions2 = new MarkerOptions();
        LatLong markerLatLong2 = new LatLong(45.961312, -1.396096);
        markerOptions2.position(markerLatLong2)
                .title("My new Marker")
                .visible(true);

        final Marker myMarker2 = new Marker(markerOptions2);

        map.addMarker(myMarker);
        map.addMarker(myMarker2);
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
                System.out.println(" Elevation on "+ e.getLocation().toString() + " is " + e.getElevation());
            }
        }
    }

    @Override
    public void geocodedResultsReceived(GeocodingResult[] results, GeocoderStatus status) {
        if(status.equals(GeocoderStatus.OK)){
            for(GeocodingResult e : results){
                System.out.println(e.getVariableName());
                System.out.println("GEOCODE: " + e.getFormattedAddress() + "\n" + e.toString());
            }
        }
    }

    @Override
    public void directionsReceived(DirectionsResult results, DirectionStatus status) {
        if(status.equals(DirectionStatus.OK)){
            System.out.println("OK");
        }
    }
}
