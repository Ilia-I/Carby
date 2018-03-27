package com.grouph.ces.carby.volume_estimation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.grouph.ces.carby.volume_estimation.DevMode.RecordFrame;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by matthewball on 22/03/2018.
 */

public class IntegralApproximation {

    private static final String TAG = "IntegralApproximation";
    private static final int NUM_PARTS = 10;

    private Context context;

    private Frame top = null;
    private Frame side = null;

    private double topWidth;
    private double topHeight;
    private double sideWidth;
    private double sideHeight;


    public IntegralApproximation(Context c) {
        this.context = c;
        loadTestMats();
    }

    private Rect calculate2dDimensions(Mat input) {
        Imgproc.cvtColor(input, input, Imgproc.COLOR_BGR2GRAY);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(input, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.RETR_TREE);

        double maxArea = 0;
        MatOfPoint maxContour = new MatOfPoint();

        for(MatOfPoint c : contours) {
            double contourArea = Imgproc.contourArea(c);
            if(contourArea > maxArea) {
                maxArea = contourArea;
                maxContour = c;
            }
        }

        Rect rect = Imgproc.boundingRect(maxContour);
//        Imgproc.rectangle(input, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 255), 3);

        return rect;
    }

    private void cropWithBoundingBoxes(Rect topRect, Rect sideRect) {
        Mat topMat = top.getImage();
        Mat sideMat = side.getImage();

        new Mat(topMat, topRect).copyTo(topMat);
        new Mat(sideMat, sideRect).copyTo(sideMat);
    }

    private int volume() {
        Mat topMat = top.getImage();

        int vol = 0;
        for(int i = 0; i < topMat.width(); i++)
            vol += volOfColumn(i);

        return vol;
    }

    private int volOfColumn(int col) {
        Mat topMat = top.getImage();
        int vol = 0;

        Mat topColumn = topMat.col(col);
        Mat sideColumn = side.getImage().col(col);
        int length = 0;
        int depth = 0;
        for(int i = 0; i < topColumn.height(); i++)
            if(topColumn.get(i,0)[0] == 255.0)
                length++;

        for(int i = 0; i < sideColumn.height(); i++)
            if(sideColumn.get(i,0)[0] == 255.0)
                depth++;

        vol += length * depth;

        return vol;
    }



    private void scaleSmallerMat() {
        double scaleWidth = 0;
        double scaleHeight = 0;
        Mat smallerMat;

        if(topWidth > sideWidth) {
            scaleWidth = topWidth;
            scaleHeight = topWidth / sideWidth * sideHeight;
            smallerMat = side.getImage();
        }
        else {
            scaleWidth = sideWidth;
            scaleHeight = sideWidth / topWidth * topHeight;
            smallerMat = top.getImage();
        }

        Imgproc.resize(smallerMat, smallerMat, new Size(scaleWidth, scaleHeight));
    }

    public void performApproximation() {
        Rect topDimensions = calculate2dDimensions(top.getImage());
        Rect sideDimensions = calculate2dDimensions(side.getImage());

        topWidth = topDimensions.width;
        topHeight = topDimensions.height;
        sideWidth = sideDimensions.width;
        sideHeight = sideDimensions.height;

        cropWithBoundingBoxes(topDimensions, sideDimensions);
        scaleSmallerMat();

        Log.e(TAG, "performApproximation: " + side.getPixelsPerCm());
        Log.e(TAG, "vol of pixels: " + volume() / Math.pow(side.getPixelsPerCm(), 3) + " cm3");
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

            Bitmap topDownOut = ProcessingAlgorithms.matToBitmap(top.getImage(), top.getImage().width(), top.getImage().height());
            if(topDownOut != null) {
                fOut = new FileOutputStream(out1);
                topDownOut.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
            }

            Bitmap sideOut = ProcessingAlgorithms.matToBitmap(side.getImage(), side.getImage().width(), side.getImage().height());
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
