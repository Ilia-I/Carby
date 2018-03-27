package com.grouph.ces.carby.volume_estimation;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.grouph.ces.carby.volume_estimation.DevMode.RecordFrame;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Created by matthewball on 22/03/2018.
 */

public class IntegralApproximation {

    private static final String TAG = "IntegralApproximation";
    private static final int NUM_PARTS = 10;

    private Context context;

    private Frame top;
    private Frame side;

    public IntegralApproximation(Context c) {
        this.context = c;
    }

    public IntegralApproximation(Frame top, Frame side) {
        this.top = top;
        this.side = side;
    }

    public void loadTestMats() {
        SharedPreferences prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        for(String name : RecordFrame.recordedFrameNames(prefs)) {
            if(name.equalsIgnoreCase("testTop")) {
                RecordFrame topRf = new RecordFrame(prefs, name);
                Mat image = new Mat();
                Utils.bitmapToMat(topRf.getImage(), image);
                this.top = new Frame(image, topRf.getPixelsPerCm(), topRf.getBoundingBox());
                Log.d(TAG, "Top image loaded: \n" + top.toString());
                break;
            }
            if(name.equalsIgnoreCase("testSide")) {
                RecordFrame sideRf = new RecordFrame(prefs, name);
                Mat image = new Mat();
                Utils.bitmapToMat(sideRf.getImage(), image);
                this.top = new Frame(image, sideRf.getPixelsPerCm(), sideRf.getBoundingBox());
                Log.d(TAG, "Side image loaded: \n" + side.toString());
                break;
            }
        }
    }

}
