package com.example.karimt.belladatidongleapp;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import layout.HomePage;
import layout.OSMapPage;
import layout.StatsPage;
import layout.YearToDatePage;

/**
 * Created by KarimT on 05.01.2017.
 */
public class MainActivityOsmdroid extends AppCompatActivity implements LocationListener {

    public static Location mLastLocation;
    public static String dist;
    public static float distance;
    JsonNode places;
    public List<Double> latitude, longitude;
    public static int countDangerous;
    public String lastDangerousLat, lastDangerousLon, emergencyNumber;
    public static MainActivityOsmdroid instance = null;
    public static BellaDatiService service;
    public static BellaDatiServiceWrapper wrapper;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    public static String phone_number;
    public int domain_id;
    private PopupWindow pw;
    Button Save;
    public static boolean isActiviyStopped = true;
    private LocationManager lm;
    EditText confPhoneNumber;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isActiviyStopped = false;
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

                places = wrapper.loadJson(builder.toString()).findPath("data");
                latitude = new ArrayList<>();
                longitude = new ArrayList<>();
                for (int i = 0; i < places.size(); i++) {
                    latitude.add(places.get(i).findPath("M_LATITUDE").asDouble());
                    longitude.add(places.get(i).findPath("M_LONGITUDE").asDouble());
                }
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
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            //this fails on AVD 19s, even with the appcompat check, says no provided named gps is available
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0l, 0f, this);
        } catch (Exception ex) {
        }

        try {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0l, 0f, this);
        } catch (Exception ex) {
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
                    return HomePage.newInstance();
                case 1:
                    //return MapPage.newInstance();
                    return OSMapPage.newInstance();
                case 2:
                    return StatsPage.newInstance();
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
    private void GetDomainId(URIBuilder domainUser) {
        wrapper = new BellaDatiServiceWrapper(service);
        JsonNode jnDomainUser = wrapper.loadJson(domainUser.toString());
        domain_id = jnDomainUser.findPath("domain_id").asInt();
    }

    //get params of domain by domain_id
    private void GetParamsByDomain(URIBuilder domain) {
        JsonNode jnDomain = wrapper.loadJson(domain.toString());
        phone_number = jnDomain.findPath("Phone_number").asText();

    }

    @Override
    public void finish() {
        super.finish();
        instance = null;
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
                LayoutInflater inflater = (LayoutInflater) MainActivityOsmdroid.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.popup,
                        (ViewGroup) findViewById(R.id.popup_1));
                confPhoneNumber = (EditText) layout.findViewById(R.id.editEmerNumber);
                PhoneNumber phoneNumber = (PhoneNumber) loadClassFile();
                if (phoneNumber != null && !phoneNumber.getPhoneNumber().equals("")) {
                    confPhoneNumber.setText(phoneNumber.getPhoneNumber());
                }

                pw = new PopupWindow(layout, 800, 800, true);

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
                writeToFile(phoneNumber);
                Toast.makeText(getApplicationContext(), getString(R.string.suc_saved), Toast.LENGTH_LONG).show();
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

    @Override
    public void onLocationChanged(Location location) {

        Location locationA = new Location("point A");

        locationA.setLatitude(location.getLatitude());
        locationA.setLongitude(location.getLongitude());

        if (mLastLocation != null && location.getLatitude() != mLastLocation.getLatitude() && location.getLongitude() != mLastLocation.getLongitude()) {
            distance += mLastLocation.distanceTo(locationA);
            dist = String.format("%.4f", distance);

        }
        mLastLocation = location;
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.HALF_UP);
        double lat = mLastLocation.getLatitude();
        double lon = mLastLocation.getLongitude();
        String latString = df.format(lat);
        String lonString = df.format(lon);
        if (ConnectBTActivity.internetConnection && latitude != null) {
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

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.removeUpdates(this);
        lm = null;
    }
}
