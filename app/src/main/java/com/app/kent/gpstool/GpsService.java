package com.app.kent.gpstool;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

public class GpsService extends Service {
    private static final String TAG = "GpsService";
    private WindowManager wm;
    private WindowManager.LayoutParams params;

    public static final int MSG_DISPLAY_SV = 1;
    public static final int MSG_STOP_DISPLAY_SV = 2;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    public SvView mSvView = null;

    public GpsService() {

    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        mSvView = new SvView(this);

        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.END | Gravity.TOP;
//        params.setTitle("123");
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        mSvView.startGps();
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mSvView.stopGps();
        Log.d(TAG, "onUnbind");

        return true;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(mView);
//        mView = null;
    }

    public void display(boolean value) {
        if(wm != null) {
            if(value) {
                wm.addView(mSvView, params);
            } else {
                wm.removeView(mSvView);
            }
        }
    }


    private class IncomingMessageHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"handleMessage: " + msg.what);
            switch (msg.what) {
                case MSG_DISPLAY_SV:
                    display(true);
                    break;
                case MSG_STOP_DISPLAY_SV:
                    display(false);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

//    public class LoadView extends View  implements LocationListener, GpsStatus.Listener {
//        private StringBuilder mStr = new StringBuilder();
//        private Paint mLoadPaint = new Paint();
//
//        private Handler mHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                if (msg.what == 1) {
//                    Log.d(TAG, "handleMessage");
//
//                    updateDisplay();
//                    Message m = obtainMessage(1);
//                    sendMessageDelayed(m, 1000);
//                }
//            }
//        };
//
//        public LoadView(Context context) {
//            super(context);
//        }
//
////        public void startGps() {
////            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
////            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
////            mLocationManager.addGpsStatusListener(this);
////            mGpsSatus = mLocationManager.getGpsStatus(null);
////
////            setPadding(4, 4, 4, 4);
////
////            mLoadPaint.setAntiAlias(true);
////            mLoadPaint.setTextSize(30);
////            mLoadPaint.setARGB(255, 255, 255, 255);
////            updateDisplay();
////        }
////
////        public void stopGps() {
////            mLocationManager.removeUpdates(this);
////            mLocationManager.removeGpsStatusListener(this);
////        }
//
//        @Override
//        protected void onAttachedToWindow() {
//            super.onAttachedToWindow();
//            Log.d(TAG,"onAttachedToWindow");
//            mHandler.sendEmptyMessage(1);
//        }
//
//        @Override
//        protected void onDetachedFromWindow() {
//            super.onDetachedFromWindow();
//            Log.d(TAG, "onDetachedFromWindow");
//            mHandler.removeMessages(1);
//        }
//
//        @Override
//        public void onDraw(Canvas canvas) {
//            Log.d(TAG, "onDraw: mStr = " + mStr.toString());
//
//            canvas.drawText(mStr.toString(), 30.0f, 30.0f, mLoadPaint);
//            mStr.delete(0, mStr.length());
//        }
//
//
//        public void updateDisplay() {
//            invalidate();
//        }
//
//        @Override
//        public void onLocationChanged(Location location) {
//            double latitude = location.getLatitude();
//            double longitude = location.getLongitude();
//            long time = location.getTime();
//
//            //mStr.append("latitude: " + latitude + ", longitude: " + longitude);
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//
//        }
//
//        @Override
//        public void onGpsStatusChanged(int event) {
//            int count = 0;
//            float temp = 0;
//            float avg = 0;
//            switch(event) {
//                case GpsStatus.GPS_EVENT_STARTED:
//                    break;
//                case GpsStatus.GPS_EVENT_STOPPED:
//                    break;
//                case GpsStatus.GPS_EVENT_FIRST_FIX:
//                    break;
//                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
//
//                    mGpsSatus = mLocationManager.getGpsStatus(mGpsSatus);
//                    gs = mGpsSatus.getSatellites();
//                    Iterator it = gs.iterator();
//                    GpsSatellite mGpsSatelliteData = null;
//
//                    while (it.hasNext()) {
//                        mGpsSatelliteData = (GpsSatellite) it.next();
//                        mPrns[count] = mGpsSatelliteData.getPrn();
//                        mSnrs[count] = mGpsSatelliteData.getSnr();
//                        mElevations[count] = mGpsSatelliteData.getElevation();
//                        mAzimuths[count] = mGpsSatelliteData.getAzimuth();
//                        mEphemerisMask[count] = mGpsSatelliteData.hasEphemeris();
//                        mAlmanacMask[count] = mGpsSatelliteData.hasAlmanac();
//                        mUsedInFixMask[count] = mGpsSatelliteData.usedInFix();
//                        count++;
//                    }
//
//                    for(int i = 0 ; i < count; i++) {
//                        temp += mSnrs[i];
//                    }
//                    avg = temp/count;
//                    mStr.append("Total SV: " + count + ", AVG = " + avg);
//                    break;
//
//            }
//        }
//    }
}
