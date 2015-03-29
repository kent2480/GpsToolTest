package com.app.kent.gpstool;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class GpsService extends Service{
    private static final String TAG = "GpsService";
    private View mView;

    public GpsService() {

    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mView = new LoadView(this);

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.END | Gravity.TOP;
        params.setTitle("123");
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        wm.addView(mView, params);
        super.onStart(intent, startId);
        Log.d(TAG, "onStart");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(mView);
        mView = null;
    }

    private class LoadView extends View  implements LocationListener {
        private StringBuilder mStr = new StringBuilder();
        private String temp;

        private Paint mLoadPaint = new Paint();

        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    Log.d(TAG, "handleMessage");
                    mStr.append(temp);
                    temp += "b";
                    updateDisplay();
                    Message m = obtainMessage(1);
                    sendMessageDelayed(m, 1000);
                }

            }
        };

        public LoadView(Context context) {
            super(context);
            setPadding(4, 4, 4, 4);

            mLoadPaint.setAntiAlias(true);
            mLoadPaint.setTextSize(30);
            mLoadPaint.setARGB(255, 255, 255, 255);
            temp += "a";
            updateDisplay();

        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            Log.d(TAG,"onAttachedToWindow");
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
            Log.d(TAG, "onDraw");
            canvas.drawText(mStr.toString(), 30.0f, 30.0f, mLoadPaint);
            mStr.delete(0, mStr.length());
        }


        void updateDisplay() {
            invalidate();
        }

        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            long time = location.getTime();

            mStr.append("latitude: " + latitude + ", longitude: " + longitude);
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
