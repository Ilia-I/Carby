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
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
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

    private Mat mCameraMatrix = new Mat();
    private Mat mDistortionCoefficients = new Mat();
    private final int scalingFactor = 2;

    public ProcessingAlgorithms(Context context) {
        if (CalibrationResult.tryLoad(context, mCameraMatrix, mDistortionCoefficients)) {
            Log.e(TAG, "Camera calibrated: " + mCameraMatrix.toString());
        } else
            Log.e(TAG, "Camera not calibrated");
    }

    public Mat performGrabCut(Mat input, Rect boundingBox) {
        Mat origImage = performScaling(input);

        // Initialise models, masks, foreground and background
        Mat background = new Mat(origImage.size(), CvType.CV_8UC3, white);
        Mat firstMask = new Mat();
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat mask;
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
        Mat dst = new Mat();

        // Create bounding box
        Point p1 = new Point(boundingBox.x, boundingBox.y);
        Point p2 = new Point(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height);

        p1.x = p1.x / scalingFactor;
        p1.y = p1.y / scalingFactor;
        p2.x = p2.x / scalingFactor;
        p2.y = p2.y / scalingFactor;
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
//        dst=featureDetect(dst);

        return dst;
    }

    private Mat performScaling(Mat input){
        Mat origImage = new Mat(input.width() / scalingFactor, input.height() / scalingFactor, CvType.CV_8UC3);

        Imgproc.resize(input, origImage, new Size(input.width() / scalingFactor, input.height() / scalingFactor));

        // Remove alpha channels from bitmap
        Imgproc.cvtColor(origImage, origImage, Imgproc.COLOR_RGBA2RGB);

        return origImage;
    }

    public Bitmap matToBitmap(Mat src) {
        Mat input = src.clone();
//        Imgproc.resize(src, input, new Size(1280, 720));
        Bitmap bitmap = Bitmap.createBitmap(input.width(), input.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(input, bitmap);

        return bitmap;
    }

    private Mat grayImg(Mat img){
        Mat gray = new Mat(img.width(), img.height(), CvType.CV_8U, new Scalar(4));
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        return gray;
    }

//    private Mat featureDetect(Mat img){
//        //convert to gray
//        Mat gray = grayImg(img);
//
//        FeatureDetector fd = FeatureDetector.create(FeatureDetector.FAST);
//        MatOfKeyPoint regions = new MatOfKeyPoint();
//        fd.detect(gray, regions);
//
//        Features2d.drawKeypoints(img, regions,img );
//
//        return img;
//    }

    public Mat findRefObject(Mat src) {
        Mat blurred = new Mat();
        Imgproc.resize(src, blurred, new org.opencv.core.Size(src.width(),src.height()));
        Imgproc.medianBlur(blurred, blurred, 7);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<>();
        List<Mat> blurredChannel = new ArrayList<>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;
        double minArea = 16000;
        double maxArea = 80000;
        int maxId = -1;

        //find contours for all 3 channels
        for (int c = 0; c < 3; c++) {
            int ch[] = { c, 0 };

            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
            Imgproc.Canny(gray0, gray, 15, 30, 3, true);
            Imgproc.dilate(gray, gray, new Mat());
            Imgproc.findContours(gray, contours, new Mat(),
                    Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

            for (MatOfPoint contour : contours) {
                MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());
                Imgproc.boundingRect(contour);
                double area = Imgproc.contourArea(contour);
                approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(temp, approxCurve,
                        Imgproc.arcLength(temp, true) * 0.07, true);

                if (approxCurve.total() == 4 && area >= minArea && area <= maxArea) {
                    RotatedRect rect = Imgproc.minAreaRect(temp);
                    Point points[] = new Point[4];
                    rect.points(points);
                    if (checkRatio(points)){
                        minArea = area;
                        maxId = contours.indexOf(contour);
                    }
                }
            }
        }

        Mat output = new Mat(src.rows(), src.cols(), CvType.CV_8UC3, new Scalar(255,255,255));
        if(maxId >= 0) {
            Log.e(TAG, "DETECTED REF OBJECT: ");
            Imgproc.drawContours(output, contours, maxId, new Scalar(255, 0,0), 1);
            return output;
        }

        return output;
    }

    private boolean checkRatio(Point[] points) {
        double width = points[2].x - points[1].x;
        double height = points[0].y - points[1].y;
        double ratio = width / height;
        if ((1.5 < ratio && ratio < 1.7) || (0.53 < ratio && ratio < 0.73)) {
            return true;
        }
        return false;

    }

}
