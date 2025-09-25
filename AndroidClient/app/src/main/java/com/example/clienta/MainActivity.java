package com.example.clienta;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class MainActivity extends AppCompatActivity {

    static String username;

    static String previousUserName;
    Handler routeDataHandler;

    Handler statsDataHandler;

    TextView textDistance, textTime , textElevation, textAvgSpeed;
    TextView totalDistance, totalTime, totalElevation, eleMsg, dstMsg ,timeMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        routeDataHandler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {

                        Bundle data = message.getData();

                        String messageFromChild = data.getString("Data ready");

                        Log.d("Handler data", messageFromChild);

                        RouteViewModel result = new RouteViewModel(messageFromChild);

                        updateRouteTable(result);

                        return true;
                    }
        });

        statsDataHandler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {

                        Bundle data = message.getData();

                        String messageFromChild = data.getString("Data ready");

                        Log.d("Handler data", messageFromChild);

                        if(messageFromChild.split(",")[0].equals("x") ){
                            userDoesNotExistAlert(messageFromChild.split(",")[5]);
                        }else{

                            UserStat result = new UserStat(messageFromChild, MainActivity.username);;

                            updateStatisticsTable(result);
                        }

                        return true;
                    }
                });


    }

    private static final int PICK_FILE_REQUEST_CODE = 1001;

    private ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        openGpxFile(uri);
                    }
                }
            });

    public void uploadFile(View v){

        EditText t = findViewById(R.id.current_user);
        this.username = t.getText().toString();


        openFilePicker();
    }

    public void getMyStats(View v){

        this.previousUserName = this.username;

        EditText t = findViewById(R.id.current_user);
        this.username = t.getText().toString();

        if(this.previousUserName == null){
            this.previousUserName = this.username;
        }

        if(!this.previousUserName.equals(this.username)){
            clearRouteTable();
        }

        MyThread myThread = new MyThread(null, statsDataHandler,this.username);
        myThread.start();


    };

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        filePickerLauncher.launch("*/*");
    }
    String creatorValue = "";

    private void openGpxFile(Uri uri) {
        Log.d("FilePicker", "Selected file URI: " + uri.toString());

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            File tempFile = File.createTempFile("temp_gpx", ".gpx");

            saveInputStreamToFile(inputStream, tempFile);


            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(tempFile);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(baos));

            byte[] data = baos.toByteArray();

            /// get creator value from the gpx context
            String gpxContent = baos.toString();
            Pattern pattern = Pattern.compile("creator=\"(.*?)\"");
            Matcher matcher = pattern.matcher(gpxContent);

            if (matcher.find()) {
                creatorValue = matcher.group(1);
            }

            ///check if the gpx creator is the same with the user logged in (dummy log in)
            if(this.username.equals(creatorValue)){


                MyThread myThread = new MyThread(data, routeDataHandler,null);
                myThread.start();



            }else{
                showNotMyFileAlert();
            }


            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }
    public  void updateRouteTable(RouteViewModel result){
        textDistance = findViewById(R.id.text_distance);
        textTime = findViewById(R.id.text_time);
        textElevation = findViewById(R.id.text_elevation);
        textAvgSpeed = findViewById(R.id.text_avg_speed);

        textDistance.setText(twoDecimalPlacesStr(Double.valueOf(result.TotalDistance)));
        textTime.setText(result.TotalTime);
        textElevation.setText(twoDecimalPlacesStr(Double.valueOf(result.TotalEle)));
        textAvgSpeed.setText(twoDecimalPlacesStr(Double.valueOf(result.AverageSpeed)));

        showSmileAlert();
    }

    public void updateStatisticsTable(UserStat stat){
        totalDistance = findViewById(R.id.total_distance);
        totalTime = findViewById(R.id.total_time);
        totalElevation = findViewById(R.id.total_elevation);
        dstMsg = findViewById(R.id.distance_msg);
        eleMsg = findViewById(R.id.ele_msg);
        timeMsg = findViewById(R.id.time_msg);

        totalDistance.setText(twoDecimalPlacesStr(Double.valueOf(stat.userAverageDistance)));
        totalTime.setText(stat.userAverageExerciseDuration);
        totalElevation.setText(twoDecimalPlacesStr(Double.valueOf(stat.userAverageElevation)));
        dstMsg.setText(stat.msgDistDiff);
        eleMsg.setText(stat.msgEleDiff);
        timeMsg.setText(stat.msgDurationDiff);
    }

    private void showNotMyFileAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Denied");
        builder.setMessage("You must pick a file whose creator name is the same with your username!");
        builder.setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSmileAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Route analysis ready!");
        builder.setMessage("Your performance has been recorded.");
        builder.setPositiveButton("Get my new stats!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getMyStats(new View(getApplicationContext()));
            }
        });
        AlertDialog dialog = builder.create();
        clearStatisticsTables();
        dialog.show();
    }

    private void userDoesNotExistAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Problem!");
        builder.setMessage(message);
        builder.setPositiveButton("Try another user name!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String twoDecimalPlacesStr(double number) {
        return String.valueOf(Math.round(number * 100.0) / 100.0);
    }

    private void saveInputStreamToFile(InputStream inputStream, File outputFile) {
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  void clearRouteTable(){
        textDistance = findViewById(R.id.text_distance);
        textTime = findViewById(R.id.text_time);
        textElevation = findViewById(R.id.text_elevation);
        textAvgSpeed = findViewById(R.id.text_avg_speed);

        textDistance.setText("");
        textTime.setText("");
        textElevation.setText("");
        textAvgSpeed.setText("");
    }

    private void clearStatisticsTables(){
        totalDistance = findViewById(R.id.total_distance);
        totalTime = findViewById(R.id.total_time);
        totalElevation = findViewById(R.id.total_elevation);
        dstMsg = findViewById(R.id.distance_msg);
        eleMsg = findViewById(R.id.ele_msg);
        timeMsg = findViewById(R.id.time_msg);

        totalDistance.setText("");
        totalTime.setText("");
        totalElevation.setText("");
        dstMsg.setText("");
        eleMsg.setText("");
        timeMsg.setText("");
    }

}