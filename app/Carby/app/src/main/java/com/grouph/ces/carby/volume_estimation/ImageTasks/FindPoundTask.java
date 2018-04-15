package com.grouph.ces.carby.volume_estimation.ImageTasks;

import android.os.AsyncTask;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthewball on 23/03/2018.
 */

public class FindPoundTask extends AsyncTask<Mat, Void, FindPoundTask.Result> {

    private static final String TAG = "FindPoundTask";

    public class Result {
        public Point center;
        public double radius;

        public Result(Point p, double r) {
            this.center = p;
            this.radius = r;
        }
    }

    @Override
    protected Result doInBackground(Mat... mats) {
        final int scalingFactor = 2;

        Mat src = mats[0];
        Mat blurred = new Mat();
        Imgproc.resize(src, blurred, new org.opencv.core.Size(src.width()/scalingFactor,src.height()/scalingFactor));
        Imgproc.cvtColor(blurred,blurred, Imgproc.COLOR_RGB2HSV);

        Imgproc.medianBlur(blurred, blurred, 5);
        //Mat colourMask= new Mat();
        //Core.inRange(blurred,new Scalar(10,50,50),new Scalar(45,255,255),colourMask);

        Imgproc.cvtColor(blurred, blurred, Imgproc.COLOR_HSV2BGR);

        Mat circles=new Mat();
        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U);
        List<Mat> blurredChannel = new ArrayList<>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<>();
        gray0Channel.add(gray0);

        for (int c = 0; c < 1; c++) {
            int ch[] = { c, 0 };

            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
            Imgproc.HoughCircles(gray0,circles, Imgproc.HOUGH_GRADIENT,2, (100/scalingFactor),200,90, (20/scalingFactor), (100/scalingFactor));
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

                for(Mat m : blurredChannel)
                    m.release();
                for(Mat m : gray0Channel)
                    m.release();

                circles.release();
                gray0.release();
                blurred.release();
                return new Result(center, r);
            }
        }
        for(Mat m : blurredChannel)
            m.release();
        for(Mat m : gray0Channel)
            m.release();

        circles.release();
        gray0.release();
        blurred.release();
        return new Result(new Point(), -1.0);
    }

}