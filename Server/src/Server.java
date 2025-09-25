import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class Server {
    public static boolean isReady = false;
    public static ArrayList<Worker> workerConnections = new ArrayList<Worker>();
    public static  ArrayList<ActionsForClients> clientConnections = new ArrayList<ActionsForClients>();
    public static int countWorkersConnected = 0;
    public static void main(String args[]) {
        new Server().openServer();
    }

    public static final int numberOfWorkersExpected = 2;

    /* Define the socket that receives requests */
    ServerSocket providerSocketForClients;
    ServerSocket providerSocketForWorkers;


    /* Define the socket that is used to handle the connection */
    Socket handlerSocketForClients;


    void openServer() {
        try {

            /* Create Server Socket */
            providerSocketForClients = new ServerSocket(4321);
            providerSocketForWorkers = new ServerSocket(1234);



            ThreadWrapper currentThread = new ThreadWrapper();
            ThreadWrapper previousThread = new ThreadWrapper();
            previousThread.threadUsed = true;


            while (true) {
                ///worker connections
                if (countWorkersConnected < numberOfWorkersExpected) {
                    System.out.println("Server: Waiting for worker to connect....");
                    Socket handlerSocketForWorkers = new Socket();
                    handlerSocketForWorkers = providerSocketForWorkers.accept();

                    workerConnections.add(new Worker(handlerSocketForWorkers));

                    countWorkersConnected++;
                    System.out.println("Worker number:" + countWorkersConnected + " connected!");

                    if (countWorkersConnected == numberOfWorkersExpected) {
                        synchronized (workerConnections) {
                            isReady = true;
                            workerConnections.notifyAll();
                        }
                    }
                    continue;
                }

                ///worker connections end


                if(previousThread.threadUsed){
                    currentThread = new ThreadWrapper(providerSocketForClients);
                    currentThread.start();
                } else {
                    currentThread = previousThread;
                }

                previousThread = currentThread;

            }




        } catch (IOException  e) {
            e.printStackTrace();
        } finally {
            try {
                providerSocketForClients.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
