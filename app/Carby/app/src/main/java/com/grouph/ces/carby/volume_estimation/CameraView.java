package com.grouph.ces.carby.volume_estimation;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CameraView extends JavaCameraView implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private static final String TAG = "myCameraView";

    private int boxSize = 300;
    private Point p1;
    private Point p2;

    private Mat mRgba;
    private Mat mRgbaF;
    private Mat mRgbaT;

    private enum Corner { TP_LEFT, TP_RIGHT, BTM_LEFT, BTM_RIGHT }

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

        Log.e(TAG, "getResolution: PICTURE SIZE" + mCamera.getParameters().getPictureSize().width + " x " + mCamera.getParameters().getPictureSize().height);
        Log.e(TAG, "getResolution: PREVIEW SIZE" + mCamera.getParameters().getPreviewSize().width +
                " x " + mCamera.getParameters().getPreviewSize().height);

        mCamera.setPreviewCallback(this);
    }


    public void takePicture(PictureCallback callback) {
        Log.e(TAG, "Taking picture");
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null, null, callback);
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();
    }

    private void initBoundingBox() {
        p1 = new Point((mRgba.size().width-boxSize)/2,(mRgba.size().height-boxSize)/2);
        p2 = new Point((mRgba.size().width+boxSize)/2, (mRgba.size().height+boxSize)/2);
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Imgproc.rectangle(mRgba, p1, p2, new Scalar(255,255,0));
        return mRgba;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8UC4);
        mRgbaF = new Mat(height,width, CvType.CV_8UC4);
        mRgbaT = new Mat(height,width, CvType.CV_8UC4);

        setResolution(1280,720);

        initBoundingBox();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        android.graphics.Rect r = new android.graphics.Rect();
        view.getDrawingRect(r);
        int touchX = (int) motionEvent.getX() - r.centerX() + (int) mRgba.size().width/2;
        int touchY = (int) motionEvent.getY() - r.centerY() + (int) mRgba.size().height/2;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE:
                Corner c = getCornerTouch(touchX, touchY);
                if(c != null)
                    switch (c) {
                        case TP_LEFT:
                            p1.x = touchX;
                            p1.y = touchY;
                            return true;
                        case TP_RIGHT:
                            p2.x = touchX;
                            p1.y = touchY;
                            return true;
                        case BTM_LEFT:
                            p1.x = touchX;
                            p2.y = touchY;
                            return true;
                        case BTM_RIGHT:
                            p2.x = touchX;
                            p2.y = touchY;
                    }
            break;
        }
        return true;
    }

    private Corner getCornerTouch(int touchX, int touchY) {
        if(isWithinRegion(touchX, touchY, p1))
            return Corner.TP_LEFT;
        else if (isWithinRegion(touchX, touchY, new Point(p2.x, p1.y)))
            return Corner.TP_RIGHT;
        else if (isWithinRegion(touchX, touchY, new Point(p1.x, p2.y)))
            return Corner.BTM_LEFT;
        else if (isWithinRegion(touchX, touchY, p2))
            return Corner.BTM_RIGHT;
        else
            return null;
    }

    private boolean isWithinRegion(int touchX, int touchY, Point p) {
        final int max_distance = 75;

        double dist = Math.sqrt((((touchX - p.x) * (touchX - p.x)) + (touchY - p.y)
                * (touchY - p.y)));
        if(dist <= max_distance)
            return true;

        return false;
    }
}