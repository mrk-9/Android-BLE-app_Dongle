package com.example.karimt.belladatidongleapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RelativeLayout;

import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.filter.FilterOperation;
import com.belladati.sdk.filter.FilterValue;
import com.belladati.android.views.Table;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by KarimT on 14.02.2017.
 */

public class AsyncCreateTable extends AsyncTask<Table,Void,Table> {

    protected Table doInBackground(Table... params) {
        try {
            Table b=params[0];
            return b;

        } catch (Exception e) {
            return null;
        }
    }
    private Attribute getAttribute(String filter, String idDataset) {
        for (Attribute aa : MainActivity.service.loadDataSet(idDataset).getAttributes()) {
            if (filter.equalsIgnoreCase(aa.getCode())) {
                return aa;
            }
        }
        return null;
    }

    protected void onPostExecute(Table table) {
        if(isCancelled())
        {
            table=null;
        }
        Filter.MultiValueFilter fUserId, fDate;

        fUserId = FilterOperation.IN.createFilter(getAttribute("L_SENSOR_ID", "32949"));
        fUserId.addValue(new FilterValue(MainActivity.savedDongleName+" - Summary"));

            /*fDate = FilterOperation.IN.createFilter(getAttribute("L_DATE", "32531"));
            fDate.addValue(new FilterValue(selMonth));*/

        ObjectNode filterNode = new ObjectMapper().createObjectNode();
        filterNode.setAll(fUserId.toJson());
        //filterNode.setAll(fDate.toJson());
        table.setHeadColumnStyle(R.drawable.cell_shape_d);
        table.setFilterNode(filterNode);
        table.setIdTable("48326-I4UWT4Fx3o");
        table.setService(MainActivity.service);
        try {
            table.createTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
