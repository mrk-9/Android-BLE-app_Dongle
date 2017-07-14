package com.example.karimt.belladatidongleapp;

import android.os.AsyncTask;
import android.os.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

/**
 * Created by KarimT on 14.02.2017.
 */

public class AsyncPostDataEvents extends AsyncTask<String[],Void,Void> {

    protected Void doInBackground(String[]... params) {
        try {
            String sensorId;
            sensorId=params[0][0].split(":")[1];
            PostData pd1 = new PostData();
            ObjectNode postEvents = new ObjectMapper().createObjectNode();
            ArrayNode columnsArray = new ObjectMapper().createArrayNode();

            for (String key:params[0]) {
                ObjectNode postData1 = new ObjectMapper().createObjectNode();
                if(key.contains("B:"))
                {
                    String[] keyValue=key.split(":");
                    postData1.put("sI", sensorId);
                    postData1.put("t", "B");
                    postData1.put("v", Double.parseDouble(keyValue[1]));
                    columnsArray.add(postData1);
                }
                /*else if(key.contains("Left"))
                {
                    String[] keyValue=key.split(":");
                    postData1.put("sensorId", sensorId);
                    postData1.put("breaking", 0);
                    postData1.put("speed_up", 0);
                    postData1.put("left_cornering", keyValue[1]);
                    postData1.put("right_cornering", 0);
                    columnsArray.add(postData1);
                }*/
                else if(key.contains("L:"))
                {
                    String[] keyValue=key.split(":");
                    postData1.put("sI", sensorId);
                    postData1.put("t", "L");
                    postData1.put("v", Double.parseDouble(keyValue[1]));
                    columnsArray.add(postData1);
                }
                else if(key.contains("R:"))
                {
                    String[] keyValue=key.split(":");
                    postData1.put("sI", sensorId);
                    postData1.put("t", "R");
                    postData1.put("v", Double.parseDouble(keyValue[1]));
                    columnsArray.add(postData1);
                }
                else if(key.contains("S:"))
                {
                    String[] keyValue=key.split(":");
                    postData1.put("sI", sensorId);
                    postData1.put("t", "S");
                    postData1.put("v", Double.parseDouble(keyValue[1]));
                    columnsArray.add(postData1);
                }
            }

            postEvents.put("sI", sensorId);
            postEvents.put("data", columnsArray);


            String path =
                    Environment.getExternalStorageDirectory()+ File.separator+"PHYDData";
            // Make sure the path directory exists.
            File folder=new File(path);
            File file = new File(folder, "PHYDEvents.txt");
            if(file.exists())
            {
                file.delete();
            }
            if(columnsArray.size()!=0) {
                String url1 = "https://service-test.belladati.com:8007/data";
                try {
                    pd1.postToCollector(url1, postEvents);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        } catch (Exception e) {
            return null;
        }
        return null;
    }

    protected void onPostExecute() {
    }
}
