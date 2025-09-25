import java.io.Serializable;

public class RouteViewModel implements Serializable {
    String TotalDistance;
    String TotalTime;
    String TotalEle;

    String AverageSpeed;
    String RouteMapId;

    public RouteViewModel(String TotalDistance,
                          String TotalTime,
                          String TotalEle,
                          String AverageSpeed,
                          String RouteMapId
    ){
        this.TotalDistance = TotalDistance;
        this.TotalTime = TotalTime;
        this.AverageSpeed = AverageSpeed;
        this.TotalEle = TotalEle;
        this.RouteMapId = RouteMapId;
    }
}
