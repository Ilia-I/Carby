package com.grouph.ces.carby.volume_estimation;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by matthewball on 14/02/2018.
 */

public class PictureCallback implements Camera.PictureCallback {

    private ImageView mImageView;

    public PictureCallback(ImageView iv) {
        this.mImageView = iv;
    }

    private static String TAG = "PictureCallback";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }

        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);

        Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap picture32 = picture.copy(Bitmap.Config.ARGB_8888, true);
        picture32 = Bitmap.createScaledBitmap(picture32, 500, 500, false);

        Mat mat = new Mat(500,500, CvType.CV_8UC3);
        Utils.bitmapToMat(picture32, mat);

        Log.i(TAG, mat.size().toString());
        Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGBA2RGB);

        Mat background = new Mat(mat.size(), CvType.CV_8UC3,
                new Scalar(255, 255, 255));
        Mat firstMask = new Mat();
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat mask;
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
        Mat dst = new Mat();

        Point p1 = new Point(100,100);
        Point p2 = new Point(mat.size().width-100, mat.size().height-100);

        Rect rect = new Rect(p1, p2);

        Imgproc.grabCut(mat, firstMask, rect, bgModel, fgModel,
                10, Imgproc.GC_INIT_WITH_RECT);

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

        Utils.matToBitmap(dst, newBitmap);

        this.mImageView.setImageBitmap(newBitmap);
        this.mImageView.setRotation(90);

//        try {
//            FileOutputStream fos = new FileOutputStream(pictureFile);
//            fos.write(data);
//            fos.close();
//        } catch (FileNotFoundException e) {
//            Log.d(TAG, "File not found: " + e.getMessage());
//        } catch (IOException e) {
//            Log.d(TAG, "Error accessing file: " + e.getMessage());
//        }

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
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
