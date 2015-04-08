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
    public static final int MSG_START_GPS = 3;
    public static final int MSG_STOP_GPS = 4;
    public static final int MSG_DISPLAY_BIG = 5;
    public static final int MSG_DISPLAY_SMALL = 6;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    public SvView mSvView = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        mSvView = new SvView(this);

        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.END | Gravity.TOP;
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startGps() {
        mSvView.startGps();
    }

    public void stopGps() {
        mSvView.stopGps();
    }

    public void display(boolean value) {
        if (wm != null) {
            if (value) {
                wm.addView(mSvView, params);
            } else {
                wm.removeView(mSvView);
            }
        }
    }

    public void changeDisplaySize(boolean isBig) {
        if(mSvView != null) {
            mSvView.changeDisplaySize(isBig);
        }
    }

    private class IncomingMessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISPLAY_SV:
                    display(true);
                    break;
                case MSG_STOP_DISPLAY_SV:
                    display(false);
                    break;
                case MSG_START_GPS:
                    startGps();
                    break;
                case MSG_STOP_GPS:
                    stopGps();
                    break;
                case MSG_DISPLAY_BIG:
                    changeDisplaySize(true);
                    break;
                case MSG_DISPLAY_SMALL:
                    changeDisplaySize(false);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
