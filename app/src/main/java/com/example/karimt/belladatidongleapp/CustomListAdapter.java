package com.example.karimt.belladatidongleapp;

/**
 * Created by KarimT on 29.09.2016.
 */
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by KarimT on 25.08.2016.
 * custom list adapter for paired devices
 */
public class CustomListAdapter extends ArrayAdapter<BluetoothDevice> {

    private final Activity context;
    private final ArrayList<BluetoothDevice> pairedDeviceArrayList;

    public CustomListAdapter(Activity context, ArrayList<BluetoothDevice> pairedDeviceArrayList) {
        super(context, R.layout.activity_list_view, pairedDeviceArrayList);
        // TODO Auto-generated constructor stub

        this.context = context;
        this.pairedDeviceArrayList=pairedDeviceArrayList;

    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.activity_list_view, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.textRow);

        txtTitle.setText(pairedDeviceArrayList.get(position).getName());
        return rowView;

    }
}