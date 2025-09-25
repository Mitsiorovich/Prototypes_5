import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActionsForClients extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;

    public ActionsForClients(Socket connection) {
        System.out.println("New thread created to serve the client!");
        try {

            in = new ObjectInputStream(connection.getInputStream());
            out = new ObjectOutputStream(connection.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
       try {

           //reads data that comes from the client
           Object obj = in.readObject();// e.g. obj = "user1"

           if(obj instanceof  String){

               Master m = new Master();

               ////Master does the job
               m.getPersonalStatisics((String) obj, in, out);


           }else{
               byte[] data = (byte[])obj;

               ByteArrayInputStream bais = new ByteArrayInputStream(data);

               DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
               DocumentBuilder builder = factory.newDocumentBuilder();
               Document doc = builder.parse(bais);
               doc.getDocumentElement().normalize();

               Master m = new Master();

               ////Master does the job
               m.masterApiInit(doc, in, out);
           }


       } catch (IOException e) {
           e.printStackTrace();
       } catch (ParserConfigurationException e) {
           throw new RuntimeException(e);
       } catch (SAXException e) {
           throw new RuntimeException(e);
       } catch (ClassNotFoundException e) {
           throw new RuntimeException(e);
       }
    }
}
