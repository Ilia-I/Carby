package com.grouph.ces.carby.volume_estimation;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.Image;
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
import org.opencv.core.Algorithm;
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
    private ImageProcessor processor;

    public PictureCallback(ImageView iv, ImageProcessor processor) {
        this.mImageView = iv;
        this.processor = processor;
    }

    private static String TAG = "PictureCallback";

    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap pictureTaken = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap grabCutPicture = processor.performGrabCut(pictureTaken);

        this.mImageView.setImageBitmap(grabCutPicture);
        this.mImageView.setRotation(90);
    }
}
