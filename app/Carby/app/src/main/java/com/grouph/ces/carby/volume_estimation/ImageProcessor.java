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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGBA);

        // unsure of syntax for your platform here... but something like ...
        Bitmap newBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(),
                Bitmap.Config.ARGB_8888);

        Imgproc.rectangle(dst, p1, p2, color);
        Utils.matToBitmap(dst, newBitmap);

//        try {
//            FileOutputStream fos = new FileOutputStream(pictureFile);
//            fos.write(data);
//            fos.close();
//        } catch (FileNotFoundException e) {
//            Log.d(TAG, "File not found: " + e.getMessage());
//        } catch (IOException e) {
//            Log.d(TAG, "Error accessing file: " + e.getMessage());
//        }

        return newBitmap;
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
