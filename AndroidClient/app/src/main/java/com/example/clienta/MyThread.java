package com.example.clienta;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MyThread extends Thread{
    UserStat stat;
    Handler myHandler;
    byte[] data;

    public MyThread(byte[] data, Handler myHandler,String userName){
        this.data = data;
        this.myHandler = myHandler;
        if(userName != null){
            stat = new UserStat();
            stat.userName = userName;
        }
    }

    @Override
    public void run() {
        try {
            Socket s = new Socket("127.0.0.1" , 4321);/// use the ip of your server
            ObjectOutputStream oos =
                    new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream ois =
                    new ObjectInputStream(s.getInputStream());

            if(stat != null){/// the code that runs if we ask for statistics (we send string of username)
                if(stat.userName != null){
                    oos.writeObject(stat.userName);
                    oos.flush();

                    String r = (String) ois.readObject();

                    Bundle data = new Bundle();
                    data.putString("Data ready", r);

                    Message msg = Message.obtain();
                    msg.setData(data);

                    myHandler.sendMessage(msg);

                }
            }else{// the code that runs if we want to analyze the route (we send byte array of route)
                oos.writeObject(data);
                oos.flush();

                String r = (String) ois.readObject();


                Bundle data = new Bundle();
                data.putString("Data ready", r);

                Message msg = Message.obtain();
                msg.setData(data);

                myHandler.sendMessage(msg);

            }

            s.close();
            oos.close();
            ois.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}