package com.grouph.ces.carby.volume_estimation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.widget.ImageView;

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
