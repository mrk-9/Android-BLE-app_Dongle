package com.example.karimt.belladatidongleapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class ConnectBTActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    public static boolean isActive = false, crash = false, appOpened = false, internetConnection = false;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> pairedDeviceArrayList, foundDevices;
    TextView textInfo, textStatus, textDetails;
    ListView listViewPairedDevice;
    boolean found = false;
    public static String startTime, endTime, dateOnly, macAddress, dongleName = "";
    public static float averageSpeed, elapsedTime;
    XmlData xmlData;
    private ProgressDialog mProgressDlg, mProgressDlgConnecting;
    CustomListAdapter pairedDeviceAdapter;
    BluetoothDevice result, device;
    String crashIndex, emergencyNumber, formattedDate, deviceAddress, suffix;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
    int btConnectionTries = 0;

    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    public static int hardBraking = 0, heavySpeed = 0, heavyLeft = 0, heavyRight = 0;

    //initialization of window activity_connect_bt
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_bt);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        textInfo = (TextView) findViewById(R.id.info);
        textStatus = (TextView) findViewById(R.id.status);
        listViewPairedDevice = (ListView) findViewById(R.id.pairedlist);
        textDetails = (TextView) findViewById(R.id.tvDetails);

        appOpened = true;
        internetConnection = isNetworkConnected();

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.CALL_PHONE, Manifest.permission.BLUETOOTH_PRIVILEGED, Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.BLUETOOTH_ADMIN};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this,
                    "FEATURE_BLUETOOTH NOT support",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mProgressDlgConnecting = new ProgressDialog(this);
        mProgressDlgConnecting.setMessage(getString(R.string.progres_connecting) + "...");
        mProgressDlgConnecting.setCancelable(false);
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage(getString(R.string.scan_dongle) + "...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                found = true;
                bluetoothAdapter.cancelDiscovery();
            }
        });

        String stInfo = bluetoothAdapter.getName() + bluetoothAdapter.getAddress();
        textInfo.setText(stInfo);
        foundDevices = new ArrayList<>();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        findViewById(R.id.buttonOffline).setOnClickListener(new ButtonOfflineListener());
    }

    //button for run aplication without dongle
    private class ButtonOfflineListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ConnectBTActivity.this, MainActivity.class);
            Bundle b = new Bundle();
            b.putBoolean("offline", true);
            intent.putExtras(b);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private final BroadcastReceiver mReceiver = new Receiver();

    /*Broadcast Receiver for finding nearby bluetooth
    devices and calling connect to bluetooth device*/
    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (found) {
                    mProgressDlg.dismiss();
                } else {
                    for (int i = 0; i < foundDevices.size(); i++) {
                        if (foundDevices.get(i).getName() != null) {
                            if (deviceAddress != null && foundDevices.get(i).getName().contains("UMS")) {
                                mProgressDlg.dismiss();
                                found = true;
                                textStatus.setText("Your dongle is unpaired!\nPair your dongle in Android!");
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
                        if (deviceAddress != null && device.getName().endsWith(suffix) && device.getName().contains("UMS")) {
                            found = true;
                            mProgressDlgConnecting.show();
                            if (myThreadConnectBTdevice == null) {
                                myThreadConnectBTdevice = new ThreadConnectBTdevice(result);
                                myThreadConnectBTdevice.start();
                            }
                        }
                    }
                }
            }
        }
    }

    //onStart is checking if bluetooth is On and calling method setup
    @Override
    public void onStart() {
        super.onStart();
        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        setup();
    }

    //setup of bluetooth devices and bluetooth adapter
    private void setup() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedDeviceArrayList = new ArrayList<BluetoothDevice>();

            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceArrayList.add(device);
            }
            pairedDeviceAdapter = new CustomListAdapter(this, pairedDeviceArrayList);

            xmlData = (XmlData) loadClassFile();
            if (xmlData == null) {
                xmlData = new XmlData();
            }
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);

            if (!xmlData.getMacAddress().equals("")) {
                deviceAddress = xmlData.getMacAddress();

                result = null;
                if (pairedDevices != null) {
                    Boolean pairedMatches = false;
                    for (BluetoothDevice device : pairedDevices) {
                        if (deviceAddress.equals(device.getAddress())) {
                            result = device;
                            suffix = result.getName().substring(12, 15);
                            pairedMatches = true;
                            if (bluetoothAdapter.isDiscovering()) {
                                bluetoothAdapter.cancelDiscovery();
                            }
                            bluetoothAdapter.startDiscovery();
                            break;
                        } else {
                            pairedMatches = false;
                        }
                    }
                    if (pairedMatches == false) {
                        listViewPairedDevice.setVisibility(View.VISIBLE);
                        textStatus.setText("Your dongle is new one!\nSelect new dongle!");
                    }
                }
            } else {
                listViewPairedDevice.setVisibility(View.VISIBLE);
                textStatus.setText("Please select dongle!");
            }

            listViewPairedDevice.setOnItemClickListener(new ClicOnItemListener());
        }
    }

    //listener for click on list of Paired devices
    private class ClicOnItemListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            BluetoothDevice device =
                    (BluetoothDevice) parent.getItemAtPosition(position);
            Toast.makeText(getApplicationContext(),
                    "Name: " + device.getName() + "\n"
                            + "Address: " + device.getAddress() + "\n"
                            + "BondState: " + device.getBondState() + "\n"
                            + "BluetoothClass: " + device.getBluetoothClass() + "\n"
                            + "Class: " + device.getClass(),
                    Toast.LENGTH_LONG).show();

            myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
            macAddress = device.getAddress();
            XmlData dataForWrite = new XmlData();
            dataForWrite.setMacAddress(macAddress);
            writeToFile(dataForWrite);
            mProgressDlgConnecting.show();
            myThreadConnectBTdevice.start();
        }
    }

    //this method is calling with ending application
    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }

    //mathematic calculation between start time and end time of drive
    public void printDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();
        System.out.println("startDate : " + startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;
        float elapsedtimeInHours = (elapsedDays * 24) + (elapsedHours) + ((float) elapsedMinutes / 60) + ((float) elapsedSeconds / 3600);
        averageSpeed = ((float) MainActivity.distance / 1000) / elapsedtimeInHours;
        elapsedTime = elapsedDays * 24 * 60 + elapsedHours * 60 + elapsedMinutes + ((float) elapsedSeconds / 60);
        System.out.printf(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays,
                elapsedHours, elapsedMinutes, elapsedSeconds);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                setup();
            } else {
                Toast.makeText(this,
                        "BlueTooth NOT enabled",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket) throws IOException {

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    /*Reset ThreadConnectBTdevice*/
    private synchronized void resetConnectThread() {
        if (myThreadConnectBTdevice != null) {
            myThreadConnectBTdevice.cancel();
            myThreadConnectBTdevice = null;
        }
        myThreadConnectBTdevice = new ThreadConnectBTdevice(result);
        myThreadConnectBTdevice.start();
    }

    /*
        ThreadConnectBTdevice:
        Background Thread to handle BlueTooth connecting
        */
    public class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;
        boolean paired;

        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;
            BluetoothSocket tempSocket = null;
            try {
                tempSocket = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (Exception e) {
                Log.e("Err", "Failed to create a secure socket!");
                try {
                    tempSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                } catch (IOException e2) {
                    Log.e("Err", "Failed to create an insecure socket!");
                }
            }
            bluetoothSocket = tempSocket;
        }

        //connect to bluetooth device
        @Override
        public void run() {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            boolean success = false;
            try {
                btConnectionTries++;
                bluetoothSocket.connect();
                success = true;
                mProgressDlgConnecting.dismiss();

            } catch (Exception e) {
                if (btConnectionTries > 1) {
                    mProgressDlgConnecting.show();
                    fallback();
                } else {
                    mProgressDlgConnecting.show();
                    cancel();
                    resetConnectThread();
                }
            }
            if (success) {
                mProgressDlg.dismiss();
                btConnectionTries = 0;
                dongleName = bluetoothDevice.getName();
                //connect successful
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                startTime = df.format(new Date());
                final String msgconnected = getString(R.string.connect_succ);
                final String msgBTDevice = getString(R.string.bt_device) + ": " + bluetoothDevice.getName();

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textStatus.setText(msgconnected);
                        textDetails.setText(msgBTDevice);
                        listViewPairedDevice.setVisibility(View.GONE);
                    }
                });

                try {
                    startThreadConnected(bluetoothSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(ConnectBTActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                //fail
            }
        }

        /*Fallback method is called when the device is not successfully connected to TEP sensor after first try*/
        public void fallback() {
            unpairDevice(bluetoothDevice);
            pairDevice(bluetoothDevice);
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices != null) {
                for (BluetoothDevice device : pairedDevices) {
                    if (bluetoothDevice.getAddress().equals(device.getAddress())) {
                        paired = true;
                        break;
                    }
                }
                if (!paired) {
                    pairDevice(bluetoothDevice);
                    cancel();
                    resetConnectThread();
                } else {
                    cancel();
                    resetConnectThread();
                }
            } else {
                pairDevice(bluetoothDevice);
                cancel();
                resetConnectThread();
            }
        }

        //cancel bluetooth connection
        public void cancel() {

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        //pair device programatically
        private void pairDevice(BluetoothDevice device) {
            try {

                Method m = device.getClass()
                        .getMethod("createBond", (Class[]) null);
                m.invoke(device, (Object[]) null);
            } catch (Exception e) {
            }
        }

        //unpair device programatically
        private void unpairDevice(BluetoothDevice device) {
            try {
                Method m = device.getClass()
                        .getMethod("removeBond", (Class[]) null);
                m.invoke(device, (Object[]) null);
                Log.e("INFO: ", "Unpaired");
            } catch (Exception e) {
            }
        }
    }

    //load xml for Mac address of bluetooth device
    public Object loadClassFile() {
        try {
            ObjectInputStream ois = new ObjectInputStream(openFileInput("macAddress.xml"));
            Object o = ois.readObject();
            return o;

        } catch (Exception ex) {
            Log.v("MacAddress", ex.getMessage());

            ex.printStackTrace();
        }
        return null;
    }

    //write Mac address of device to file macAddress.xml
    private void writeToFile(Object buffer) {
        try {
            ObjectOutputStream outputStreamWriter = new ObjectOutputStream(openFileOutput("macAddress.xml", Context.MODE_PRIVATE));
            outputStreamWriter.writeObject(buffer);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    //thread for reading data from bluetooth device
    public class ThreadConnected extends Thread {
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;
        byte[] bufToWrite;
        boolean calibration = false,alreadyDeleted=false;

        public ThreadConnected(BluetoothSocket socket) throws IOException {
            OutputStream outputStream = null;
            InputStream in = null;
            try {
                in = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            connectedInputStream = in;
            connectedOutputStream = outputStream;
            //String s = "012345AB";
            String macAddress = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");
            //byte[] bAcount = s.getBytes();
            macAddress = macAddress.replace(":", "");
            byte[] bAcount = hexStringToByteArray(macAddress);
            byte[] freeBuf = new byte[2];
            byte[] bAcountEightBytes = new byte[bAcount.length + freeBuf.length];
            System.arraycopy(bAcount, 0, bAcountEightBytes, 0, bAcount.length);
            System.arraycopy(freeBuf, 0, bAcountEightBytes, bAcount.length, freeBuf.length);
            //byte[] bAcountEightBytes=s.getBytes();

            byte[] bInsurance = ByteBuffer.allocate(4).putInt(1112).array();

            Date date = new Date();
            long dateLong = date.getTime();
            byte[] bDate = ByteBuffer.allocate(8).putLong(dateLong).array();

            byte[] bInitialValue = hexStringToByteArray("10160447");

            byte[] c = new byte[bInitialValue.length + bAcountEightBytes.length];
            System.arraycopy(bInitialValue, 0, c, 0, bInitialValue.length);
            System.arraycopy(bAcountEightBytes, 0, c, bInitialValue.length, bAcountEightBytes.length);

            byte[] d = new byte[c.length + bInsurance.length];
            System.arraycopy(c, 0, d, 0, c.length);
            System.arraycopy(bInsurance, 0, d, c.length, bInsurance.length);

            bufToWrite = new byte[d.length + bDate.length];
            System.arraycopy(d, 0, bufToWrite, 0, d.length);
            System.arraycopy(bDate, 0, bufToWrite, d.length, bDate.length);

            connectedOutputStream.write(bufToWrite);
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
            dateOnly = df2.format(new Date());
            StoreEventData("SI:" + ConnectBTActivity.dongleName.substring(12));
            isActive = true;

        }

        //post data to IoT-Data Collector
        @Override
        public void run() {
            byte[] buffer = new byte[7];
            while (true) {
                try {

                    int bytesAvailable = connectedInputStream.available();
                    byte[] packetBytes;

                    if (bytesAvailable > 20) {
                        packetBytes = new byte[bytesAvailable];
                    } else {
                        packetBytes = new byte[7];
                    }
                    connectedInputStream.read(packetBytes);
                    Log.e("INFO", Arrays.toString(packetBytes));
                    //byte[] bytesToAutoCalib = hexStringToByteArray("301956000000000000000000000000000000000000000000000000");
                    // Make sure the path directory exists.
                    String readCalibration;
                    if (!calibration) {
                        try {
                            readCalibration = readFromFileCalib("calibration");
                            readCalibration = readCalibration.replaceAll("\\[", "").replaceAll("\\]", "");
                            String[] arrayCalibration = readCalibration.split("/");
                            if (arrayCalibration.length > 1 && !arrayCalibration[1].equals("0")) {
                                String last = arrayCalibration[arrayCalibration.length - 1].replace(" ", "");
                                String latest = arrayCalibration[arrayCalibration.length - 2].replace(" ", "");
                                String res = latest + "," + last;
                                String[] strArray = res.split(",");
                                byte[] bytesArray = new byte[strArray.length];
                                int index = 0;
                                for (String item : strArray) {
                                    bytesArray[index] = Byte.parseByte(item);
                                    index++;
                                }
                                connectedOutputStream.write(bytesArray);
                                /*CalibrationData dataForWrite = new CalibrationData();
                                dataForWrite.setStoredData(bytesArray);
                                writeToFileCalib(dataForWrite);*/
                                calibration = true;
                            }
                        } catch (Exception e) {
                        }
                    }

                    /*if (!calibration) {
                        CalibrationData calibrationData = (CalibrationData) loadClassFileCalib();
                        if (calibrationData != null) {
                            byte[] bytesToAutoCalib = calibrationData.getStoredData();
                            Log.e("INFO", Arrays.toString(bytesToAutoCalib));
                            connectedOutputStream.write(bytesToAutoCalib);
                            calibration = true;
                        }
                    }*/
                    if (!calibration) {
                        //byte[] bytesToAutoCalib = new byte[]{48, 100, 86, 0, 0, 3, -10, 0, 0, -1, 97, 0, 0, 17, 105, -1, -1, -33, -77, 0, 0, 7, -9, -1, -1, 2, 44, 1, 6, 5, -1, -1, -13, 3, 0, 0, 13, 52, -1, -1, 0, -86, -1, -1, -11, 105, 0, 0, 12, -33, -1, -1, 0, -117, -1, -1, -42, 19, 0, 0, 8, 64, -1, -1, 3, -106, -1, -1, -109, 69, -1, -1, -6, 117, -1, -1, 24, 78, -1, -1, -12, -17, 0, 0, 5, 65, -1, -1, 0, 75, -1, -1, -10, 0, 0, 0, 12, -94, -1, -1, 0, -127};
                        byte[] bytesToAutoCalib = hexStringToByteArray("301956000000000000000000000000000000000000000000000000");
                        connectedOutputStream.write(bytesToAutoCalib);
                        calibration = true;
                    }
                    byte[] bytesToWrite = hexStringToByteArray("C000");
                    connectedOutputStream.write(bytesToWrite);

                    if ((packetBytes[2] == 86 ) || (packetBytes.length > 7 && packetBytes[2] != 67 && packetBytes[0] != -64)) {
                        if ((packetBytes[1] == -1 || packetBytes[1]==0) && packetBytes[2] == 86) {
                            byte[] bytesToAutoCalib = hexStringToByteArray("301956000000000000000000000000000000000000000000000000");
                            connectedOutputStream.write(bytesToAutoCalib);
                            calibration = true;
                        } else {
                            // Save your stream, don't forget to flush() it before closing it.
                            try {
                                File file = new File(getFilesDir(), "calibration");
                                if(file.exists()&&!alreadyDeleted)
                                {
                                    file.delete();
                                    alreadyDeleted=true;
                                }
                                FileOutputStream fOut = openFileOutput("calibration", Context.MODE_APPEND); //new FileOutputStream(file, true);
                                OutputStreamWriter out = new OutputStreamWriter(fOut);
                                out.append(Arrays.toString(packetBytes) + "/");
                                out.close();
                                fOut.flush();
                                fOut.close();
                            } catch (IOException e) {
                                Log.e("Exception", "File write failed: " + e.toString());
                            }
                        }
                    }

                    if (packetBytes[2] == 80) {
                        int unsigned = packetBytes[4] & 0xFF;
                        double calculatedValue;
                        String calcVal;
                        DecimalFormat df1 = new DecimalFormat("#.####");
                        df1.setRoundingMode(RoundingMode.CEILING);
                        switch (packetBytes[3]) {
                            case 1:
                                calculatedValue = (unsigned * 0.0025) + 0.18;
                                if (calculatedValue > MainActivity.limitBreaking) {
                                    hardBraking++;
                                    //StoreEventData("Hard breaking: " + unsigned + " Calculated: " + calculatedValue);
                                    calcVal = df1.format(calculatedValue);
                                    StoreEventData("B:" + calcVal);
                                    Log.e("INFO", "Hard breaking: " + unsigned + " Calculated: " + calculatedValue);
                                }
                                break;
                            case 2:
                                calculatedValue = (unsigned * 0.0017) + 0.18;
                                if (calculatedValue > MainActivity.limitSpeedUp) {
                                    heavySpeed++;
                                    //StoreEventData("Heavy speed up: " + unsigned + " Calculated: " + calculatedValue);
                                    calcVal = df1.format(calculatedValue);
                                    StoreEventData("S:" + calcVal);
                                    Log.e("INFO", "Heavy speed up: " + unsigned + " Calculated: " + calculatedValue);
                                }
                                break;
                            case 3:
                                calculatedValue = (unsigned * 0.0025) + 0.18;
                                if (calculatedValue > MainActivity.limitCornering) {
                                    heavyRight++;
                                    //StoreEventData("Heavy right cornering: " + unsigned + " Calculated: " + calculatedValue);
                                    calcVal = df1.format(calculatedValue);
                                    StoreEventData("R:" + calcVal);
                                    Log.e("INFO", "Heavy right cornering: " + unsigned + " Calculated: " + calculatedValue);
                                }
                                break;
                            case 4:
                                calculatedValue = (unsigned * 0.0025) + 0.18;
                                if (calculatedValue > MainActivity.limitCornering) {
                                    heavyLeft++;
                                    //StoreEventData("Heavy left cornering: " + unsigned + " Calculated: " + calculatedValue);
                                    calcVal = df1.format(calculatedValue);
                                    StoreEventData("L:" + calcVal);
                                    Log.e("INFO", "Heavy left cornering: " + unsigned + " Calculated: " + calculatedValue);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    if (packetBytes[2] == 67) {
                        if (packetBytes.length > 7) {
                            for (int i = 0; i < packetBytes.length; i = i + 7) {
                                byte[] splited = Arrays.copyOfRange(packetBytes, i, i + 7);
                                if (splited[2] == 67) {
                                    Log.e("Splitted", Arrays.toString(splited));
                                    ObjectNode postData = new ObjectMapper().createObjectNode();
                                    postData.setAll(getDataFromDongle(splited));
                                    ObjectNode postDataSms = new ObjectMapper().createObjectNode();
                                    postDataSms.setAll(getDataForSms(splited));
                                    PostData pd = new PostData();
                                    String url = "https://52.39.108.83:8004/data";
                                    if (internetConnection) {
                                        try {
                                            pd.postToCollector(url, postData);
                                        } catch (Exception e) {
                                        }
                                    }
                                    if (splited[6] == 4) {
                                        crashIndex = "4";
                                        sendSMSMessage(postDataSms);
                                        try {
                                            Thread.sleep(3000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        call();
                                        crash = false;
                                        break;
                                    }
                                    if (splited[6] == 3) {
                                        crashIndex = "3";
                                        if (!crash) {
                                            sendSMSMessage(postDataSms);
                                        }
                                        crash = true;
                                    }
                                }
                            }
                        } else {
                            ObjectNode postData = new ObjectMapper().createObjectNode();
                            postData.setAll(getDataFromDongle(packetBytes));
                            ObjectNode postDataSms = new ObjectMapper().createObjectNode();
                            postDataSms.setAll(getDataForSms(packetBytes));
                            PostData pd = new PostData();
                            String url = "https://52.39.108.83:8004/data";
                            if (internetConnection) {
                                try {
                                    pd.postToCollector(url, postData);
                                } catch (Exception e) {
                                }
                            }
                            if (packetBytes[6] == 4) {
                                crashIndex = "4";
                                sendSMSMessage(postDataSms);
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                call();
                                crash = false;
                                break;
                            }
                            if (packetBytes[6] == 3) {
                                crashIndex = "3";
                                if (!crash) {
                                    sendSMSMessage(postDataSms);
                                }
                                crash = true;
                            }
                        }
                    }
                    //}
                } catch (IOException e) {

                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    try {
                        MainActivity.instance.finish();
                    } catch (Exception e1) {
                    }
                    finishReading();
                    try {
                        connectedInputStream.close();
                        connectedOutputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }
            }
            try {
                crash = false;
                MainActivity.instance.finish();
            } catch (Exception e1) {
            }
            finishReading();
        }

        //read data from dongle
        private ObjectNode getDataFromDongle(byte[] buffer) throws InterruptedException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
            ObjectNode postData = new ObjectMapper().createObjectNode();
            postData.put("sensorId", dongleName + " - Crash");
            for (int i = 0; i < 7; i++) {
                if (i > 2) {
                    switch (i) {
                        case 3:
                            postData.put("event_id", buffer[i]);
                            break;
                        case 4:
                            postData.put("number_of_message", buffer[i]);
                            break;
                        case 5:
                            postData.put("crash_index", buffer[i]);
                            break;
                        case 6:
                            postData.put("crash_status", buffer[i]);
                            break;
                        default:
                            break;
                    }
                }
            }
            Thread.sleep(500);
            if (MainActivity.mLastLocation != null) {
                postData.put("location", MainActivity.mLastLocation.getLatitude() + "," + MainActivity.mLastLocation.getLongitude());
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formattedDate = df.format(new Date());
            postData.put("date", formattedDate);
            return postData;
        }

        public byte[] hexStringToByteArray(String s) {
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }
            return data;
        }

        //read data from dongle for SMS sending
        private ObjectNode getDataForSms(byte[] buffer) throws InterruptedException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
            ObjectNode postData = new ObjectMapper().createObjectNode();
            postData.put("sensorId", dongleName + " - Crash");
            for (int i = 0; i < 7; i++) {
                if (i > 2) {
                    switch (i) {
                        case 3:
                            postData.put("event_id", buffer[i]);
                            break;
                        case 5:
                            postData.put("crash_index", buffer[i]);
                            break;
                        case 6:
                            postData.put("crash_status", buffer[i]);
                            break;
                        default:
                            break;
                    }
                }
            }
            Thread.sleep(500);
            if (MainActivity.mLastLocation != null) {
                postData.put("location", MainActivity.mLastLocation.getLatitude() + "," + MainActivity.mLastLocation.getLongitude());
            }
            return postData;
        }

        private void StoreEventData(String event) throws IOException {
            String path =
                    Environment.getExternalStorageDirectory() + File.separator + "PHYDData";
            // Make sure the path directory exists.
            File folder = new File(path);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, "PHYDEvents.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            // Save your stream, don't forget to flush() it before closing it.
            try {
                FileOutputStream fOut = new FileOutputStream(file, true);
                OutputStreamWriter out = new OutputStreamWriter(fOut);
                out.append(event);
                out.append(",");
                out.close();
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }

        //load xml for Mac address of bluetooth device
        public Object loadClassFileCalib() {
            try {
                ObjectInputStream ois = new ObjectInputStream(openFileInput("calibration.xml"));
                Object o = ois.readObject();
                return o;

            } catch (Exception ex) {
                Log.v("Calibration", ex.getMessage());

                ex.printStackTrace();
            }
            return null;
        }

        //write Mac address of device to file macAddress.xml
        private void writeToFileCalib(Object buffer) {
            try {
                ObjectOutputStream outputStreamWriter = new ObjectOutputStream(openFileOutput("calibration.xml", Context.MODE_PRIVATE));
                outputStreamWriter.writeObject(buffer);
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }

    }

    /*Finish reading from TEP sensor*/
    public void finishReading() {
        try {
            unregisterReceiver(mReceiver);
            if (startTime != null) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                endTime = df.format(new Date());
                SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                dateOnly = df2.format(new Date());
                try {
                    Date dateEnd = df.parse(endTime);
                    Date dateStart = df.parse(startTime);
                    printDifference(dateStart, dateEnd);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                try {
                    String path =
                            Environment.getExternalStorageDirectory() + File.separator + "PHYDData";
                    // Make sure the path directory exists.
                    File folder = new File(path);
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }

                    File file = new File(folder, "storedData.txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    DecimalFormat df1 = new DecimalFormat("#.####");
                    df1.setRoundingMode(RoundingMode.CEILING);
                    String dist = df1.format(MainActivity.distance / 1000);
                    FileOutputStream fOut = new FileOutputStream(file, true);
                    OutputStreamWriter out = new OutputStreamWriter(fOut);
                    out.append(ConnectBTActivity.dongleName + " - Summary" + "," + dist + "," + ConnectBTActivity.startTime +
                            "," + ConnectBTActivity.endTime + "," + ConnectBTActivity.elapsedTime + "," + String.format("%.4f", ConnectBTActivity.averageSpeed) +
                            "," + MainActivity.countDangerous + "," + hardBraking + "," + heavySpeed + "," + heavyRight + "," + heavyLeft + "," + ConnectBTActivity.dateOnly + "/");
                    out.close();
                    fOut.flush();
                    fOut.close();
                    if (MainActivity.distance != 0D)
                        MainActivity.distance = 0D;
                    hardBraking = 0;
                    heavySpeed = 0;
                    heavyLeft = 0;
                    heavyRight = 0;
                    if (MainActivity.countDangerous != 0)
                        MainActivity.countDangerous = 0;
                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }

                if (internetConnection) {
                    String readString = readFromFile("storedData.txt");
                    String[] arrayToPost = readString.split("/");
                    AsynPostDataFromFile myTask = new AsynPostDataFromFile();
                    try {
                        myTask.execute(arrayToPost);
                    } catch (Exception ex) {
                    }
                    String readEvents = readFromFile("PHYDEvents.txt");
                    String[] arrayEvents = readEvents.split(",");
                    AsyncPostDataEvents myAsync = new AsyncPostDataEvents();
                    try {
                        myAsync.execute(arrayEvents);
                    } catch (Exception ex) {
                    }
                } else {

                }
                if (myThreadConnectBTdevice != null) {
                    myThreadConnectBTdevice.cancel();
                }
            } else {
                if (myThreadConnectBTdevice != null) {
                    myThreadConnectBTdevice.cancel();
                }
            }
            MainActivity.isActivityRunning = false;
            isActive = false;
            PackageManager pm = getPackageManager();
            Intent i = pm.getLaunchIntentForPackage(getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        } catch (IllegalArgumentException e) {

        }

    }

    /*Finish reading from TEP sensor and close activity*/
    public void finishReadingAndActivity() {
        try {
            unregisterReceiver(mReceiver);

            if (startTime != null) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                endTime = df.format(new Date());
                SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                dateOnly = df2.format(new Date());
                try {
                    Date dateEnd = df.parse(endTime);
                    Date dateStart = df.parse(startTime);
                    printDifference(dateStart, dateEnd);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                try {
                    String path =
                            Environment.getExternalStorageDirectory() + File.separator + "PHYDData";
                    // Make sure the path directory exists.
                    File folder = new File(path);
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }

                    File file = new File(folder, "storedData.txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    DecimalFormat df1 = new DecimalFormat("#.####");
                    df1.setRoundingMode(RoundingMode.CEILING);
                    String dist = df1.format(MainActivity.distance / 1000);
                    FileOutputStream fOut = new FileOutputStream(file, true);
                    OutputStreamWriter out = new OutputStreamWriter(fOut);
                    out.append(ConnectBTActivity.dongleName + " - Summary" + "," + dist + "," + ConnectBTActivity.startTime +
                            "," + ConnectBTActivity.endTime + "," + ConnectBTActivity.elapsedTime + "," + String.format("%.4f", ConnectBTActivity.averageSpeed) +
                            "," + MainActivity.countDangerous + "," + hardBraking + "," + heavySpeed + "," + heavyRight + "," + heavyLeft + "," + ConnectBTActivity.dateOnly + "/");
                    out.close();
                    fOut.flush();
                    fOut.close();
                    if (MainActivity.distance != 0D)
                        MainActivity.distance = 0D;
                    hardBraking = 0;
                    heavySpeed = 0;
                    heavyLeft = 0;
                    heavyRight = 0;
                    if (MainActivity.countDangerous != 0)
                        MainActivity.countDangerous = 0;
                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }

                if (internetConnection) {
                    String readString = readFromFile("storedData.txt");
                    String[] arrayToPost = readString.split("/");
                    AsynPostDataFromFile myTask = new AsynPostDataFromFile();
                    try {
                        myTask.execute(arrayToPost);
                    } catch (Exception ex) {
                    }
                    String readEvents = readFromFile("PHYDEvents.txt");
                    String[] arrayEvents = readEvents.split(",");
                    AsyncPostDataEvents myAsync = new AsyncPostDataEvents();
                    try {
                        myAsync.execute(arrayEvents);
                    } catch (Exception ex) {
                    }
                } else {

                    // Save your stream, don't forget to flush() it before closing it.

                }
                if (myThreadConnectBTdevice != null) {
                    myThreadConnectBTdevice.cancel();
                }
            } else {
                if (myThreadConnectBTdevice != null) {
                    myThreadConnectBTdevice.cancel();
                }
            }
            MainActivity.isActivityRunning = false;
            isActive = false;
            finish();
        } catch (IllegalArgumentException e) {

        }


    }

    /*Reading from file in folder PHYDData*/
    private String readFromFile(String fileName) {

        String ret = "";

        try {
            String path =
                    Environment.getExternalStorageDirectory() + File.separator + "PHYDData";
            // Make sure the path directory exists.
            File folder = new File(path);
            File file = new File(folder, fileName);
            FileInputStream in = new FileInputStream(file);
            InputStream inputStream = in;

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    /*Reading from internal storage file calibration*/
    private String readFromFileCalib(String fileName) {

        String ret = "";

        try {
           /* String path =
                    Environment.getExternalStorageDirectory() + File.separator + ".autocalib";
            // Make sure the path directory exists.
            File folder = new File(path);*/
            File file = new File(getFilesDir(), fileName);
            FileInputStream in = new FileInputStream(file);
            InputStream inputStream = in;

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    //call emergency center
    private void call() {
        PhoneNumber phoneNumber = (PhoneNumber) loadClassFileEmerNumber();
        if (phoneNumber != null) {
            emergencyNumber = phoneNumber.getPhoneNumber();
        }
        Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + emergencyNumber));
        try {
            startActivity(in);
        } catch (android.content.ActivityNotFoundException ex) {

            Toast.makeText(getApplicationContext(), "yourActivity is not founded", Toast.LENGTH_SHORT).show();
        }
    }

    //send sms to emergency center
    protected void sendSMSMessage(ObjectNode post) {
        PhoneNumber phoneNumber = (PhoneNumber) loadClassFileEmerNumber();
        if (phoneNumber != null) {
            emergencyNumber = phoneNumber.getPhoneNumber();
        }
        SmsManager.getDefault().sendTextMessage(emergencyNumber, null, post.toString(), null, null);
    }

    //check if device is connected to the internet
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    //load xml file Emergency Phone number
    public Object loadClassFileEmerNumber() {
        try {
            ObjectInputStream ois = new ObjectInputStream(openFileInput("phoneNumber.xml"));
            Object o = ois.readObject();
            return o;

        } catch (Exception ex) {
            Log.v("MacAddress", ex.getMessage());

            ex.printStackTrace();
        }
        return null;
    }

    //enable permissions for Calling,Send_SMS and GPS_Location
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myThreadConnected != null && MainActivity.backButton) {
            finishReadingAndActivity();
            MainActivity.backButton = false;
        } else if (MainActivity.backButton) {
            try {
                unregisterReceiver(mReceiver);
                if (myThreadConnectBTdevice != null) {
                    myThreadConnectBTdevice.cancel();
                }
                {
                    if (myThreadConnectBTdevice != null) {
                        myThreadConnectBTdevice.cancel();
                    }
                }
                MainActivity.isActivityRunning = false;
                isActive = false;
                MainActivity.backButton = false;
                PackageManager pm = getPackageManager();
                Intent i = pm.getLaunchIntentForPackage(getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            } catch (IllegalArgumentException e) {

            }
        }
    }
}