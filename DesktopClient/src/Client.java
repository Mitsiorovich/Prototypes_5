import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.*;
import java.net.*;

public class Client extends Thread {
    File inputFile;
    Client(File inputFile) {
        this.inputFile = inputFile;
    }
    public void run(){

        Socket providerSocket = null;
        ObjectInputStream in = null ;
        ObjectOutputStream out = null;

        try {

            providerSocket = new Socket("127.0.0.1" , 4321);


            out = new ObjectOutputStream(providerSocket.getOutputStream());




            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(inputFile);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(baos));
            byte[] data = baos.toByteArray();



            //sends data to the server
            out.writeObject(data);
            out.flush();

            in = new ObjectInputStream(providerSocket.getInputStream());
            String r = (String) in.readObject();
            RouteViewModel deserializedRoute = new RouteViewModel(r);

            System.out.println("========= Route analysis for file: "+ inputFile.getName() +" ===============================");
            System.out.println("Server>" + deserializedRoute.TotalDistance + " is your total distance");
            System.out.println("Server>" + deserializedRoute.TotalTime + " is your total time");
            System.out.println("Server>" + deserializedRoute.TotalEle + " is your total elevation");
            System.out.println("Server>" + deserializedRoute.AverageSpeed + " is your average speed");
            System.out.println("Server> " + deserializedRoute.RouteMapId.split("_")[0] + " keep on the good work!");


        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
                out.close();
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Client(new File("seed_gpx/route1.gpx")).start();
        new Client(new File("seed_gpx/route2.gpx")).start();
        new Client(new File("seed_gpx/route3.gpx")).start();
        new Client(new File("seed_gpx/route4.gpx")).start();
        new Client(new File("seed_gpx/route5.gpx")).start();
        new Client(new File("seed_gpx/route6.gpx")).start();

        System.out.println("Client said: i run");
    }


    //static void printWpt(Wpt waypoint) {
    //   System.out.println("Latitude: " + waypoint.getLat() +
    //           ", Longitude: " + waypoint.getLon() +
    //           " , Elevation: " + waypoint.getEle() +
    //         " , Time: " + waypoint.getTime()
    //   );
    //}
}
