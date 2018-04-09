package com.grouph.ces.carby.volume_estimation.ImageTasks;

import android.os.AsyncTask;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthewball on 23/03/2018.
 */

public class FindCardTask extends AsyncTask<Mat, Void, FindCardTask.Result> {

    private static String TAG = "FindCardTask";

    public class Result {
        public RotatedRect boundRect;
        public double width;

        public Result(RotatedRect r, double w) {
            this.boundRect = r;
            this.width = w;
        }
    }

    @Override
    protected Result doInBackground(Mat... mats) {
        int scalingFactor = 4;
        Mat src = mats[0];
        Mat blurred = new Mat();
        Imgproc.resize(src,blurred, new org.opencv.core.Size(src.width()/scalingFactor,src.height()/scalingFactor));
        Mat output = blurred.clone();

        Imgproc.medianBlur(blurred, blurred, 7);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<>();
        List<Mat> blurredChannel = new ArrayList<>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;
        double minArea = 16000/(scalingFactor*scalingFactor);
        double maxArea = 80000/(scalingFactor*scalingFactor);

        //find contours for all 3 channels
        for (int c = 0; c < 3; c++) {
            int ch[] = { c, 0 };

            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
            Imgproc.Canny(gray0, gray, 15, 30, 3, true);
            Imgproc.dilate(gray,gray,new Mat());
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
                    RotatedRect newRect = new RotatedRect(rect.center,rect.size,0);
                    Point points[] = new Point[4];
                    newRect.points(points);
                    if (checkRatio(points)){
                        Log.e(TAG, "doInBackground: "+area);
                        rect.size.height*=scalingFactor;
                        rect.size.width*=scalingFactor;
                        double w = newRect.size.width*scalingFactor;
                        return new Result(rect, w);
                    }
                }
            }
        }

        return new Result(new RotatedRect(), -1);
    }

    //checks if the ratio of the detected card is within the actual dimension limit
    private boolean checkRatio(Point[] points) {
        double width = points[2].x - points[1].x;
        double height = points[0].y - points[1].y;
        double ratio = width / height;
        if ((1.55 < ratio && ratio < 1.65) || (0.58 < ratio && ratio < 0.68)) {
            return true;
        }
        return false;
    }

}