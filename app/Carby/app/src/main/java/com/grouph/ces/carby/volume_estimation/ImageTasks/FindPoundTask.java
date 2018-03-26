package com.grouph.ces.carby.volume_estimation.ImageTasks;

import android.os.AsyncTask;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthewball on 23/03/2018.
 */

public class FindPoundTask extends AsyncTask<Mat, Void, Double> {

    private static String TAG = "FindPoundTask";
    private static double poundRadius = 2.323/2;

    @Override
    protected Double doInBackground(Mat... mats) {
        final double scalingFactor = 2.5;

        Mat src = mats[0];
        Mat blurred = new Mat();
        Imgproc.resize(src, blurred, new org.opencv.core.Size(src.width()/scalingFactor,src.height()/scalingFactor));

        Imgproc.medianBlur(blurred, blurred, 5);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U);

        Mat circles=new Mat();
        List<Mat> blurredChannel = new ArrayList<>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<>();
        gray0Channel.add(gray0);

        for (int c = 0; c < 3; c++) {
            int ch[] = { c, 0 };

            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
            Imgproc.HoughCircles(gray0,circles, Imgproc.HOUGH_GRADIENT,2, (int) (500/scalingFactor),200,50, (int) (20/scalingFactor), (int) (100/scalingFactor));
            double x = 0.0;
            double y = 0.0;
            double r = 0.0;

            if (circles.rows()>0)
            {
                double[] data = circles.get(0, 0);
                for(int j = 0 ; j < data.length ; j++){
                    x = data[0]*scalingFactor;
                    y = data[1]*scalingFactor;
                    r = data[2]*scalingFactor;
                }
                Point center = new Point(x,y);
                // circle center
                Imgproc.circle(mats[0], center, 3, new Scalar(0,255,0), -1);
                // circle outline
                Imgproc.circle(mats[0], center, (int) r, new Scalar(255,0,0), 3);
                return r / poundRadius;
            }
        }
        return -1.0;
    }

}