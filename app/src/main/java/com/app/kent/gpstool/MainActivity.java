package com.app.kent.gpstool;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements LocationListener{
    private static final String TAG = "MainActivity";
    private Switch mSwitch;
    private CheckBox mCheckBox;
    private LocationManager mLocationManager;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setListener();

    }

    private void initView() {
        intent = new Intent(MainActivity.this, GpsService.class);
        mSwitch = (Switch) findViewById(R.id.switch_gps);
        mCheckBox = (CheckBox) findViewById(R.id.cb_show);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    private void setListener() {
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                1000, 0, MainActivity.this);
                    } else {
                        Toast.makeText(MainActivity.this, "Please turn on GPS", Toast.LENGTH_SHORT)
                                .show();
                        mSwitch.setChecked(false);
                    }
                } else {
                    mLocationManager.removeUpdates(MainActivity.this);
                }
            }
        });

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {

                    startService(intent);
                } else {
                    stopService(intent);
                }
            }
        });
    }

    private void showMessage() {




    }


    @Override
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {

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
}
