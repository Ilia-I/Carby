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
