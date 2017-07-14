package com.example.karimt.belladatidongleapp;

/*import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothReceiver extends BroadcastReceiver {

    boolean found = false;
   public static BluetoothDevice device;
    ArrayList<BluetoothDevice> foundDevices=new ArrayList<>();
    BluetoothAdapter bluetoothAdapter;
    ConnectBTActivity.ThreadConnectBTdevice myThreadConnectBTdevice;

    String suffix="000";
    BluetoothDevice result;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ConnectBTActivity.isActive) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices != null) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getAddress().contains("UMS")) {
                        suffix = device.getName().substring(12, 15);
                        result=device;
                    }
                }
            }
            Bundle intentExtras = intent.getExtras();
            bluetoothAdapter.startDiscovery();
            if (intentExtras != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //discovery starts, we can show progress dialog or perform other tasks
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    if (found) {
                    } else {
                        for (int i = 0; i < foundDevices.size(); i++) {
                            if (foundDevices.get(i).getName() != null) {
                                if (foundDevices.get(i).getName().contains("UMS")) {
                                    found = true;
                                    bluetoothAdapter.cancelDiscovery();
                                }
                            }
                        }
                        if (!found)
                            bluetoothAdapter.startDiscovery();
                    }
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //bluetooth device found
                    device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    foundDevices.add(device);
                    if (device != null) {
                        if (device.getName() != null) {
                            if (device.getName().contains("UMS") && device.getName().endsWith(suffix)) {
                                found = true;
                                if (myThreadConnectBTdevice == null) {
                                    bluetoothAdapter.cancelDiscovery();
                                    PackageManager pm = context.getPackageManager();
                                    Intent i = pm.getLaunchIntentForPackage(context.getPackageName());
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    i.putExtra("btdevice",result);
                                    context.startActivity(i);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }*/