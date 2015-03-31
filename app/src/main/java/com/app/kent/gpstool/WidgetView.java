package com.app.kent.gpstool;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kent_Zheng on 2015/3/31.
 */
public class WidgetView extends AppWidgetProvider {
    private final static String TAG = "WidgetView";
    private Context mContext;
    private static LocationManager mLM;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");
        mContext = context;

        mLM = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Intent intent = new Intent(mContext, UpdateService.class);
        mContext.startService(intent);

        super.onUpdate(mContext, appWidgetManager, appWidgetIds);
    }

    public static class UpdateService extends Service implements LocationListener {
        private String lat, lon, altitude, speed, accuracy, bearing;
        private String time;
        private SimpleDateFormat mSimpleDateFormat;

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

        @Override
        public void onStart(Intent intent, int startId) {
            super.onStart(intent, startId);
            Log.d(TAG, "onStart");
            mLM.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
            mSimpleDateFormat = new SimpleDateFormat("hh:mm:ss");
            RemoteViews updateViews = new RemoteViews(this.getPackageName(), R.layout.widget);


            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
            updateViews.setTextViewText(R.id.tv_widget, "" + sdf.format(new Date()));
//            if (lat != null && !lat.equals("") && lon != null && !lon.equals("")) {
//                updateViews.setTextViewText(R.id.tv_widget, "Lat: " + lat + ", Lon:" + lon);
//            } else {
//                updateViews.setTextViewText(R.id.tv_widget, "Lat: , Lon:");
//            }
            ComponentName thisWidget = new ComponentName(this, WidgetView.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
        }

        private static String doubleToString(double value, int decimals) {
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

            time = mSimpleDateFormat.format(location.getTime());

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
