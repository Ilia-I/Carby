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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by matthewball on 22/03/2018.
 */

public class IntegralApproximation {

    private static final String TAG = "IntegralApproximation";
    private static final int NUM_PARTS = 10;

    private Context context;

    private Frame top = null;
    private Frame side = null;

    public IntegralApproximation(Context c) {
        this.context = c;
    }

    public IntegralApproximation(Frame top, Frame side) {
        this.top = top;
        this.side = side;
    }

    public void performApproximation() {
        //
    }

    public void loadTestMats() {
        SharedPreferences prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        for(String name : RecordFrame.recordedFrameNames(prefs)) {
            Log.d(TAG, "name: " + name);
            if(name.equalsIgnoreCase("takePicture_testTop")) {
                RecordFrame topRf = new RecordFrame(prefs, name);
                Mat image = new Mat();
                Utils.bitmapToMat(topRf.getImage(), image);
                this.top = new Frame(image, topRf.getPixelsPerCm(), topRf.getBoundingBox());
            }
            if(name.equalsIgnoreCase("takePicture_testSide")) {
                RecordFrame sideRf = new RecordFrame(prefs, name);
                Mat image = new Mat();
                Utils.bitmapToMat(sideRf.getImage(), image);
                this.side = new Frame(image, sideRf.getPixelsPerCm(), sideRf.getBoundingBox());
            }
        }

        if(top == null || side == null)
            Log.e(TAG, "Error loading test Mats");
        else {
            Log.d(TAG, "\n Top image loaded: \n" + top.toString());
            Log.d(TAG, "\nSide image loaded: \n" + side.toString());
        }
    }

    public void showResults() {
        Intent results = new Intent(context, ResultsActivity.class);

        File out1 = new File(context.getCacheDir(), "1.png");
        File out2 = new File(context.getCacheDir(), "2.png");

        try {
            FileOutputStream fOut;

            Bitmap topDownOut = ProcessingAlgorithms.matToBitmap(top.getImage());
            if(topDownOut != null) {
                fOut = new FileOutputStream(out1);
                topDownOut.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
            }

            Bitmap sideOut = ProcessingAlgorithms.matToBitmap(side.getImage());
            if(sideOut != null) {
                fOut = new FileOutputStream(out2);
                sideOut.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        results.putExtra("image1", out1.getAbsolutePath());
        results.putExtra("image2", out2.getAbsolutePath());

        context.startActivity(results);
    }

}
