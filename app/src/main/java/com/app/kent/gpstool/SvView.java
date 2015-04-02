package com.app.kent.gpstool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Iterator;

/**
 * Created by Kent_Zheng on 2015/3/30.
 */
public class SvView extends View implements LocationListener, GpsStatus.Listener {
    private static final String TAG = "SvView";
    private Context mContext;
    private LocationManager mLocationManager;
    private GpsStatus mGpsSatus;
    private Iterable<GpsSatellite> gs;
    private int NUM_SATELLITES = 32;

    private int[] mPrns = new int[NUM_SATELLITES];
    private float[] mSnrs = new float[NUM_SATELLITES];
    private float[] mElevations = new float[NUM_SATELLITES];
    private float[] mAzimuths = new float[NUM_SATELLITES];
    private boolean[] mEphemerisMask = new boolean[NUM_SATELLITES];
    private boolean[] mAlmanacMask = new boolean[NUM_SATELLITES];
    private boolean[] mUsedInFixMask = new boolean[NUM_SATELLITES];


    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    private String sv, cn;
    private String lat, lon, altitude, speed, accuracy, bearing;
    private String time;
    private Paint mPaint, mAddress, mAltAcc, mTime;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                updateDisplay();
                Message m = obtainMessage(1);
                sendMessageDelayed(m, 1000);
            }
        }
    };
    private boolean isAttach = false;

    public SvView(Context context) {
        super(context);
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addGpsStatusListener(this);
        mGpsSatus = mLocationManager.getGpsStatus(null);

        setPadding(4, 4, 0, 0);

        float density = mContext.getResources().getDisplayMetrics().density;
        Log.d(TAG, "density = " + density);
        int textSize;
        if (density < 1) {
            textSize = 9;
        } else {
            textSize = (int)(10 * density);
            if (textSize < 10) {
                textSize = 10;
            }
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(textSize);
        mPaint.setARGB(255, 255, 255, 255);

        mAddress = new Paint();
        mAddress.setAntiAlias(true);
        mAddress.setTextSize(textSize);
        mAddress.setARGB(255, 128, 255, 128);

        mAltAcc = new Paint();
        mAltAcc.setAntiAlias(true);
        mAltAcc.setTextSize(textSize);
        mAltAcc.setARGB(255, 255, 128, 128);

        mTime = new Paint();
        mTime.setAntiAlias(true);
        mTime.setTextSize(textSize);
        mTime.setARGB(255, 128, 128, 255);
        mTime.setShadowLayer(2, 0, 0, 0xff000000);
    }


    public void startGps() {
        Log.d(TAG, "startGps");
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        updateDisplay();
    }

    public void stopGps() {
        mLocationManager.removeUpdates(this);
        if(isAttach) {
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        }
        //mLocationManager.removeGpsStatusListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttach = true;
        Log.d(TAG, "onAttachedToWindow");
        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        mHandler.sendEmptyMessage(1);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttach = false;
        Log.d(TAG, "onDetachedFromWindow");
//        mLocationManager.removeUpdates(this);
        mHandler.removeMessages(1);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int y = getPaddingTop() - (int)mPaint.ascent();

        if(lat != null && !lat.equals("") && lon != null && !lon.equals("")) {
            canvas.drawText("Lat: " + lat + ", Lon: " + lon, 0, y, mAddress);
            canvas.drawText("Alt: " + altitude + ", Acc: " + accuracy, 0, 2*y, mAltAcc);
            canvas.drawText("Time: " + time, 0, 3*y, mTime);
            canvas.drawText("Total SV: " + sv + ", AVG: " + cn, 0, 4*y, mPaint);
        } else if(sv != null && !sv.equals("") && cn != null && !cn.equals("")){
            canvas.drawText("Total SV: " + sv + ", AVG: " + cn, 0, y, mPaint);
        } else {
            canvas.drawText("Total SV:  , AVG: ", 0, y, mPaint);
        }
    }

    public void updateDisplay() {
        invalidate();
    }

    @Override
    public void onGpsStatusChanged(int event) {
        int count = 0;
        float temp = 0;
        float avg;
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                mGpsSatus = mLocationManager.getGpsStatus(mGpsSatus);
                gs = mGpsSatus.getSatellites();
                Iterator it = gs.iterator();
                GpsSatellite mGpsSatelliteData = null;

                while (it.hasNext()) {
                    mGpsSatelliteData = (GpsSatellite) it.next();
                    mPrns[count] = mGpsSatelliteData.getPrn();
                    mSnrs[count] = mGpsSatelliteData.getSnr();
                    mElevations[count] = mGpsSatelliteData.getElevation();
                    mAzimuths[count] = mGpsSatelliteData.getAzimuth();
                    mEphemerisMask[count] = mGpsSatelliteData.hasEphemeris();
                    mAlmanacMask[count] = mGpsSatelliteData.hasAlmanac();
                    mUsedInFixMask[count] = mGpsSatelliteData.usedInFix();
                    count++;
                }
                for (int i = 0; i < count; i++) {
                    temp += mSnrs[i];
                }
                if (count > 0) {
                    avg = temp / count;
                    sv = String.valueOf(count);
                    cn = doubleToString(avg, 1);
                }
                break;
        }
    }

    private static String doubleToString(double value, int decimals) {
        String result = Double.toString(value);
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
        lat = doubleToString(location.getLatitude(), 4);
        lon = doubleToString(location.getLongitude(), 4);

        time = mSimpleDateFormat.format(location.getTime());
        altitude = doubleToString(location.getAltitude(), 1);
        accuracy = doubleToString(location.getAccuracy(), 1);
        bearing = doubleToString(location.getBearing(), 1);
        speed = doubleToString(location.getSpeed() * 3.6, 1);
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