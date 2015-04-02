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
    private final int UPDATE_WIDGET_TIME = 15 * 60 * 1000; // 15 min

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new UpdateService(context, appWidgetManager), 1, UPDATE_WIDGET_TIME);
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
        private StringBuilder mStringBuilder;


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
            mStringBuilder = new StringBuilder();
            if (lat != null && !lat.equals("") && lon != null && !lon.equals("")) {
                mStringBuilder.append("Lat:" + lat + ", Lon:" + lon);
            } else {
                mStringBuilder.append("Unknow Location");
            }

            if(altitude != null && !altitude.equals("")) {
                mStringBuilder.append("\n" + "Alt:" + altitude);
            }

            if(accuracy != null && !accuracy.equals("")) {
                mStringBuilder.append(", Acc:" + accuracy);
            }

            if(time != null && !time.equals("")) {
                mStringBuilder.append("\n" + "Time:" + time);
            }

            Log.d(TAG, "mStringBuilder = " + mStringBuilder);

            remoteViews.setTextViewText(R.id.tv_widget, mStringBuilder.toString());
            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }


        private String doubleToString(double value, int decimals, int width) {
            String empty = " ";
            String result = Double.toString(value);
            int dot = result.indexOf('.');
            if (dot > 0) {
                int end = dot + decimals + 1;
                if (end < result.length()) {
                    result = result.substring(0, end);
                }
            }

            if(width > 0) {
                width = width  - result.length();
                for (int i = 0; i < width; i++) {
                    result = empty + result;
                }
            }

            return result;
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Widget: onLocationChanged");
            lat = doubleToString(location.getLatitude(), 4, 9);
            lon = doubleToString(location.getLongitude(), 4, 9);

            time = sdf.format(location.getTime());
            altitude = doubleToString(location.getAltitude(), 1, 9);
            accuracy = doubleToString(location.getAccuracy(), 1, 9);
            bearing = doubleToString(location.getBearing(), 1, 9);
            speed = doubleToString(location.getSpeed() * 3.6, 1, -1);

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
