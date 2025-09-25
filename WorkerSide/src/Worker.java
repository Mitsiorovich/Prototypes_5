import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Worker {

    Socket providerSocket;
    ObjectInputStream inFromMaster;
    ObjectOutputStream outToMaster;

    public static void main(String args[]) {
        new Worker().openWorker();
    }

    private static ArrayList<ActionsForClients> masterClientThreads
            = new ArrayList<ActionsForClients>();




    void openWorker(){
        try{
            while (true) {
                    providerSocket = new Socket("127.0.0.1", 1234);
                    System.out.println("Worker: New connection established!");

                    System.out.println("Worker: Waiting message from server");
                    ActionsForClients afc = new ActionsForClients(providerSocket);
                    afc.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inFromMaster != null) {
                    inFromMaster.close();
                }
                if (outToMaster != null) {
                    outToMaster.close();
                }
                if (providerSocket != null) {
                    providerSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
