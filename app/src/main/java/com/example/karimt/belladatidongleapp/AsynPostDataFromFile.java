package com.example.karimt.belladatidongleapp;

import android.os.AsyncTask;
import android.os.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by KarimT on 13.02.2017.
 */

public class AsynPostDataFromFile extends AsyncTask<String[],Void,Void> {

    protected Void doInBackground(String[]... params) {
        try {

            /*PostData pd1 = new PostData();
            ObjectNode postData1 = new ObjectMapper().createObjectNode();
            postData1.put("sensorId", params[0][0]);
            postData1.put("distance", params[0][1]);
            postData1.put("start_time", params[0][2]);
            postData1.put("end_time", params[0][3]);
            postData1.put("elapsed_time", params[0][4]);
            postData1.put("average_speed", params[0][5]);
            postData1.put("count_dangerous_places", params[0][6]);
            postData1.put("count_braking",params[0][7]);
            postData1.put("count_speed_up",params[0][8]);
            postData1.put("count_speed_right",params[0][9]);
            postData1.put("count_speed_left",params[0][10]);
            postData1.put("date_only",params[0][11]);
            if(MainActivity.distance!=0D)
            MainActivity.distance=0D;*/


            String sensorId;
            sensorId=params[0][0].split(",")[0];
            PostData pd1 = new PostData();
            ObjectNode postEvents = new ObjectMapper().createObjectNode();
            ArrayNode columnsArray = new ObjectMapper().createArrayNode();

            for (String key:params[0]) {
                String[] keyValue=key.split(",");
                ObjectNode postData1 = new ObjectMapper().createObjectNode();
                postData1.put("sensorId", sensorId);
                postData1.put("distance", keyValue[1]);
                postData1.put("start_time", keyValue[2]);
                postData1.put("end_time", keyValue[3]);
                postData1.put("elapsed_time", keyValue[4]);
                postData1.put("average_speed", keyValue[5]);
                postData1.put("count_dangerous_places", keyValue[6]);
                postData1.put("count_braking",keyValue[7]);
                postData1.put("count_speed_up",keyValue[8]);
                postData1.put("count_speed_right",keyValue[9]);
                postData1.put("count_speed_left",keyValue[10]);
                postData1.put("date_only",keyValue[11]);
                columnsArray.add(postData1);
            }

            postEvents.put("sensorId", sensorId);
            postEvents.put("data", columnsArray);
            if(MainActivity.distance!=0D)
                MainActivity.distance=0D;

            String path =
                    Environment.getExternalStorageDirectory()+ File.separator+"PHYDData";
            // Make sure the path directory exists.
            File folder=new File(path);
            File file = new File(folder, "storedData.txt");
            if(file.exists())
            {
                file.delete();
            }
            //String url1 = "https://52.39.108.83:8005/data";
            String url1 = "https://service-test.belladati.com:8005/data";

            try {
                pd1.postToCollector(url1, postEvents);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (Exception e) {
            return null;
        }
        return null;
    }

    protected void onPostExecute() {
    }
}
