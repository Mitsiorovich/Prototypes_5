import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class Route implements Serializable {

    String routeMapId;
    String routeId;
    ArrayList<Wpt> waypointPath;

    public Route(ArrayList<Wpt> wptPath){
        this.waypointPath = wptPath;
    }
}

