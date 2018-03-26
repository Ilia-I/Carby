package com.grouph.ces.carby.volume_estimation.ImageTasks;

import android.os.AsyncTask;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
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
        final int scalingFactor = 2;

        Mat src = mats[0];
        Mat blurred = new Mat();
        Imgproc.resize(src, blurred, new org.opencv.core.Size(src.width()/scalingFactor,src.height()/scalingFactor));
        Imgproc.cvtColor(blurred,blurred, Imgproc.COLOR_RGB2HSV);


        Imgproc.medianBlur(blurred, blurred, 5);
        //Mat colourMask= new Mat();
        //Core.inRange(blurred,new Scalar(10,50,50),new Scalar(45,255,255),colourMask);

        Mat gray = new Mat();
        Imgproc.cvtColor(blurred, gray, Imgproc.COLOR_HSV2BGR);

        Mat circles=new Mat();
        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U);
        List<Mat> blurredChannel = new ArrayList<>();
        blurredChannel.add(gray);
        List<Mat> gray0Channel = new ArrayList<>();
        gray0Channel.add(gray0);

        for (int c = 0; c < 3; c++) {
            int ch[] = { c, 0 };

            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
            Imgproc.HoughCircles(gray0,circles, Imgproc.HOUGH_GRADIENT,2, (100/scalingFactor),200,100, (20/scalingFactor), (100/scalingFactor));
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
                Imgproc.circle(src, center, 3, new Scalar(0,255,0), -1);
                // circle outline
                Imgproc.circle(src, center, (int) r, new Scalar(255,0,0), 3);
                return r / poundRadius;
            }
        }


        return -1.0;
    }

}