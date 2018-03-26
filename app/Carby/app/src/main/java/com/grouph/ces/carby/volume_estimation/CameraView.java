package com.grouph.ces.carby.volume_estimation;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.grouph.ces.carby.volume_estimation.ImageTasks.FindPoundTask;

import java.util.concurrent.ExecutionException;

public class CameraView extends JavaCameraView implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private static final String TAG = "myCameraView";
    private enum Corner { TP_LEFT, TP_RIGHT, BTM_LEFT, BTM_RIGHT }

    private Point p1;
    private Point p2;
    private Scalar boxColor = new Scalar(255, 255,0);
    private Mat mRgba;
    private Frame frame;

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

    public Frame getFrame() {
        return frame;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Mat originalImage = mRgba.clone();

        double result = -1;

        try {
            result = new FindPoundTask().execute(mRgba).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        frame.setBoundingBox(new Rect(p1, p2));
        frame.setImage(originalImage);
        frame.setPixelsPerCm(result);

        Imgproc.rectangle(mRgba, p1, p2, boxColor, 3, Imgproc.LINE_AA,0);
        Imgproc.circle(mRgba, p1, 5, boxColor, 34);
        Imgproc.circle(mRgba, new Point(p2.x, p1.y), 5, boxColor, 34);
        Imgproc.circle(mRgba, new Point(p1.x, p2.y), 5, boxColor, 34);
        Imgproc.circle(mRgba, p2, 5, boxColor, 34);
        return mRgba;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8UC4);
        frame = new Frame();
        setResolution(1280,720);

        initBoundingBox();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Rect getBoundingBox() {
        return new Rect(p1,p2);
    }

    private void initBoundingBox() {
        final int BOX_SIZE = 300;
        p1 = new Point((mRgba.size().width - BOX_SIZE)/2,(mRgba.size().height - BOX_SIZE)/2);
        p2 = new Point((mRgba.size().width + BOX_SIZE)/2, (mRgba.size().height + BOX_SIZE)/2);
        boxColor = new Scalar(255, 255,0);
    }

    private int[] toPreviewCoordinates(CameraView view, MotionEvent e) {
        float pixelRatio = view.getHeight() / 720f;
        float width = view.getHeight() * 16 / 9;
        float offset = (view.getWidth() - width) / 2;

        int x = (int) ((e.getX() - offset)/pixelRatio);
        int y = (int) (e.getY() / pixelRatio);
        return new int[] {x,y};
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int[] coords = toPreviewCoordinates((CameraView) view, motionEvent);
        int touchX = coords[0];
        int touchY = coords[1];

        touchX = touchX >= 1280 ? 1280 : touchX;
        touchX = touchX <= 0 ? 0 : touchX;
        touchY= touchY >= 720 ? 720 : touchY;
        touchY = touchY <= 0 ? 0 : touchY;

        Corner c = getCornerTouch(touchX, touchY);
        if(c == null)
            return true;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE:
                boxColor = new Scalar(255,0,0);
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
            case MotionEvent.ACTION_UP:
                boxColor = new Scalar(255, 255, 0);
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