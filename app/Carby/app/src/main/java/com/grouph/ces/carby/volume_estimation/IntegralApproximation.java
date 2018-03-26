package com.grouph.ces.carby.volume_estimation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.widget.Toast;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.volume_estimation.DevMode.RecordFrame;
import com.grouph.ces.carby.volume_estimation.DevMode.ShowFramesActivity;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by matthewball on 22/03/2018.
 */

public class IntegralApproximation {

    private static String TAG = "IntegralApproximation";
    private static final int DEV_IMG = 7;

    private int numParts = 10;

    private Mat top = new Mat();
    private Mat side = new Mat();

    private List<RecordFrame> rfs;
    private Context context;

    public IntegralApproximation(Context context) {
        this.context = context;
    }

    public IntegralApproximation(Mat top, Mat side, Context context) {
        this.top = top;
        this.side = side;
        this.context = context;
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
