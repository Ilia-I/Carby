package com.grouph.ces.carby.volume_estimation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.volume_estimation.DevMode.RecordFrame;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthewball on 22/03/2018.
 */

public class IntegralApproximation {

    private static final String TAG = "IntegralApproximation";
    private static final double POUND_RADIUS = 1.42;

    private VolEstActivity activity;

    private Frame top = null;
    private Frame side = null;

    private double topWidth;
    private double topHeight;
    private double sideWidth;
    private double sideHeight;


    public IntegralApproximation(Activity activity) {
        this.activity = (VolEstActivity) activity;
    }

    public IntegralApproximation(Activity activity, Frame top, Frame side) {
        this.activity = (VolEstActivity) activity;
        this.top = top;
        this.side = side;
    }

    //returns a rect with bounding box dimensions
    private Rect calculate2dDimensions(Mat input) {
        if(input.channels() != 1)
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

        if(maxContour.empty())
            return null;

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
        Log.e(TAG, "orig top dimensions: " + top.getImage().height() + "x" + top.getImage().width());
        Log.e(TAG, "orig side dimensions: " + side.getImage().height() + "x" + side.getImage().width());
        double scaleWidth = 0;
        double scaleHeight = 0;
        Mat smallerMat;

        if(topWidth == sideWidth) // No resize if same size
            return;

        if(topWidth == 0 || sideWidth == 0) // No contour detected
            return;

        if(topWidth > sideWidth) {
            scaleWidth = topWidth;
            scaleHeight = topWidth / sideWidth * sideHeight;
            smallerMat = side.getImage();

        }
        else {
            scaleWidth = sideWidth;
            scaleHeight = sideWidth / topWidth * topHeight;
            smallerMat = top.getImage();
            top.setReferenceObjectSize(top.getReferenceObjectSize()*sideWidth/topWidth);
        }

        Imgproc.resize(smallerMat, smallerMat, new Size(scaleWidth, scaleHeight));
    }

    public double getApproximation() {
        Rect topDimensions = calculate2dDimensions(top.getImage());
        Rect sideDimensions = calculate2dDimensions(side.getImage());

        if(topDimensions == null || sideDimensions == null)
            return -1.0;

        topWidth = topDimensions.width;
        topHeight = topDimensions.height;
        sideWidth = sideDimensions.width;
        sideHeight = sideDimensions.height;

        cropWithBoundingBoxes(topDimensions, sideDimensions);
        scaleSmallerMat();

        double vol = Math.cbrt(pixToCmVal());
        Log.e(TAG, "top dimensions: " + top.getImage().height() + "x" + top.getImage().width());
        Log.e(TAG, "side dimensions: " + side.getImage().height() + "x" + side.getImage().width());
        Log.e(TAG, "pixels to cm: " + vol);
        Log.e(TAG, "Predicted Volume: " + volume() / pixToCmVal() + " cm3");

        return vol;
    }

    private double pixToCmVal(){
        return Math.pow(top.getReferenceObjectSize() / POUND_RADIUS,3);
    }

    public boolean loadTestMats() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
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

        if(top == null || side == null) {
            Log.e(TAG, "Error loading test Mats");
            return false;
        }
        else {
            Log.d(TAG, "\n Top image loaded: \n" + top.toString());
            Log.d(TAG, "\nSide image loaded: \n" + side.toString());
            return true;
        }
    }

    public void showResults(int foodType) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        if (preferences.getBoolean(activity.getResources().getString(R.string.key_dev_mode), false)) {
            RecordFrame testTop = new RecordFrame(ResultsFragment.IMAGE_SET_STRETCH + 1, top);
            testTop.saveObj(preferences);
            RecordFrame testSide = new RecordFrame(ResultsFragment.IMAGE_SET_STRETCH + 2, side);
            testSide.saveObj(preferences);
            Bundle bundle = new Bundle();
            bundle.putInt("foodType",foodType);
            bundle.putDouble("volume",volume()/pixToCmVal());
            activity.setFragmentResults(bundle);
        } else {
            NutritionInformationCalculator nic = new NutritionInformationCalculator(activity,volume()/pixToCmVal(),foodType);
            nic.show();
        }
    }

}
