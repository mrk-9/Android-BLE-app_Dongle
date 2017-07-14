package com.example.karimt.belladatidongleapp;

import android.os.AsyncTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import layout.MapPage;


/**
 * Created by KarimT on 21.09.2016.
 * asynchronous writing to IoT data collector
 */
public class AsyncPostData extends AsyncTask<Void,Void,Void> {

    protected Void doInBackground(Void... params) {
        try {
            String dist;
            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.CEILING);
            dist = df.format(MainActivity.distance / 1000);
            PostData pd1 = new PostData();
            ObjectNode postData1 = new ObjectMapper().createObjectNode();
            postData1.put("sensorId", ConnectBTActivity.dongleName+" - Summary");
            postData1.put("distance", dist);
            postData1.put("start_time", ConnectBTActivity.startTime);
            postData1.put("end_time", ConnectBTActivity.endTime);
            postData1.put("elapsed_time", ConnectBTActivity.elapsedTime);
            postData1.put("average_speed", String.format("%.4f", ConnectBTActivity.averageSpeed));
            postData1.put("count_dangerous_places", MainActivity.countDangerous);
            postData1.put("count_braking",ConnectBTActivity.hardBraking);
            postData1.put("count_speed_up",ConnectBTActivity.heavySpeed);
            postData1.put("count_speed_right",ConnectBTActivity.heavyRight);
            postData1.put("count_speed_left",ConnectBTActivity.heavyLeft);
            postData1.put("date_only",ConnectBTActivity.dateOnly);
            MainActivity.distance=0D;

            String url1 = "https://52.39.108.83:8005/data";
            try {
                pd1.postToCollector(url1, postData1);
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
