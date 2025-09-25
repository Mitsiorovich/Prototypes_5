import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class RouteAnalyzer {
    ArrayList<Wpt> wptPath;

    public RouteAnalyzer(ArrayList<Wpt> wptPath){
        this.wptPath = wptPath;
    }

    String getTotalTime(){
        Instant start = wptPath.get(0).getTime();
        Instant end = wptPath.get(wptPath.size() - 1).getTime();

        Duration totalTime = Duration.between(start, end);
        long hours = totalTime.toHours();
        long minutes = totalTime.toMinutes() % 60;
        long seconds = totalTime.getSeconds() % 60;

        //System.out.println("Total time of chunk> " +hours + "h " + minutes + "m " + seconds + "s");

        return hours + "h " + minutes + "m " + seconds + "s";
    }

    double getTotalTimeInSeconds() {
        Instant start = wptPath.get(0).getTime();
        Instant end = wptPath.get(wptPath.size() - 1).getTime();

        Duration totalTime = Duration.between(start, end);
        return totalTime.getSeconds();
    }

    double getAverageSpeed() {
        double totalTimeInSeconds = this.getTotalTimeInSeconds();
        double totalDistanceInKilometers = getTotalDistance();
        double averageSpeed = totalDistanceInKilometers / (totalTimeInSeconds / 3600);
        return averageSpeed;
    }

    double getTotalElevation(){
        double sumElo=0d;

        for (int i=0;i<wptPath.size()-1;i++) {
            if (wptPath.get(i).getEle() < wptPath.get(i + 1).getEle()) {
                if (wptPath.get(i + 1).getEle() < 0) {
                    sumElo += Math.abs(wptPath.get(i).getEle()) - Math.abs(wptPath.get(i + 1).getEle());
                    System.out.println("In i+1<0 Elevation is-> " + sumElo);
                } else {
                    sumElo += wptPath.get(i + 1).getEle() - wptPath.get(i).getEle();
                }
            }
        }

        return sumElo;
    }
    double getTotalDistance(){
        double distance = 0.0d,latDistance, lonDistance, a, c, lat2, lat1, lon2, lon1;
        for (int i = 0; i < wptPath.size(); i++) {

            if (wptPath.size() == i + 1){
                break;
            }
            lat2 = wptPath.get(i+1).getLat();
            lat1 = wptPath.get(i).getLat();
            lon2 = wptPath.get(i+1).getLon();
            lon1= wptPath.get(i).getLon();

            latDistance = Math.toRadians(lat2 - lat1);
            lonDistance = Math.toRadians(lon2 - lon1);

            a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

            c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            distance += 6371 /*EARTH_RADIUS*/ * c;
        }

        return distance;
    }
}

