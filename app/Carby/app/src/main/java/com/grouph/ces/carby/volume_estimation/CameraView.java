package com.grouph.ces.carby.volume_estimation;

import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.FileOutputStream;
import java.util.List;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

public class CameraView extends JavaCameraView {

    private static final String TAG = "myCameraView";
    private String mPictureFileName;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setResolution(int width, int height) {
        mCamera.setPreviewCallback(null);

        disconnectCamera();
        mMaxHeight = height;
        mMaxWidth = width;

        connectCamera(getWidth(), getHeight());

        Camera.Parameters params = mCamera.getParameters();
        Size pictureSize = params.getSupportedPictureSizes().get(0);
        for(Size s : params.getSupportedPictureSizes())
            if(s.width == width && s.height == height) {
                pictureSize = s;
                break;
            }
        params.setPictureSize(pictureSize.width, pictureSize.height);

        mCamera.setParameters(params);
        mCamera.setPreviewCallback(this);
    }

    public Size getPictureSize() {
        Log.e(TAG, "getResolution: PICTURE SIZE" + mCamera.getParameters().getPictureSize().width + " x " + mCamera.getParameters().getPictureSize().height);

        return mCamera.getParameters().getPictureSize();
    }

    public Size getPreviewSize() {
        Log.e(TAG, "getResolution: PREVIEW SIZE" + mCamera.getParameters().getPreviewSize().width +
                " x " + mCamera.getParameters().getPreviewSize().height);

        return mCamera.getParameters().getPreviewSize();
    }

    public void takePicture(String fileName, PictureCallback callback) {
        Log.e(TAG, "Taking picture");
        this.mPictureFileName = fileName;
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
//        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null, null, callback);
//        mCamera.setPreviewCallback(this);
//        mCamera.startPreview();
    }

}