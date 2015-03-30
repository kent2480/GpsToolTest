package com.app.kent.gpstool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements ServiceConnection{
    private static final String TAG = "MainActivity";
    private Switch mSwitch;
    private CheckBox mCheckBox;
    private LocationManager mLocationManager;
    private Messenger mServiceMessenger = null;
    private ServiceConnection mConnection = this;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setListener();

    }

    private void initView() {
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
                        bindService(new Intent(MainActivity.this, GpsService.class), mConnection,
                                Context.BIND_AUTO_CREATE);
                    } else {
                        Toast.makeText(MainActivity.this, "Please turn on GPS", Toast.LENGTH_SHORT)
                                .show();
                        mSwitch.setChecked(false);
                    }
                } else {
                    unbindService(mConnection);
                    mCheckBox.setChecked(false);
                }
            }
        });

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    sendMessageToService(GpsService.MSG_DISPLAY_SV);
                } else {
                    sendMessageToService(GpsService.MSG_STOP_DISPLAY_SV);
                }
            }
        });
    }

    private void sendMessageToService(int value) {
        try {
            Message msg = Message.obtain(null, value, 0, 0);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected");
        mServiceMessenger = new Messenger(service);
        try {
            Message msg = Message.obtain(null, 0);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }


    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected");
        // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
        mServiceMessenger = null;
    }

    /**
     * Handle incoming messages from MyService
     */
    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // Log.d(LOGTAG,"IncomingHandler:handleMessage");
            switch (msg.what) {

                default:
                    super.handleMessage(msg);
            }
        }
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
}
