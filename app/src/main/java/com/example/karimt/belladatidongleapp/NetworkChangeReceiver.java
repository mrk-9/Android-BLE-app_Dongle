package com.example.karimt.belladatidongleapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by KarimT on 19.12.2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    String readString,readEvents;
    String[] arrayToPost,arrayEvents;
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm1 = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo1 = cm1.getActiveNetworkInfo();
        if (netInfo1 != null) {
            if (netInfo1.isConnected() && !ConnectBTActivity.isActive) {
                readString = readFromFile("storedData.txt");
                readEvents = readFromFile("PHYDEvents.txt");
                arrayToPost = readString.split("/");
                arrayEvents = readEvents.split(",");
                AsynPostDataFromFile myTask = new AsynPostDataFromFile();
                AsyncPostDataEvents myAsync = new AsyncPostDataEvents();
                try {
                    myTask.execute(arrayToPost);
                } catch (Exception ex) {
                }
                try {
                    myAsync.execute(arrayEvents);
                } catch (Exception ex) {
                }
            }
        }
        if (MainActivity.isActivityRunning) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    ConnectBTActivity.internetConnection = true;
                } else {
                    ConnectBTActivity.internetConnection = false;
                }
            } else {
                ConnectBTActivity.internetConnection = false;
            }

            Intent i = new Intent();
            i.setClassName("com.example.karimt.belladatidongleapp", "com.example.karimt.belladatidongleapp.MainActivity");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MainActivity.instance.finish();
            context.startActivity(i);
        }
        if (ConnectBTActivity.appOpened) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    ConnectBTActivity.internetConnection = true;
                } else {
                    ConnectBTActivity.internetConnection = false;
                }
            } else {
                ConnectBTActivity.internetConnection = false;
            }
        }
    }
    private String readFromFile(String fileName) {

        String ret = "";

        try {
            String path =
                    Environment.getExternalStorageDirectory()+ File.separator+"PHYDData";
            // Make sure the path directory exists.
            File folder=new File(path);
            File file = new File(folder, fileName);
            FileInputStream in = new FileInputStream(file);
            InputStream inputStream = in;

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}