import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ControllerWithMap implements MapComponentInitializedListener {
    private double [][] BASIC_LAT_LONG = new double[][] { new double[]{44.500001, -2.500001}, new double[]{44.000000, -3.000000}, new double[]{45.000000, -2.000000}};

    private String[] PATH_TO_IMAGE = new String[] {"marker_drone.png","marker_start.png","marker_finish.png"};

    private String[] TITLE_MARKER = new String[] {"marker drone","marker start","marker finish"};

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
            for (ControllerWithMap.MARKER marker : ControllerWithMap.MARKER.values()) {
                map.put(marker.value, marker);
            }
        }

        public static Controller.MARKER valueOf(int pageType) {
            return (Controller.MARKER) map.get(pageType);
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
    public Text centerText;

    @FXML
    public GridPane resultCommunicationSensor;

    @FXML
    public TextField start_longitude;

    @FXML
    public TextField start_latitude;

    @FXML
    public TextField finish_longitude;

    @FXML
    public TextField finish_latitude;

    public boolean withParameters;

    private boolean addMarkerStart;

    private boolean addMarkerFinish;

    private Marker markerStart;

    private Marker markerFinish;

    private double[][] array_marker_lat_long;

    private EventHandlerMarker eventHandlerMarker = new EventHandlerMarker();
    private EventListenerMarker eventListenerMarker = new EventListenerMarker(this);

    public void setStageMapForThisScene(String title, String resource)
    {
        Stage stage = (Stage) mapAnchor.getScene().getWindow();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));

        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ControllerWithMap controller = loader.getController();
        stage.getIcons().add(new Image("/icon_arrionis.png"));
        stage.setTitle(title);

        stage.setScene(new Scene(root, stage.getWidth()-18, stage.getHeight()-47));
        controller.initData(array_marker_lat_long, withParameters);
        stage.show();
    }

    public void initData(double[][] array_marker_lat_long, boolean withParameters)
    {
        this.array_marker_lat_long = array_marker_lat_long;
        this.withParameters = withParameters;
        eventHandlerMarker.dataInitialized();
    }

    private void lineStartToFinish()
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
        addMarkerFinish = false;
        addMarkerStart = false;
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
        mapAnchor.setCenter(mapComponent);
    }

    public void authorizeAddFromMapClickMarkerStart()
    {
        addMarkerFinish = false;
        addMarkerStart = true;
    }

    public void authorizeAddFromMapClickMarkerFinish()
    {
        addMarkerStart = false;
        addMarkerFinish = true;
    }

    public void updateMarker(LatLong coordinate)
    {
        if(addMarkerFinish)
        {
            updateFinishMarker(coordinate);
        }
        else if(addMarkerStart)
        {

            updateStartMarker(coordinate);
        }
    }

    public void updateFinishMarker(LatLong coordinate)
    {
        addMarkerFinish = false;
        boolean toAddFirstTime = (markerFinish == null);
        setValueInArray(Controller.MARKER.FINISH.getValue(), coordinate.getLatitude(), coordinate.getLongitude());
        if(!toAddFirstTime)
        {
            map.removeMarker(markerFinish);
        }
        markerFinish = createMarkerIndex(Controller.MARKER.FINISH.getValue());
        map.addMarker(markerFinish);
        start_latitude.setText(Double.toString(coordinate.getLatitude()));
        start_longitude.setText(Double.toString(coordinate.getLongitude()));
    }

    public void updateStartMarker(LatLong coordinate)
    {
        addMarkerStart = false;
        boolean toAddFirstTime = (markerStart == null);
        setValueInArray(Controller.MARKER.START.getValue(), coordinate.getLatitude(), coordinate.getLongitude());
        if(!toAddFirstTime)
        {
            map.removeMarker(markerStart);
        }
        markerStart = createMarkerIndex(Controller.MARKER.START.getValue());
        map.addMarker(markerStart);
        finish_latitude.setText(Double.toString(coordinate.getLatitude()));
        finish_longitude.setText(Double.toString(coordinate.getLongitude()));
    }

    public void setValueInArray(int i, double x, double y)
    {
        array_marker_lat_long[i] = new double[]{x,y};
    }

    public double[] getValueInArray(int i)
    {
        return array_marker_lat_long[i];
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
            updateMarker(ll);

        });
        eventHandlerMarker.mapInitialized();
    }

    public void configureMap()
    {
        if(withParameters)
        {
            addMarkerIndex(Controller.MARKER.START.getValue());
            addMarkerIndex(Controller.MARKER.FINISH.getValue());
            addMarkerIndex(Controller.MARKER.DRONE.getValue());
            lineStartToFinish();
            map.setCenter(new LatLong(getValueInArray(0)[0], getValueInArray(0)[1]));
        }
        else
        {
            //Set center text refresh
            map.centerProperty().addListener((ObservableValue<? extends LatLong> obs, LatLong o, LatLong n) -> {
                centerText.setText("Center :\nLatitude : " + n.getLatitude() + "\nLongitude" + n.getLongitude());
            });
            LatLong center = new LatLong(BASIC_LAT_LONG[0][0], BASIC_LAT_LONG[0][1]);
            centerText.setText("Center :\nLatitude : " + center.getLatitude() + "\nLongitude : " + center.getLongitude());
        }
    }

    public void addStartMarkerFromInput()
    {
        addMarkerFromInput(Controller.MARKER.START.getValue(), start_longitude.getText(), start_latitude.getText());
    }

    public void addFinishMarkerFromInput()
    {
        System.out.println(finish_longitude.getText() + " - " + finish_latitude.getText());
        addMarkerFromInput(Controller.MARKER.FINISH.getValue(),finish_longitude.getText(), finish_latitude.getText());
    }

    private void addMarkerFromInput(int index, String latitude, String longitude)
    {
        try
        {
            double x = Double.parseDouble(latitude);
            double y = Double.parseDouble(longitude);
            setValueInArray(index, x, y);
            LatLong coordinate = new LatLong(x,y);
            if(index == Controller.MARKER.START.getValue())
            {
                updateStartMarker(coordinate);
            }
            if(index == Controller.MARKER.FINISH.getValue())
            {
                updateFinishMarker(coordinate);
            }
        }
        catch (Exception e)
        {
            System.out.println("not a number");
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
}
