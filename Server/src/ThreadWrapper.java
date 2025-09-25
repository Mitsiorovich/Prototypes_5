import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadWrapper extends Thread{
    public boolean threadUsed = false;
    ServerSocket providerSocketForClients;

    ThreadWrapper(){

    }
    ThreadWrapper(ServerSocket providerSocketForClients){
        this.providerSocketForClients = providerSocketForClients;
    }

    public void run(){
        Socket handlerSocketForClients = null;
        try {
            handlerSocketForClients = providerSocketForClients.accept();
            threadUsed = true;
            ActionsForClients x1 = new ActionsForClients(handlerSocketForClients);
            x1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
