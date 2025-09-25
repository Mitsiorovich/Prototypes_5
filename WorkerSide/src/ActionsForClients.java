import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.*;
import java.net.Socket;

public class ActionsForClients extends Thread {
    Route subRoute;
    RouteViewModel rvm;

    ObjectInputStream inFromMaster;
    ObjectOutputStream outToMaster;
    public ActionsForClients(Socket providerSocket) {

        try {

            inFromMaster = new ObjectInputStream(providerSocket.getInputStream());
            outToMaster = new ObjectOutputStream(providerSocket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){

        try {
            subRoute = (Route) inFromMaster.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }



        System.out.println("New Job from Master!");

        RouteAnalyzer routeAnalysis = new RouteAnalyzer(subRoute.waypointPath);

        /// computes the stuff needed
        rvm = new RouteViewModel(
                String.valueOf(routeAnalysis.getTotalDistance()),
                String.valueOf(routeAnalysis.getTotalTime()),
                String.valueOf(routeAnalysis.getTotalElevation()),
                String.valueOf(routeAnalysis.getAverageSpeed()),
                String.valueOf(subRoute.routeMapId)
        );



        System.out.println("Worker: I processed chunk with map id: " + subRoute.routeMapId);

        try {
            outToMaster.writeObject(rvm);
            outToMaster.flush();
            inFromMaster.close();
            outToMaster.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public RouteViewModel getRvm(){
        return  this.rvm;
    }
}
