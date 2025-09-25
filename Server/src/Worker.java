import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Worker extends Thread{

    Socket masterAsClient = null;
    Route wptPath;
    RouteViewModel deserializedRoute;

    ObjectInputStream inFromWorker = null;
    ObjectOutputStream outToWorker= null;

    public Worker(Socket connectionWithServer){
        masterAsClient= connectionWithServer;
    }
    public void assignRoute(Route wptPath, String routeMapId) {
        this.wptPath = wptPath;
        this.wptPath.routeMapId = routeMapId;
    }
    public void run(){
            try{
                System.out.println("Server: New worker thread created!");
                //sends the wpt path to worker for processing
                if(outToWorker == null){
                    outToWorker = new ObjectOutputStream(masterAsClient.getOutputStream());
                }
                outToWorker.writeObject(wptPath);
                outToWorker.flush();
                //reads results the worker returns

                if(inFromWorker == null){
                    inFromWorker = new ObjectInputStream(masterAsClient.getInputStream());
                }



                deserializedRoute = (RouteViewModel) inFromWorker.readObject();

                System.out.println("Server: I got the route calculations!");
                outToWorker.close();
                inFromWorker.close();
                masterAsClient.close();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
    }
    public RouteViewModel getResultFromWorkers(){
        return this.deserializedRoute;
    }
}
