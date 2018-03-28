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
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthewball on 23/03/2018.
 */

public class GrabCutTask extends AsyncTask<Object, Void, Mat> {

    private static String TAG = "GrabCutTask";
    private final int scalingFactor = 2;
    private Scalar white = new Scalar(255,255,255);

    @Override
    protected Mat doInBackground(Object... params) {
        Mat inputMat = (Mat) params[0];
        Rect boundingBox = (Rect) params[1];

        Mat origImage = performScaling(inputMat);

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

        Imgproc.resize(firstMask, firstMask, origImage.size());

        //firstMask.release();
        source.release();
        bgModel.release();
        fgModel.release();
        vals.release();

        return firstMask;
    }

    private Mat performScaling(Mat input){
        Mat origImage = new Mat(input.width() / scalingFactor, input.height() / scalingFactor, CvType.CV_8UC3);

        Imgproc.resize(input, origImage, new Size(input.width() / scalingFactor, input.height() / scalingFactor));

        // Remove alpha channels from bitmap
        Imgproc.cvtColor(origImage, origImage, Imgproc.COLOR_RGBA2RGB);

        return origImage;
    }

    private Mat grayImg(Mat img){
        Mat gray = new Mat(img.width(), img.height(), CvType.CV_8U, new Scalar(4));
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        return gray;
    }
}