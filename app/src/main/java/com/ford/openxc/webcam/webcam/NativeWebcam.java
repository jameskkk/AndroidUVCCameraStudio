package com.ford.openxc.webcam.webcam;

import android.graphics.Bitmap;
import android.util.Log;

//import com.socks.library.KLog;

import java.io.File;

public class NativeWebcam implements IWebcam {

    private static String TAG = "NativeWebcam";
    private static final int DEFAULT_IMAGE_WIDTH = 640;
    private static final int DEFAULT_IMAGE_HEIGHT = 480;

    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;


    private native int startCamera(String deviceName, int width, int height);
    private native void processCamera();
    private native boolean cameraAttached();
    private native void stopCamera();
    private native void loadNextFrame(Bitmap bitmap);

    static {
        System.loadLibrary("webcam");
    }

    public NativeWebcam(String deviceName, int width, int height) {
        mWidth = width;
        mHeight = height;
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        connect(deviceName, mWidth, mHeight);
    }

    public NativeWebcam(String deviceName) {
        this(deviceName, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }

    private void connect(String deviceName, int width, int height) {
        boolean deviceReady = true;

        File deviceFile = new File(deviceName);
        if(deviceFile.exists()) {
            if(!deviceFile.canRead()) {
                Log.w(TAG, "Insufficient permissions on " + deviceName +
                        " -- does the app have the CAMERA permission?");
                //需要有讀許可權。或通過命令列執行chmod命令。否則會出現預覽黑屏
                try {
                    Process su = Runtime.getRuntime().exec("/system/xbin/su");
                    String cmd = "";
                    for (int i = 0; i < 2; ++i)
                        cmd += String.format("chmod 777 dev/video%d\n", i);
                    cmd += "exit\n";
                    su.getOutputStream().write(cmd.getBytes(), 0, cmd.getBytes().length);
                    su.getOutputStream().flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(!deviceFile.canRead()) {
                Log.w(TAG, "Insufficient permissions on " + deviceName +
                        " -- does the app have the CAMERA permission?");
                deviceReady = false;
            }else{
                deviceReady = true;
            }
        } else {
            Log.w(TAG, deviceName + " does not exist");
            deviceReady = false;
        }

        if(deviceReady) {
            Log.w(TAG, "Preparing camera with device name " + deviceName);
            startCamera(deviceName, width, height);
        }
    }

    public Bitmap getFrame() {
        loadNextFrame(mBitmap);
        return mBitmap;
    }

    public void stop() {
        stopCamera();
    }

    public boolean isAttached() {
        return cameraAttached();
    }
}
