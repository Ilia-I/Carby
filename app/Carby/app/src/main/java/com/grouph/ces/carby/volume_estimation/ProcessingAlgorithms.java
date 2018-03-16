package com.grouph.ces.carby.volume_estimation;

/**
 * Created by matthewball on 18/02/2018.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.grouph.ces.carby.camera_calibration.CalibrationResult;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class ProcessingAlgorithms {

    private static String TAG = "ProcessingAlgorithms";
    private Scalar white = new Scalar(255,255,255);
    private ArrayList<Bitmap> grabCutPictures = new ArrayList<>();

    private Mat mCameraMatrix = new Mat();
    private Mat mDistortionCoefficients = new Mat();

    public ProcessingAlgorithms(Context context) {
        if (CalibrationResult.tryLoad(context, mCameraMatrix, mDistortionCoefficients)) {
            Log.e(TAG, "Camera calibrated: " + mCameraMatrix.toString());
        } else
            Log.e(TAG, "Camera not calibrated");
    }

    public Bitmap performGrabCut(Bitmap input) {
        // Convert to correct image format
        Bitmap picture32 = input.copy(Bitmap.Config.ARGB_8888, true);

        final int scalingFactor = 2;
        int scaledWidth = input.getWidth() / scalingFactor;
        int scaledHeight = input.getHeight() / scalingFactor;

        Bitmap scaledPicture = Bitmap.createScaledBitmap(picture32, scaledWidth, scaledHeight, false);

        // Convert to OpenCV origImage
        Mat origImage = new Mat(scaledWidth, scaledHeight, CvType.CV_8UC3);

        // Place bitmap in origImage
        Utils.bitmapToMat(scaledPicture, origImage);

        // Remove alpha channels from bitmap
        Imgproc.cvtColor(origImage,origImage,Imgproc.COLOR_RGBA2RGB);

        // Initialise models, masks, foreground and background
        Mat background = new Mat(origImage.size(), CvType.CV_8UC3, white);
        Mat firstMask = new Mat();
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat mask;
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
        Mat dst = new Mat();

        // Create bounding box
        int boxSize = 300/scalingFactor;
        Point p1 = new Point((origImage.size().width-boxSize)/2,(origImage.size().height-boxSize)/2);
        Point p2 = new Point((origImage.size().width+boxSize)/2, (origImage.size().height+boxSize)/2);
        Rect rect = new Rect(p1, p2);

        Imgproc.grabCut(origImage, firstMask, rect, bgModel, fgModel,
                10, Imgproc.GC_INIT_WITH_RECT);

        Core.compare(firstMask, source, firstMask, Core.CMP_EQ);
        Mat foreground = new Mat(origImage.size(), CvType.CV_8UC3, white);
        origImage.copyTo(foreground, firstMask);

        Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
        Mat tmp = new Mat();

        Imgproc.resize(background, tmp, origImage.size());
        background = tmp;

        mask = grayImg(foreground);
        Imgproc.threshold(mask, mask, 254, 255, Imgproc.THRESH_BINARY_INV);

        background.copyTo(dst);
        background.setTo(vals, mask);
        Core.add(background, foreground, dst, mask);

        firstMask.release();
        source.release();
        bgModel.release();
        fgModel.release();
        vals.release();

        Mat refObjMat =findRectangle(origImage);
        origImage.copyTo(dst,refObjMat);
        dst=featureDetect(dst);

        //uncomment to draw the bounding box
//        Scalar color = new Scalar(255, 0, 0, 255);
//        Imgproc.rectangle(dst, p1, p2, color);

        //convert back to bitmap
        Utils.matToBitmap(dst, scaledPicture);
        return scaledPicture;
    }

    private Mat grayImg(Mat img){
        Mat gray = new Mat(img.width(), img.height(), CvType.CV_8U, new Scalar(4));
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        return gray;
    }

    private Mat featureDetect(Mat img){
        //convert to gray
        Mat gray = grayImg(img);

        FeatureDetector fd = FeatureDetector.create(FeatureDetector.FAST);
        MatOfKeyPoint regions = new MatOfKeyPoint();
        fd.detect(gray, regions);

        Features2d.drawKeypoints(img, regions,img );

        return img;
    }

    private Mat findRectangle(Mat src) {
        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred, 9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<>();
        List<Mat> blurredChannel = new ArrayList<>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;
        double maxArea = 2500;
        int maxId = -1;

        for (int c = 0; c < 3; c++) {
            int ch[] = { c, 0 };

            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
            Imgproc.Canny(gray0, gray, 10, 30, 3, true);

            Imgproc.findContours(gray, contours, new Mat(),
                    Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

            for (MatOfPoint contour : contours) {
                MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());

                double area = Imgproc.contourArea(contour);
                approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(temp, approxCurve,
                        Imgproc.arcLength(temp, true) * 0.02, true);
                 if (approxCurve.total() == 4 && area >= maxArea) {
                     maxArea = area;
                     maxId = contours.indexOf(contour);
                 }
            }
        }

        Mat mask = new Mat(src.size(), CvType.CV_8UC3,
                new Scalar(0, 0, 0));
        if (maxId >= 0) {
            Imgproc.drawContours(mask, contours, maxId, white, -1);
            //Imgproc.drawContours(mask, contours, -1, white, 1);
        }
        return mask;
    }


    private double angle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }

}
