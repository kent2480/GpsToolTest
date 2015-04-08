package com.app.kent.gpstool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
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
import java.util.ArrayList;
import java.util.Arrays;
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
    private int drawCount = 0;
    private int textSize = 15;
    private boolean isAttach = false;
    private float LINE_STROKE_WDTH = 1.0f;
    // if true => display 6 cn address. Otherwise 12 cn address;
    private boolean isBigSize = false;
    private int scale = 1;


    private ArrayList<Float> mTop4;
    private int[] mPrns = new int[NUM_SATELLITES];
    private float[] mSnrs = new float[NUM_SATELLITES];
    private float[] mElevations = new float[NUM_SATELLITES];
    private float[] mAzimuths = new float[NUM_SATELLITES];
    private boolean[] mEphemerisMask = new boolean[NUM_SATELLITES];
    private boolean[] mAlmanacMask = new boolean[NUM_SATELLITES];
    private boolean[] mUsedInFixMask = new boolean[NUM_SATELLITES];

    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    private String sv, cnAll, cn4;
    private String lat, lon, altitude, speed, accuracy, bearing;
    private String time;
    private Paint mGraph, mCircle, mLine, mPathLine, mPaint, mAddress, mAltAcc, mTime;
    private Path mPath;
    private int startLineX = 0, startLineY = 1, endLineX = 400, endLineY = 101, lineSpace = 20,
            pointSpace = 80;

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


    public SvView(Context context) {
        super(context);
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addGpsStatusListener(this);
        mGpsSatus = mLocationManager.getGpsStatus(null);

        setPadding(4, 4, 0, 0);

        mGraph = new Paint();
        mGraph.setAntiAlias(true);
        mGraph.setStyle(Paint.Style.STROKE);
        mGraph.setStrokeWidth(LINE_STROKE_WDTH);
        mGraph.setARGB(255, 128, 128, 128);

        mCircle = new Paint();
        mCircle.setAntiAlias(true);
        mCircle.setStrokeWidth(4.0f);
        mCircle.setARGB(255, 128, 255, 128);

        mLine = new Paint();
        mLine.setAntiAlias(true);
        mLine.setStyle(Paint.Style.STROKE);
        mLine.setStrokeWidth(2.0f);
        mLine.setARGB(255, 128, 255, 128);
        mLine.setPathEffect(new CornerPathEffect(5)); //affect line address

        mPathLine = new Paint();
        mPathLine.setAntiAlias(true);
        mPathLine.setStyle(Paint.Style.STROKE);
        mPathLine.setStrokeWidth(4.0f);
        mPathLine.setARGB(255, 255, 128, 128);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(textSize);
        mPaint.setARGB(255, 253, 202, 81);

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

        mPath = new Path();

        checkDisplay();
    }

    public void startGps() {
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

    public  void changeDisplaySize(boolean isBig) {
        this.isBigSize = isBig;
        checkDisplay();
    }

    public void checkDisplay() {
        if(isBigSize) {
            scale = 2;
        } else {
            scale = 1;
        }
        mTop4 = new ArrayList<Float>(6 * scale);

        for (int i = 0; i < 6 * scale; i++) {
            mTop4.add(0.0f);
        }
        drawCount = 0;
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

        for (int i = 0; i < 6; i++) {
            canvas.drawLine(startLineX, startLineY + i * lineSpace,
                            endLineX * scale, startLineY + i * lineSpace, mGraph);
        }

        mPath.reset();

        if(cn4 != null) {
            mTop4.set(drawCount, Float.parseFloat(cn4));
            mPath.moveTo(startLineX, endLineY - mTop4.get(0)*2);

            for (int i = 0; i <= drawCount; i++) {
                mPath.lineTo(i * pointSpace, endLineY - mTop4.get(i)*2);
                canvas.drawCircle(i * pointSpace, endLineY - mTop4.get(i) * 2, 5.0f, mCircle);

                if(drawCount == (5  * scale) && i < (drawCount  * scale)) {
                    mTop4.set(i, mTop4.get(i + 1));
                }
            }


            if(drawCount < (5  * scale)) {
                drawCount++;
            }
        }

//        Log.d(TAG, "mTop4 = " + mTop4 + ", drawCount = " + drawCount);


        canvas.drawPath(mPath, mLine);

        if(lat != null && !lat.equals("") && lon != null && !lon.equals("")) {
            canvas.drawText("Lat: " + lat + ", Lon: " + lon, 0, endLineY + y, mAddress);
            canvas.drawText("Alt: " + altitude + ", Acc: " + accuracy + ", Time: " + time, 0,
                    endLineY + 2*y, mAltAcc);
            canvas.drawText("Total SV: " + sv + ", AVG: " + cnAll + ", Top4: " + cn4, 0, endLineY + 3*y,
                    mPaint);
        } else if(sv != null && !sv.equals("") && cnAll != null && !cnAll.equals("")){
            canvas.drawText("Total SV: " + sv + ", AVG: " + cnAll + ", Top4: " + cn4, 0, endLineY + y,
                    mPaint);
        } else {
            canvas.drawText("Total SV:  , AVG: " + ", Top4", 0, 101 + y, mPaint);
        }
    }

    public void updateDisplay() {
        invalidate();
    }

    @Override
    public void onGpsStatusChanged(int event) {
        int count = 0;
        float allSn = 0, top4 = 0;
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

                Arrays.fill(mSnrs, 0);
                while (it.hasNext()) {
                    mGpsSatelliteData = (GpsSatellite) it.next();
                    mPrns[count] = mGpsSatelliteData.getPrn();
                    mSnrs[count] = mGpsSatelliteData.getSnr();
                    mElevations[count] = mGpsSatelliteData.getElevation();
                    mAzimuths[count] = mGpsSatelliteData.getAzimuth();
                    mEphemerisMask[count] = mGpsSatelliteData.hasEphemeris();
                    mAlmanacMask[count] = mGpsSatelliteData.hasAlmanac();
                    mUsedInFixMask[count] = mGpsSatelliteData.usedInFix();

//                    Log.d(TAG, "count: " + count + ", CN = " + mGpsSatelliteData.getSnr());
                    count++;
                }

                Arrays.sort(mSnrs);
                for (int i = mSnrs.length - 1; i >= 0; i--) {
//                    Log.d(TAG, "SN[" + i + "] = " + mSnrs[i]);
                    allSn += mSnrs[i];

                    if((mSnrs.length - i) <= 4) {
                       top4 += mSnrs[i];
                    }
                }

                if (count > 0) {
                    avg = allSn / count;
                    top4 = top4 / 4;
                    sv = String.valueOf(count);
                    cnAll = doubleToString(avg, 1);
                    cn4 = doubleToString(top4, 1);
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