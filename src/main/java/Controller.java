import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.directions.DirectionStatus;
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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.scene.web.WebEvent;
import javafx.stage.Stage;

import java.awt.event.ActionListener;
import java.io.Console;
import java.io.IOException;
import java.util.*;

import netscape.javascript.JSObject;

public class Controller extends Application implements MapComponentInitializedListener, ElevationServiceCallback, GeocodingServiceCallback, DirectionsServiceCallback {

    double [][] BASIC_LAT_LONG = new double[][] { new double[]{44.500000, -2.500000}, new double[]{44.000000, -3.000000}, new double[]{45.000000, -2.000000}};

    protected GoogleMapView mapComponent;
    protected GoogleMap map;
    protected DirectionsPane directions;

    public enum MARKER {
        DRONE(0), START(1), FINISH(2);
        private int value;
        private static Map map = new HashMap<>();

        MARKER(int value) {
            this.value = value;
        }

        static {
            for (MARKER marker : MARKER.values()) {
                map.put(marker.value, marker);
            }
        }

        public static MARKER valueOf(int pageType) {
            return (MARKER) map.get(pageType);
        }

        public int getValue() {
            return value;
        }
    }

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

    @FXML
    public Text centerText;

    public boolean withParameters;

    public boolean addMarkerStart = true;

    public boolean addMarkerFinish = true;

    public Marker markerStart;

    public Marker markerFinish;

    public double[][] array_marker_lat_long;

    public String[] PATH_TO_IMAGE = new String[] {"marker_drone.png","marker_start.png","marker_finish.png"};

    public String[] TITLE_MARKER = new String[] {"marker drone","marker start","marker finish"};

    EventHandlerMarker eventHandlerMarker = new EventHandlerMarker();
    EventListenerMarker eventListenerMarker = new EventListenerMarker(this);

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Configure.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Configure");
        primaryStage.setScene(new Scene(root, 1080, 720));
        Controller controller = loader.getController();
        controller.initData(new double[][]{{},{},{}}, false);
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
        controller.initData(array_marker_lat_long, withParameters);
        stage.show();

    }

    public void test()
    {
        System.out.println("Anchor pane : " + anchorPaneTest.getWidth() + " - " + anchorPaneTest.getHeight());
        System.out.println("Grid pane : " + testpane.getWidth() + " - " + testpane.getHeight());
    }

    public void Fligth() {
        withParameters = true;
        if(getValueInArray(MARKER.START.getValue()).length != 0 && getValueInArray(MARKER.FINISH.getValue()).length != 0)
        {
            double middle_x = (getValueInArray(MARKER.START.getValue())[0] + getValueInArray(MARKER.FINISH.getValue())[0]) / 2;
            double middle_y = (getValueInArray(MARKER.START.getValue())[1] + getValueInArray(MARKER.FINISH.getValue())[1]) / 2;
            setValueInArray(MARKER.DRONE.getValue(), middle_x, middle_y);
            setStageForThisScene("Flight", "/Flight.fxml");
        }

    }

    public void Configure() {
        withParameters = false;
        setStageForThisScene("Configure", "/Configure.fxml");
    }



    public void Control() {
        setStageForThisScene("Control the drone", "/ControlDrone.fxml");
    }

    public void initData(double[][] array_marker_lat_long, boolean withParameters)
    {
        this.array_marker_lat_long = array_marker_lat_long;
        this.withParameters = withParameters;
        eventHandlerMarker.dataInitialized();
    }

    public void lineStartToFinish()
    {
        LatLong[] line_start_to_finish = new LatLong[]{new LatLong(array_marker_lat_long[1][0], array_marker_lat_long[1][1]), new LatLong(array_marker_lat_long[2][0], array_marker_lat_long[2][1])};
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
         array_marker_lat_long = new double[][]{{},{},{}};

         initializeMap();

         MediaPlayer player = new MediaPlayer( new Media(getClass().getResource("/video_all.mp4").toExternalForm()));

         video_front.setMediaPlayer(player);
         video_below.setMediaPlayer(player);
         video_below_thermal.setMediaPlayer(player);

         video_front.toFront();
         video_below.toFront();
         video_below_thermal.toFront();

         player.play();
    }

    public void initializeMap()
    {
        eventHandlerMarker.addListener(eventListenerMarker);
        mapComponent = new GoogleMapView();
        mapComponent.addMapInitializedListener(this);
        mapComponent.setDisableDoubleClick(true);
        mapComponent.getWebview().getEngine().setOnAlert((WebEvent<String> event) -> {
            //System.out.println("Event event: " + event);
        });
        mapAnchor.setCenter(mapComponent);
    }



    public void updateMarker(LatLong coordinate)
    {
        if(addMarkerFinish)
        {
            addMarkerFinish = false;
            boolean toAdd = (markerFinish == null);
            setValueInArray(MARKER.FINISH.getValue(), coordinate.getLatitude(), coordinate.getLongitude());
            markerFinish = createMarkerIndex(MARKER.FINISH.getValue());
            System.out.println(toAdd);
            if(toAdd)
            {
                map.addMarker(markerFinish);
            }

        }
        else if(addMarkerStart)
        {

            addMarkerStart = false;
            boolean toAdd = (markerStart == null);
            setValueInArray(MARKER.START.getValue(), coordinate.getLatitude(), coordinate.getLongitude());
            markerStart = createMarkerIndex(MARKER.START.getValue());
            if(toAdd)
            {
                map.addMarker(markerStart);
            }
        }
    }

    private void setValueInArray(int i, double x, double y)
    {
        array_marker_lat_long[i] = new double[]{x,y};
    }

    private double[] getValueInArray(int i)
    {
        return array_marker_lat_long[i];
    }

    private void checkCenter(LatLong center) {
//        System.out.println("Testing fromLatLngToPoint using: " + center);
//        Point2D p = map.fromLatLngToPoint(center);
//        System.out.println("Testing fromLatLngToPoint result: " + p);
//        System.out.println("Testing fromLatLngToPoint expected: " + mapComponent.getWidth()/2 + ", " + mapComponent.getHeight()/2);
    }

    @Override
    public void mapInitialized() {
        //Once the map has been loaded by the Webview, initialize the map details.
        LatLong center = new LatLong(BASIC_LAT_LONG[0][0], BASIC_LAT_LONG[0][1]);

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

        //Click event listener
        map.addUIEventHandler(UIEventType.click, (JSObject obj) -> {
            LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
            System.out.println("LatLong: lat: " + ll.getLatitude() + " lng: " + ll.getLongitude());
            updateMarker(ll);

        });



        eventHandlerMarker.mapInitialized();
    }

    public void configureMap()
    {
        if(withParameters)
        {
            addMarkerIndex(MARKER.START.getValue());
            addMarkerIndex(MARKER.FINISH.getValue());
            System.out.println("test");
            addMarkerIndex(MARKER.DRONE.getValue());
            System.out.println("test 2");
            lineStartToFinish();
        }
        else
        {
            //Set center text refresh
            map.centerProperty().addListener((ObservableValue<? extends LatLong> obs, LatLong o, LatLong n) -> {
                centerText.setText("Center :\nLatitude : " + n.getLatitude() + "\nLongitude" + n.getLongitude());
            });
        }
    }

    public void addMarkerIndex(int i)
    {
        map.addMarker(createMarkerIndex(i));
    }

    public Marker createMarkerIndex(int i)
    {
        return createMarker(TITLE_MARKER[i], PATH_TO_IMAGE[i], array_marker_lat_long[i][0], array_marker_lat_long[i][1]);
    }

    public Marker createMarker(String title, String path, double x, double y)
    {
        MarkerOptions markerOptions = new MarkerOptions();
        LatLong lat_long_marker_start = new LatLong(x, y);
        markerOptions.position(lat_long_marker_start)
                .title(title)
                .icon(path)
                .visible(true);
        return new Marker(markerOptions);
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
