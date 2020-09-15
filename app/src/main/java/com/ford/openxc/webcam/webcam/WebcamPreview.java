package com.ford.openxc.webcam.webcam;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//import com.socks.library.KLog;

public class WebcamPreview extends SurfaceView implements
        SurfaceHolder.Callback, Runnable {
    private final static String TAG = "WebcamPreview";

    private Rect mViewWindow;
    private volatile boolean mRunning = true;
    private final Object mServiceSyncToken = new Object();
    private WebcamService mWebcamService;
    private SurfaceHolder mHolder;

    public WebcamPreview(Context context) {
        super(context);
        init();
    }

    public WebcamPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Log.w(TAG, "WebcamPreview constructed");
        setFocusable(true);

        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void startPreview(String video) {
//        stopPreview();

        mRunning = true;

        Intent intent = new Intent(getContext(), WebcamService.class);
        intent.putExtra("video", video);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        (new Thread(this)).start();
    }

    public void stopPreview() {
        mRunning = false;

        if(mWebcamService != null) {
            Log.w(TAG, "Unbinding from webcam manager");
            getContext().unbindService(mConnection);
            mWebcamService = null;
        }
    }

    @Override
    public void run() {
        while(mRunning) {
            synchronized(mServiceSyncToken) {
                if(mWebcamService == null) {
                    try {
                        mServiceSyncToken.wait();
                    } catch(InterruptedException e) {
                        break;
                    }
                }

                Bitmap bitmap = mWebcamService.getFrame();
                Canvas canvas = mHolder.lockCanvas();
                if(canvas != null) {
                    drawOnCanvas(canvas, bitmap);
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    protected void drawOnCanvas(Canvas canvas, Bitmap videoBitmap) {
        canvas.drawBitmap(videoBitmap, null, mViewWindow, null);
    }

    protected Rect getViewingWindow() {
        return mViewWindow;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.w(TAG, "Surface created");

        startPreview(WebcamService.VIDEO);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.w(TAG, "Surface destroyed");

        stopPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int winWidth,
            int winHeight) {
        Log.w("WebCam", "surfaceChanged");
        int width, height, dw, dh;
        if(winWidth * 3 / 4 <= winHeight) {
            dw = 0;
            dh = (winHeight - winWidth * 3 / 4) / 2;
            width = dw + winWidth - 1;
            height = dh + winWidth * 3 / 4 - 1;
        } else {
            dw = (winWidth - winHeight * 4 / 3) / 2;
            dh = 0;
            width = dw + winHeight * 4 / 3 - 1;
            height = dh + winHeight - 1;
        }
        mViewWindow = new Rect(dw, dh, width, height);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.w(TAG, "Bound to WebcamManager");
            synchronized(mServiceSyncToken) {
                mWebcamService = ((WebcamService.WebcamBinder)service).getService();
                mServiceSyncToken.notify();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "WebcamManager disconnected unexpectedly");
            synchronized(mServiceSyncToken) {
                mRunning = false;
                mWebcamService = null;
                mServiceSyncToken.notify();
            }
        }
    };
}
