import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.Marker;

// Someone interested in "Hello" events
class EventListenerMarker implements MarkerListener {
    private boolean mapInitialized;

    private boolean dataInitialized;

    private Controller controller;

    public EventListenerMarker(Controller controller)
    {
        this.controller = controller;
        mapInitialized = false;
        dataInitialized = false;
    }

    @Override
    public void mapIsInitialized()
    {
        mapInitialized = true;
        if(this.dataInitialized)
        {
            addAllMarker();
        }

    }

    @Override
    public void dataIsInitialized()
    {
        dataInitialized = true;
        if(this.mapInitialized)
        {
            addAllMarker();
        }
    }

    private void addAllMarker()
    {
        controller.configureMap();
    }
}