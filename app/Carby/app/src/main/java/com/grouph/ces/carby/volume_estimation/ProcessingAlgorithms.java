package com.grouph.ces.carby.volume_estimation;

/**
 * Created by matthewball on 18/02/2018.
 */

import android.content.Context;
import android.graphics.Bitmap;

import com.grouph.ces.carby.camera_calibration.CalibrationResult;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ProcessingAlgorithms {

    private static final int CAMERA_MATRIX_ROWS = 3;
    private static final int CAMERA_MATRIX_COLS = 3;
    private static final int DISTORTION_COEFFICIENTS_SIZE = 5;

    private Mat mCameraMatrix = Mat.eye(3, 3, CvType.CV_64F);
    private Mat mDistortionCoefficients = Mat.zeros(8, 1, CvType.CV_64F);

    private boolean isCalibrated = false;

    public ProcessingAlgorithms(Context context) {
        this.isCalibrated = CalibrationResult.tryLoad(context, mCameraMatrix, mDistortionCoefficients);
    }

    private static String TAG = "ProcessingAlgorithms";

    public static Bitmap matToBitmap(Mat input, int width, int height) {
        Imgproc.resize(input, input, new Size(width, height));

        Bitmap bitmap = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(input, bitmap);

        return bitmap;
    }

    public void undistort(Mat input) {
        Mat undistorted = new Mat(input.rows(), input.cols(), CvType.CV_8U);

        if(isCalibrated) {
            Imgproc.undistort(input, undistorted, mCameraMatrix, mDistortionCoefficients);
            undistorted.copyTo(input);
        }
        
        undistorted.release();
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
