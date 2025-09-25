package com.example.clienta;
import java.io.Serializable;

public class RouteViewModel implements Serializable{
    String TotalDistance;
    String TotalTime;
    String TotalEle;

    String AverageSpeed;
    String RouteMapId;

    public RouteViewModel(String strRoute){
        this.TotalDistance = strRoute.split(",")[0];
        this.TotalTime =  strRoute.split(",")[1];
        this.AverageSpeed =  strRoute.split(",")[2];
        this.TotalEle =  strRoute.split(",")[3];
        this.RouteMapId =  strRoute.split(",")[4];;
    };

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
