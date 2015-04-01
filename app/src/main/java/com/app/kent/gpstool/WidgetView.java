package com.app.kent.gpstool;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kent_Zheng on 2015/3/31.
 */
public class WidgetView extends AppWidgetProvider {
    private final static String TAG = "WidgetView";
    private static LocationManager mLM;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new UpdateService(context, appWidgetManager), 1, 30000);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public class UpdateService extends TimerTask implements LocationListener {
        private LocationManager mLocationManager;
        private String lat, lon, altitude, speed, accuracy, bearing;
        private String time;
        private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        private RemoteViews remoteViews;
        private AppWidgetManager appWidgetManager;
        private ComponentName thisWidget;


        UpdateService(Context context,  AppWidgetManager appWidgetManager) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            thisWidget = new ComponentName(context, WidgetView.class);
            this.appWidgetManager = appWidgetManager;
            mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        }

        @Override
        public void run() {
            Log.d(TAG, "run");
            updateView();
        }

        private void updateView() {
            if (lat != null && !lat.equals("") && lon != null && !lon.equals("")) {
                remoteViews.setTextViewText(R.id.tv_widget, "Lat: " + lat + ", Lon:" + lon);
            } else {
                remoteViews.setTextViewText(R.id.tv_widget, "Lat: , Lon:");
            }
            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }


        private String doubleToString(double value, int decimals) {
            String result = Double.toString(value);
            // truncate to specified number of decimal places
            int dot = result.indexOf('.');
            if (dot > 0) {
                int end = dot + decimals + 1;
                if (end < result.length()) {
                    result = result.substring(0, end);
                }
            }
            return result;
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Widget: onLocationChanged");
            lat = doubleToString(location.getLatitude(), 4);
            lon = doubleToString(location.getLongitude(), 4);

            time = sdf.format(location.getTime());

            //altitude = location.getAltitude();
            altitude = doubleToString(location.getAltitude(), 1);


            //accuracy = location.getAccuracy();
            accuracy = doubleToString(location.getAccuracy(), 1);

            //bearing = location.getBearing();
            bearing = doubleToString(location.getBearing(), 1);

            //speed = location.getSpeed();
            speed = doubleToString(location.getSpeed() * 3.6, 1);

            Log.d(TAG, "lat = " + lat);
            Log.d(TAG, "lon = " + lon);
            Log.d(TAG, "time = " + time);
            Log.d(TAG, "altitude = " + altitude);
            Log.d(TAG, "accuracy = " + accuracy);
            Log.d(TAG, "bearing = " + bearing);
            Log.d(TAG, "speed = " + speed);

            updateView();
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
}
