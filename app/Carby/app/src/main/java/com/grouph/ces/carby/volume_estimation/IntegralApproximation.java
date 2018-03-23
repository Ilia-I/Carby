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
    private Mat topRef = new Mat();
    private Mat sideRef = new Mat();

    public IntegralApproximation() {}

    public IntegralApproximation(Mat top, Mat side, Mat topRef, Mat sideRef) {
        this.top = top;
        this.side = side;
        this.topRef = topRef;
        this.sideRef = sideRef;
    }

    public void loadTestMats() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/Carby/test/");
        File topFile = new File(dir, "top.png");
        File sideFile = new File(dir, "side.png");
        File topReferenceFile = new File(dir, "topRef.png");
        File sideReferenceFile =  new File(dir, "sideRef.png");


        Utils.bitmapToMat(BitmapFactory.decodeFile(topFile.getAbsolutePath()), top);
        Utils.bitmapToMat(BitmapFactory.decodeFile(sideFile.getAbsolutePath()), side);
        Utils.bitmapToMat(BitmapFactory.decodeFile(topReferenceFile.getAbsolutePath()), topRef);
        Utils.bitmapToMat(BitmapFactory.decodeFile(sideReferenceFile.getAbsolutePath()), sideRef);

        Log.e(TAG, "top: " + top.size().toString());
        Log.e(TAG, "side: " + side.size().toString());
        Log.e(TAG, "topRef: " + topRef.size().toString());
        Log.e(TAG, "sideRef: " + sideRef.size().toString());
    }

}
