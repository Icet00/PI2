import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.Marker;

import java.util.ArrayList;
import java.util.List;

public class EventHandlerMarker {
    private List<MarkerListener> listeners = new ArrayList<MarkerListener>();

    public void addListener(MarkerListener toAdd) {
        listeners.add(toAdd);
    }

    public void mapInitialized() {
        // Notify everybody that may be interested.
        for (MarkerListener hl : listeners)
            hl.mapIsInitialized();
    }

    public void dataInitialized() {
        // Notify everybody that may be interested.
        for (MarkerListener hl : listeners)
            hl.dataIsInitialized();
    }
}