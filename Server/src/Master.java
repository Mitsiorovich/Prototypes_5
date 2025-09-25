import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.time.Duration;


import java.util.*;
import java.util.stream.Collectors;

public class Master {

    static HashMap<String,ArrayList<UserStat>> userStats = new HashMap<String,ArrayList<UserStat> >();

    static HashMap<String,UserStat> leaderBoard = new HashMap<String,UserStat>();

    static UserStat globalTotal = new UserStat("0h 0m 0s", 0 , 0);

    static UserStat globalAverage = new UserStat("0h 0m 0s", 0 , 0);

    public double findPercentageOfDiff(double userAvg, double globalAvg){

        double difference = globalAvg - userAvg;//5 - 8
        return Math.abs((difference) / globalAvg) * 100; // 60%
    };

    public String belowOrAbove(double number1, double number2){
        return number1 < number2 ? "below" : "above";
    }

    private String twoDecimalPlacesStr(double number) {
        return String.valueOf(Math.round(number * 100.0) / 100.0);
    }
    public void getPersonalStatisics(String userName , ObjectInputStream in, ObjectOutputStream out){
        try {
            ArrayList<UserStat> allStatsForThisUser = userStats.get(userName);

            UserStat thisUsersAverage = leaderBoard.get(userName);

            String resultToString ="";


            if(thisUsersAverage != null){
                double percentageDiffEle = findPercentageOfDiff(thisUsersAverage.userAverageElevation,globalAverage.userAverageElevation);
                double percentageDiffDistance = findPercentageOfDiff(thisUsersAverage.userAverageDistance, globalAverage.userAverageDistance);

                String msg = belowOrAbove(thisUsersAverage.userAverageElevation,globalAverage.userAverageElevation);
                String msgEleDiff = "You are " + msg +" by " + twoDecimalPlacesStr(percentageDiffEle) + "% of the average user's elevation.";

                msg = belowOrAbove(thisUsersAverage.userAverageDistance,globalAverage.userAverageDistance);
                String msgDistDiff = "You are " + msg +" by " + twoDecimalPlacesStr(percentageDiffDistance) + "% of the average user's distance.";

                double userAvgDurationInSeconds = convertToDuration(thisUsersAverage.userAverageExerciseDuration);
                double globalAvgDurationInSeconds = convertToDuration(globalAverage.userAverageExerciseDuration);
                msg = belowOrAbove(userAvgDurationInSeconds,globalAvgDurationInSeconds);
                double percentageDiffDuration = findPercentageOfDiff(userAvgDurationInSeconds, globalAvgDurationInSeconds);
                String msgTimeDiff = "You are " + msg +" by " + twoDecimalPlacesStr(percentageDiffDuration) + "% of the average user's duration of exercise.";

                String totalExerciseTime = "0h 0m 0s";
                double totalElevation = 0;
                double totalDistance = 0;

                for (UserStat u: allStatsForThisUser) {
                    totalExerciseTime = Master.addTimeSpans(totalExerciseTime, u.userAverageExerciseDuration);
                    totalElevation += u.userAverageElevation;
                    totalDistance += u.userAverageDistance;
                }

                resultToString = totalExerciseTime + "," + totalElevation +
                        "," + totalDistance + "," + msgEleDiff + "," + msgDistDiff + "," + msgTimeDiff;
            }else{
                resultToString = "x,x,x,x,x," + "No data for this user!";
            }


            out.writeObject(resultToString);/// returns statistics to client
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public  void masterApiInit(Document doc, ObjectInputStream in, ObjectOutputStream out) {
        synchronized (Server.workerConnections){

            System.out.println("Thread " + Thread.currentThread().getId() + " started communication with master");

            HashMap<String , Route> map = MapData(doc);

            /// takes care of the iteration inside worker Connections
            // which should be identical with the number of chunks
            // we will round-robin give each chunk to every worker
            try {
                int holder = 0;
                int currentWorkerChunkIndex = 0;
                Wpt firstWayPointOfLastSubroute = new Wpt();
                int lastChunkCount = map.entrySet().size() - 1;
                for (Map.Entry<String, Route> entry : map.entrySet()) {
                    String routeMapId = entry.getKey();
                    String creator = routeMapId.split("_")[0];

                    Route wptPath = map.get(creator + "_" + lastChunkCount);
                    wptPath.routeMapId = creator + "_" + lastChunkCount;

                    while(!Server.isReady){
                        try {
                            System.out.println("Worker thread " + Thread.currentThread().getId() + " is waiting");
                            Server.workerConnections.wait(); // Wait until isReady is true
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Worker w = Server.workerConnections.get(currentWorkerChunkIndex);

                    if(Server.numberOfWorkersExpected > 1 && currentWorkerChunkIndex == 0){
                        firstWayPointOfLastSubroute = wptPath.waypointPath.get(0);
                    }

                    if(currentWorkerChunkIndex - 1 == holder){
                        wptPath.waypointPath.add(firstWayPointOfLastSubroute);
                        holder++;
                        firstWayPointOfLastSubroute = wptPath.waypointPath.get(0);
                        //System.out.println("i added the first wpt of the last path to the end of the previous");
                    }
                    w.assignRoute(wptPath, routeMapId);
                    w.start();

                    currentWorkerChunkIndex++;
                    lastChunkCount--;
                }

                ArrayList<RouteViewModel> resultList = new ArrayList<RouteViewModel>();


                for (int i = 0; i < Server.numberOfWorkersExpected; i++){
                    Worker tempWorker = Server.workerConnections.get(i);
                    tempWorker.join();
                    RouteViewModel rvm = tempWorker.getResultFromWorkers();
                    resultList.add(rvm);

                }


                ///reduce
                RouteViewModel finalResult = ReduceData(resultList);

                String userName = finalResult.RouteMapId.split("_")[0];


                ArrayList<UserStat> us = userStats.get(userName);

                UserStat userOverallStat = leaderBoard.get(userName);

                if(us != null){
                    us.add(new UserStat(
                            finalResult.TotalTime,
                            Double.valueOf(finalResult.TotalEle),
                            Double.valueOf(finalResult.TotalDistance)
                    ));
                }else{
                    ArrayList<UserStat> newArr = new ArrayList<UserStat>();
                    UserStat newUserStat = new UserStat(
                            finalResult.TotalTime,
                            Double.valueOf(finalResult.TotalEle),
                            Double.valueOf(finalResult.TotalDistance));

                    newArr.add(newUserStat);

                    if(userOverallStat == null){
                        leaderBoard.put(userName, newUserStat);
                    }

                    userStats.put(userName, newArr);
                }

                int count = userStats.get(userName).size();
                double totalDistance = 0d,totalElevation = 0d;
                ArrayList<String> durations = new ArrayList<String>();

                for (UserStat stat : userStats.get(userName)) {
                    durations.add(stat.userAverageExerciseDuration);
                    totalDistance += stat.userAverageDistance;
                    totalElevation += stat.userAverageElevation;
                }

                UserStat newOverallUserStat = new UserStat(
                        UserStat.calculateAverageTimeSpan(durations, count),
                        totalElevation/count,
                        totalDistance/count);

                leaderBoard.put(userName, newOverallUserStat);/// adding a new overall user stat


                ///updating the global total
                globalTotal.userAverageExerciseDuration = addTimeSpans(globalTotal.userAverageExerciseDuration, newOverallUserStat.userAverageExerciseDuration);/// IMPORTANT ITS NOT AVERAGE IS TOTAL
                globalTotal.userAverageDistance += newOverallUserStat.userAverageDistance;/// IMPORTANT ITS NOT AVERAGE IS TOTAL
                globalTotal.userAverageElevation += newOverallUserStat.userAverageElevation;/// IMPORTANT ITS NOT AVERAGE IS TOTAL

                ///updating the global average
                ArrayList<String> averageDurations = new ArrayList<String>();
                for (Map.Entry<String, UserStat> entry : leaderBoard.entrySet()) {
                    String key = entry.getKey();
                    UserStat value = entry.getValue();
                    averageDurations.add(value.userAverageExerciseDuration);
                }

                globalAverage.userAverageExerciseDuration = UserStat.calculateAverageTimeSpan(averageDurations, userStats.size());//mesos oros twn meswn orwn
                globalAverage.userAverageDistance = globalTotal.userAverageDistance/userStats.size();/// IMPORTANT ITS NOT AVERAGE IS TOTAL
                globalAverage.userAverageElevation = globalTotal.userAverageElevation/userStats.size();/// IMPORTANT ITS NOT AVERAGE IS TOTAL


                System.out.println("Server: received the results from worker!");

                String resultToString = finalResult.TotalDistance + "," + finalResult.TotalTime +
                        "," + finalResult.AverageSpeed + "," + finalResult.TotalEle + "," + finalResult.RouteMapId;

                out.writeObject(resultToString);
                out.flush();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    in.close();
                    out.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }



            System.out.println("Client served!");

            for (int i = 0; i <Server.numberOfWorkersExpected; i++) {

                Server.workerConnections.remove(0);
                Server.countWorkersConnected--;
            }

            Server.isReady = false;

            System.out.println("Thread " + Thread.currentThread().getId() + " is done communicating with master");
        }


    }

    public static HashMap<String, Route> MapData(Document doc){
        short numberOfWorkers = Server.numberOfWorkersExpected;
        int chunkCount = 0;

        NodeList nodeList = doc.getElementsByTagName("wpt");
        HashMap<String, Route> subRouteMap = new HashMap<String, Route>();

        int chunkSize = nodeList.getLength()/numberOfWorkers;
        Route subRoute = new Route(new ArrayList<Wpt>());
        String creator = doc.getDocumentElement().getAttribute("creator");
        subRoute.routeId = String.valueOf(chunkCount);

        for (int i = 0; i < nodeList.getLength(); i++) {

            if(i < chunkSize){
                Node node = nodeList.item(i);
                Split(subRoute.waypointPath, node);//Adds wpt obj to path
                if(i == chunkSize - 1){
                    subRouteMap.put(creator + "_" + subRoute.routeId, subRoute);/// puts list of way points at map
                }
            }else{
                creator = doc.getDocumentElement().getAttribute("creator");
                Node node = nodeList.item(i);
                Split(subRoute.waypointPath, node);//Adds wpt obj to path
                subRoute = new Route(new ArrayList<Wpt>());///creates new subroute
                chunkCount++;
                subRoute.routeId = String.valueOf(chunkCount);
                chunkSize += chunkSize;
            }
        }

        return subRouteMap;
    }

    public static RouteViewModel ReduceData(ArrayList<RouteViewModel> rvmList){
        double totalDistance = 0d, totalEle  = 0d, averageSpeed = 0d;
        String totalTime = "0h 0m 0s";
        String routeId = rvmList.get(0).RouteMapId;
        String routeMapId = rvmList.get(0).RouteMapId;

        for (RouteViewModel rvm : rvmList) {
            totalDistance+= Double.valueOf(rvm.TotalDistance);
            totalTime = Master.addTimeSpans(totalTime, rvm.TotalTime);
            totalEle+= Double.valueOf(rvm.TotalEle);
            averageSpeed += Double.valueOf(rvm.AverageSpeed);
        }

        averageSpeed = averageSpeed/rvmList.size();

        RouteViewModel merged = new RouteViewModel(
                String.valueOf(totalDistance),
                String.valueOf(totalTime),
                String.valueOf(totalEle),
                String.valueOf(averageSpeed),
                routeId
        );


        return  merged;
    }
    public static void Split(ArrayList<Wpt> waypointPath, Node node){
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            Wpt waypoint = new Wpt(
                    element.getAttribute("lat"),
                    element.getAttribute("lon"),
                    element.getElementsByTagName("ele").item(0).getTextContent(),
                    element.getElementsByTagName("time").item(0).getTextContent());
            waypointPath.add(waypoint);///constructing the route waypoint by waypoint
        }
    }

    public static String addTimeSpans(String timeSpan1, String timeSpan2) {
        // parse the first time span
        String[] components1 = timeSpan1.split("\\s+");
        int hours1 = Integer.parseInt(components1[0].replace("h", ""));
        int minutes1 = Integer.parseInt(components1[1].replace("m", ""));
        int seconds1 = Integer.parseInt(components1[2].replace("s", ""));
        Duration duration1 = Duration.ofHours(hours1).plusMinutes(minutes1).plusSeconds(seconds1);

        // parse the second time span
        String[] components2 = timeSpan2.split("\\s+");
        int hours2 = Integer.parseInt(components2[0].replace("h", ""));
        int minutes2 = Integer.parseInt(components2[1].replace("m", ""));
        int seconds2 = Integer.parseInt(components2[2].replace("s", ""));
        Duration duration2 = Duration.ofHours(hours2).plusMinutes(minutes2).plusSeconds(seconds2);

        // add the time spans together
        Duration sum = duration1.plus(duration2);

        // format the result as a string in the same format as the input
        long totalSeconds = sum.getSeconds();
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    public static long convertToDuration(String timeString) {
        String[] timeParts = timeString.split("\\s+");
        int hours = extractValue(timeParts[0], "h");
        int minutes = extractValue(timeParts[1], "m");
        int seconds = extractValue(timeParts[2], "s");

        return (hours * 3600) + (minutes * 60) + seconds;
    }

    private static int extractValue(String timePart, String unit) {
        String value = timePart.replace(unit, "");
        return Integer.parseInt(value);
    }

    public static void writeUserStatsToFile(HashMap<String, UserStat> userStats, String fileName) {
        try {
            BufferedWriter writer;
            writer = new BufferedWriter(new FileWriter(fileName));
            for (Map.Entry<String, UserStat> entry : userStats.entrySet()) {
                writer.write(entry.getKey() + ":");
                UserStat userStat = entry.getValue();
                writer.write("Exercise Duration: " + userStat.userAverageExerciseDuration + ", ");
                writer.write("Elevation: " + userStat.userAverageElevation + ", ");
                writer.write("Distance: " + userStat.userAverageDistance);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
