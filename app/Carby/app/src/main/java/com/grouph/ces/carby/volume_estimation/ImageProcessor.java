package com.grouph.ces.carby.volume_estimation;

/**
 * Created by matthewball on 18/02/2018.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Algorithm;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class ImageProcessor {

    private static String TAG = "ImageProcessor";

    public Bitmap performGrabCut(Bitmap input) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return null;
        }

        // Convert to correct image format
        Bitmap picture32 = input.copy(Bitmap.Config.ARGB_8888, true);

        // Scale to 640x480
        Bitmap scaledPicture = Bitmap.createScaledBitmap(picture32, 640, 480, false);

        // Convert to OpenCV mat
        Mat mat = new Mat(640,480, CvType.CV_8UC3);

        // Place bitmap in mat
        Utils.bitmapToMat(scaledPicture, mat);

        // Remove alpha channels from bitmap
        Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGBA2RGB);

        // Initialise models, masks, foreground and background
        Mat background = new Mat(mat.size(), CvType.CV_8UC3,
                new Scalar(255, 255, 255));
        Mat firstMask = new Mat();
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat mask;
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
        Mat dst = new Mat();


        // Create bounding box
        Point p1 = new Point((mat.size().width-250)/2,(mat.size().height-250)/2);
        Point p2 = new Point((mat.size().width+250)/2, (mat.size().height+250)/2);
        Rect rect = new Rect(p1, p2);

        Imgproc.grabCut(mat, firstMask, rect, bgModel, fgModel,
                5, Imgproc.GC_INIT_WITH_RECT);

        Core.compare(firstMask, source, firstMask, Core.CMP_EQ);

        Mat foreground = new Mat(mat.size(), CvType.CV_8UC3,
                new Scalar(255, 255, 255));
        mat.copyTo(foreground, firstMask);

        Scalar color = new Scalar(255, 0, 0, 255);
        Imgproc.rectangle(mat, p1, p2, color);

        Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
        Mat tmp = new Mat();

        Imgproc.resize(background, tmp, mat.size());
        background = tmp;
        mask = new Mat(foreground.size(), CvType.CV_8UC1,
                new Scalar(255, 255, 255));

        Imgproc.cvtColor(foreground, mask, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(mask, mask, 254, 255, Imgproc.THRESH_BINARY_INV);

        background.copyTo(dst);
        background.setTo(vals, mask);
        Core.add(background, foreground, dst, mask);

        firstMask.release();
        source.release();
        bgModel.release();
        fgModel.release();
        vals.release();

        dst=featureDetect(dst);

        Mat gray = new Mat();
        Mat cannyEdges = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(gray,cannyEdges,75,100);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(cannyEdges,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourImg = new Mat();
        contourImg.create(cannyEdges.rows(), cannyEdges.cols(), CvType.CV_8UC3);
        contourImg.setTo(new Scalar(0,0,0));

        //for (int i = 0; i < contours.size(); i++) {
        Imgproc.drawContours(contourImg, contours, -1, new Scalar(233,29,29), -1);


        //convert back to bitmap
        Utils.matToBitmap(contourImg, scaledPicture);
        return scaledPicture;
    }

    private Mat featureDetect(Mat img){
        Mat blurredImage = new Mat();
        Imgproc.blur(img, blurredImage, new Size(7, 7));

        //convert to gray
        Mat gray = new Mat(img.width(), img.height(), CvType.CV_8U, new Scalar(4));
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        FeatureDetector fd = FeatureDetector.create(FeatureDetector.FAST);
        MatOfKeyPoint regions = new MatOfKeyPoint();
        fd.detect(gray, regions);

        Mat output=new Mat();
        //int r=regions.rows();
        //System.out.println("REGIONS ARE: " + regions);
        Features2d.drawKeypoints(img, regions,output );

        return output;
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Carby Images");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Carby Images", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }

}
