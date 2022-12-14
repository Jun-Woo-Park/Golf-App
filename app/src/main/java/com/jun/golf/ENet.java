package com.jun.golf;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

class ENet {
    static {
        System.loadLibrary("yolov5");
    }

    public static native void init(AssetManager manager, boolean useGPU);
    public static native float[] detect(Bitmap bitmap);
}
