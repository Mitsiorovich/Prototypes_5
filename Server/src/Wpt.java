import java.io.Serializable;
import java.time.Instant;
public class Wpt implements Serializable {

    private float lat;
    private float lon;

    private double ele;
    private Instant time;

    public Wpt(){

    }

    public Wpt(String lat , String lon , String ele , String time){
        this.lat = Float.parseFloat(lat);
        this.lon = Float.parseFloat(lon);
        this.ele = Double.parseDouble(ele);
        this.time = Instant.parse(time);
    }
    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public double getEle() {
        return ele;
    }

    public void setEle(double ele) {
        this.ele = ele;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}


