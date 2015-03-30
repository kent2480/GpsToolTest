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


    private StringBuilder mStr = new StringBuilder();
    private Paint mLoadPaint;
    private Paint mAddress;
    private Paint mSystemPaint;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.d(TAG, "handleMessage");

                updateDisplay();
                Message m = obtainMessage(1);
                sendMessageDelayed(m, 1000);
            }
        }
    };

    public SvView(Context context) {
        super(context);
        mContext = context;
    }


    public void startGps() {
        Log.d(TAG, "startGps");
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mLocationManager.addGpsStatusListener(this);
        mGpsSatus = mLocationManager.getGpsStatus(null);

        setPadding(4, 4, 4, 4);



        float density = mContext.getResources().getDisplayMetrics().density;
        Log.d(TAG, "density = " + density);
        int textSize = 10;
        if (density < 1) {
            textSize = 9;
        } else {
            textSize = (int)(10 * density);
            if (textSize < 10) {
                textSize = 10;
            }
        }

        mLoadPaint = new Paint();
        mLoadPaint.setAntiAlias(true);
        mLoadPaint.setTextSize(textSize);
        mLoadPaint.setARGB(255, 255, 255, 255);

        mAddress = new Paint();
        mAddress.setAntiAlias(true);
        mAddress.setTextSize(textSize);
        mAddress.setARGB(255, 128, 255, 128);

        mSystemPaint = new Paint();
        mSystemPaint.setARGB(0x80, 0xff, 0, 0);
        mSystemPaint.setShadowLayer(2, 0, 0, 0xff000000);

        updateDisplay();
    }

    public void stopGps() {
        Log.d(TAG, "stopGps");
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow");
        mHandler.sendEmptyMessage(1);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
        mHandler.removeMessages(1);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: mStr = " + mStr.toString());

        canvas.drawText(mStr.toString(), 30.0f, 30.0f, mLoadPaint);
        canvas.drawText(mStr.toString(), 60.0f, 60.0f, mAddress);
        mStr.delete(0, mStr.length());
    }

    public void updateDisplay() {
        invalidate();
    }

    @Override
    public void onGpsStatusChanged(int event) {
        Log.d(TAG, "onGpsStatusChanged: " + event);
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
                avg = temp / count;
                mStr.append("Total SV: " + count + ", AVG = " + avg);
                Log.d(TAG, "mStr = " + mStr.toString());
                break;
        }
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