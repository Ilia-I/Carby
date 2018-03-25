package com.grouph.ces.carby.volume_estimation;

/**
 * Created by matthewball on 18/02/2018.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.grouph.ces.carby.camera_calibration.CalibrationResult;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ProcessingAlgorithms {

    private static String TAG = "ProcessingAlgorithms";

    private Mat mCameraMatrix = new Mat();
    private Mat mDistortionCoefficients = new Mat();

    public ProcessingAlgorithms(Context context) {
        if (CalibrationResult.tryLoad(context, mCameraMatrix, mDistortionCoefficients)) {
            Log.e(TAG, "Camera calibrated: " + mCameraMatrix.toString());
        } else
            Log.e(TAG, "Camera not calibrated");
    }


    public Bitmap matToBitmap(Mat input) {
        Imgproc.resize(input, input, new Size(1280, 720));

        Bitmap bitmap = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(input, bitmap);

        return bitmap;
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

}
