package com.example.karimt.belladatidongleapp;

import android.os.AsyncTask;
import android.util.Log;

import com.belladati.sdk.BellaDatiConnection;
import com.belladati.sdk.BellaDatiService;

/**
 * Created by KarimT on 22.09.2016.
 * xAuth to BellaDati for download data from datasets
 */
public class ConnectToBD extends AsyncTask<BellaDatiConnection,Void,BellaDatiService> {

    protected BellaDatiService doInBackground(BellaDatiConnection... params) {
        try {
            Log.i("time",System.currentTimeMillis()+"");

            BellaDatiService service = params[0].xAuth("apiKey", "apiSecret", "androidadmin", "BellaDati01");
      //chinese
            //BellaDatiService service = params[0].xAuth("apiKey", "apiSecret", "androidadminzh", "Belladati01");

            return service;
        } catch (Exception e) {
            return null;
        }
    }
    protected void onPostExecute(BellaDatiService result) {

    }
}
