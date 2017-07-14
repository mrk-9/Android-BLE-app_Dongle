package com.example.karimt.belladatidongleapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.belladati.httpclientandroidlib.client.utils.URIBuilder;
import com.belladati.sdk.BellaDati;
import com.belladati.sdk.BellaDatiConnection;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.impl.BellaDatiServiceWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import layout.EmerAndBreakSup;
import layout.HomePage;
import layout.MapPage;
import layout.YearToDatePage;

/**
 * Created by KarimT on 19.09.2016.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private GoogleMap mMap;
    Marker mCurrLocationMarker;
    public static Location mLastLocation;

    public static double distance=0D;
    JsonNode places,limits;
    public List<Double> latitude, longitude;
    public static int countDangerous;
    public String lastDangerousLat, lastDangerousLon, emergencyNumber;
    public static MainActivity instance = null;
    public static BellaDatiService service;
    public static BellaDatiServiceWrapper wrapper;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    public static String phone_number;
    public int domain_id;
    public static double limitBreaking,limitCornering,limitSpeedUp;
    public String dsFilter;
    private PopupWindow pw;
    Button Save;
    public static boolean isActivityRunning = false;
    EditText confPhoneNumber,servicePhone,firstAidPhone;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1; // 1 minutes
    public static boolean backButton;
    public static String savedDongleName="";
    boolean offline;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            offline = b.getBoolean("offline");
        }
        isActivityRunning = true;
        instance = this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            if (ConnectBTActivity.internetConnection) {
                final BellaDatiConnection connection = BellaDati.connectInsecure("https://service.belladati.com");
                ConnectToBD myTask = new ConnectToBD();
                myTask.execute(connection);

                service = myTask.get();
                wrapper = new BellaDatiServiceWrapper(service);
                URIBuilder builderDomainUser = new URIBuilder("api/users/username/carinsuranceadmin");
                GetDomainId(builderDomainUser);

                URIBuilder builderDomain = new URIBuilder("api/domains/" + domain_id);
                GetParamsByDomain(builderDomain);
                URIBuilder builder = new URIBuilder("api/dataSets/32552/data");

                places = MainActivity.wrapper.loadJson(builder.toString()).findPath("data");
                latitude = new ArrayList<>();
                longitude = new ArrayList<>();
                for (int i = 0; i < places.size(); i++) {
                    latitude.add(places.get(i).findPath("M_LATITUDE").asDouble());
                    longitude.add(places.get(i).findPath("M_LONGITUDE").asDouble());
                }

                URIBuilder builderLimits = new URIBuilder("api/dataSets/"+dsFilter+"/data");
                limits = MainActivity.wrapper.loadJson(builderLimits.toString()).findPath("data");
                double limitBreaking=limits.findPath("L_BREAKING").asDouble();
                double limitCornering=limits.findPath("L_CORNERING").asDouble();
                double limitSpeedUp=limits.findPath("L_SPEED_UP").asDouble();
                LimitData limitData = new LimitData();
                limitData.setLimitBreaking(limitBreaking);
                limitData.setLimitCornering(limitCornering);
                limitData.setLimitSpeedUp(limitSpeedUp);
                writeToFileLimits(limitData);
            }
            LimitData limitData1 = (LimitData) loadClassFileLimits();
            if (limitData1 != null) {
                limitBreaking=limitData1.getLimitBreaking();
                limitCornering=limitData1.getLimitCornering();
                limitSpeedUp=limitData1.getLimitSpeedUp();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        PhoneNumber phoneNumber = (PhoneNumber) loadClassFile();
        if (phoneNumber == null) {
            showPopup();
        }
        if(!ConnectBTActivity.dongleName.equals("")) {
            DongleData dongleData = new DongleData();
            dongleData.setDongleName(ConnectBTActivity.dongleName);
            writeToFileDongleName(dongleData);
        }
        DongleData dongleData = (DongleData) loadClassFileDongleName();
        if (dongleData != null) {
            savedDongleName=dongleData.getDongleName();
        }


    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    //return HomePage.newInstance();
                    return EmerAndBreakSup.newInstance();
                case 1:
                    return MapPage.newInstance();
                case 2:
                    return HomePage.newInstance();
                case 3:
                    return YearToDatePage.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.home);
                case 1:
                    return getString(R.string.map);
                case 2:
                    return getString(R.string.stats);
                case 3:
                    return getString(R.string.dashboard);
            }
            return null;
        }
    }

    //get domain id based on the user id
    private void GetDomainId(URIBuilder domainUser)
    {
        wrapper = new BellaDatiServiceWrapper(service);
        JsonNode jnDomainUser = wrapper.loadJson(domainUser.toString());
        domain_id = jnDomainUser.findPath("domain_id").asInt();
    }

    //get params of domain by domain_id
    private void GetParamsByDomain(URIBuilder domain)
    {
        JsonNode jnDomain = wrapper.loadJson(domain.toString());
        phone_number = jnDomain.findPath("Phone_number").asText();
        dsFilter=jnDomain.findPath("dsFilter").asText();
    }

    @Override
    public void finish() {
        super.finish();
        instance = null;
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }
    @Override
    public void onBackPressed() {
        // your code.
        if(offline)
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which)
                    {
                        case DialogInterface.BUTTON_POSITIVE:
                            finish();
                            backButton=true;
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:

                            break;
                    }
                }
            };
            AlertDialog.Builder builder= new AlertDialog.Builder(this);
            builder.setMessage("Start scanning for dongle?").setNegativeButton("No",dialogClickListener).setPositiveButton("Yes",dialogClickListener).show();
        }
        else {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            finish();
                            backButton = true;
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:

                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Close application?").setNegativeButton("No", dialogClickListener).setPositiveButton("Yes", dialogClickListener).show();
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        showPopup();
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //show popup for setup emergency phone number
    private void showPopup() {
        try {
// We need to get the instance of the LayoutInflater
            LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup,
                    (ViewGroup) findViewById(R.id.popup_1));
            confPhoneNumber = (EditText) layout.findViewById(R.id.editEmerNumber);
            servicePhone = (EditText) layout.findViewById(R.id.editServiceNumber);
            firstAidPhone = (EditText) layout.findViewById(R.id.editFirstAidNumber);

            PhoneNumber phoneNumber = (PhoneNumber) loadClassFile();
            if (phoneNumber != null && !phoneNumber.getPhoneNumber().equals("")) {
                confPhoneNumber.setText(phoneNumber.getPhoneNumber());
                servicePhone.setText(phoneNumber.getServiceNumber());
                firstAidPhone.setText(phoneNumber.getFirstAidNumber());
            }

            pw = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

            findViewById(R.id.main_content).post(new Runnable() {
                public void run() {
                    pw.showAtLocation(findViewById(R.id.main_content), Gravity.CENTER, 0, 0);
                    pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            PhoneNumber phoneNumber = (PhoneNumber) loadClassFile();
                            if (phoneNumber == null || phoneNumber.equals("")) {
                                showPopup();
                            }
                        }
                    });
                }
            });

            //pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
            Save = (Button) layout.findViewById(R.id.save_number);
            Save.setOnClickListener(save_button);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //save button for emergency phone number
    private View.OnClickListener save_button = new View.OnClickListener() {
        public void onClick(View v) {
            emergencyNumber = confPhoneNumber.getText().toString();
            if (!emergencyNumber.equals("")) {
                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setPhoneNumber(emergencyNumber);
                phoneNumber.setFirstAidNumber(firstAidPhone.getText().toString());
                phoneNumber.setServiceNumber(servicePhone.getText().toString());
                writeToFile(phoneNumber);
                Toast.makeText(getApplicationContext(), "Successful saved", Toast.LENGTH_LONG).show();
            }
            pw.dismiss();
        }
    };

    //load xml for Mac address of bluetooth device
    public Object loadClassFile() {
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

    //write Mac address of device to file macAddress.xml
    private void writeToFile(Object buffer) {
        try {
            ObjectOutputStream outputStreamWriter = new ObjectOutputStream(openFileOutput("phoneNumber.xml", Context.MODE_PRIVATE));
            outputStreamWriter.writeObject(buffer);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //ALL METHODS BELOW ARE GOOGLE MAPS COORDINATION API!!!
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setFastestInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.HALF_UP);

        Location locationA = new Location("point A");
        locationA.setLatitude(location.getLatitude());
        locationA.setLongitude(location.getLongitude());
        locationA.setAltitude(location.getAltitude());

        if (mLastLocation != null && location.getLatitude() != mLastLocation.getLatitude() && location.getLongitude() != mLastLocation.getLongitude()) {
            String latStringLast = df.format(mLastLocation.getLatitude());
            String lonStringLast = df.format(mLastLocation.getLongitude());
            String latStringCurrent = df.format(location.getLatitude());
            String lonStringCurrent = df.format(location.getLongitude());
            if(!latStringLast.equalsIgnoreCase(latStringCurrent)&&!lonStringLast.equalsIgnoreCase(lonStringCurrent)) {
                distance += locationA.distanceTo(mLastLocation);
                Log.e("Distance: ", Double.toString(distance));
            }
        }
        mLastLocation = location;

        double lat = mLastLocation.getLatitude();
        double lon = mLastLocation.getLongitude();
        String latString = df.format(lat);
        String lonString = df.format(lon);
        if (ConnectBTActivity.internetConnection && latitude!=null) {
            for (int j = 0; j < latitude.size(); j++) {
                String latDangerous = df.format(latitude.get(j));
                String lonDangerous = df.format(longitude.get(j));
                if (latDangerous.equalsIgnoreCase(lastDangerousLat) && lonDangerous.equalsIgnoreCase(lastDangerousLon)) {

                } else {
                    if (latString.equalsIgnoreCase(latDangerous) && lonString.equalsIgnoreCase(lonDangerous)) {
                        countDangerous++;
                        lastDangerousLat = latDangerous;
                        lastDangerousLon = lonDangerous;
                    }
                }
            }
        }
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
    }
    //load xml for Mac address of bluetooth device
    public Object loadClassFileLimits() {
        try {
            ObjectInputStream ois = new ObjectInputStream(openFileInput("limits.xml"));
            Object o = ois.readObject();
            return o;

        } catch (Exception ex) {
            Log.v("MacAddress", ex.getMessage());

            ex.printStackTrace();
        }
        return null;
    }

    //write Mac address of device to file macAddress.xml
    private void writeToFileLimits(Object buffer) {
        try {
            ObjectOutputStream outputStreamWriter = new ObjectOutputStream(openFileOutput("limits.xml", Context.MODE_PRIVATE));
            outputStreamWriter.writeObject(buffer);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    public Object loadClassFileDongleName() {
        try {
            ObjectInputStream ois = new ObjectInputStream(openFileInput("donglename.xml"));
            Object o = ois.readObject();
            return o;

        } catch (Exception ex) {
            Log.v("MacAddress", ex.getMessage());

            ex.printStackTrace();
        }
        return null;
    }

    //write Mac address of device to file macAddress.xml
    private void writeToFileDongleName(Object buffer) {
        try {
            ObjectOutputStream outputStreamWriter = new ObjectOutputStream(openFileOutput("donglename.xml", Context.MODE_PRIVATE));
            outputStreamWriter.writeObject(buffer);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}

