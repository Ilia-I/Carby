package com.grouph.ces.carby.volume_estimation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;

/**
 * Created by matthewball on 22/03/2018.
 */

public class IntegralApproximation {

    private static String TAG = "IntegralApproximation";

    private int numParts = 10;

    private Mat top = new Mat();
    private Mat side = new Mat();

    public IntegralApproximation() {}

    public IntegralApproximation(Mat top, Mat side) {
        this.top = top;
        this.side = side;
    }

    public void loadTestMats() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/Carby/test/");
        File topFile = new File(dir, "top.png");
        File sideFile = new File(dir, "side.png");

        Utils.bitmapToMat(BitmapFactory.decodeFile(topFile.getAbsolutePath()), top);
        Utils.bitmapToMat(BitmapFactory.decodeFile(sideFile.getAbsolutePath()), side);

        Log.e(TAG, "top: " + top.size().toString());
        Log.e(TAG, "side: " + side.size().toString());
    }

}
