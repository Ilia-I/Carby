package com.grouph.ces.carby.volume_estimation;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CameraView extends JavaCameraView implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private static final String TAG = "myCameraView";

    private int counter = 0;

    private int boxSize = 300;
    private Point p1;
    private Point p2;
    private Scalar boxColor;

    private Mat mRgba;
    private Mat orignalFrame;

    private boolean refObjectDetected = false;

    public boolean isRefObjectDetected() {
        return refObjectDetected;
    }

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

    private class FindRefObjectTask extends AsyncTask<Mat, Void, Mat> {

        @Override
        protected Mat doInBackground(Mat... mats) {
            int scalingFactor = 4;
            Mat src = mats[0];
            Mat blurred = new Mat();
            Imgproc.resize(src,blurred, new org.opencv.core.Size(src.width()/scalingFactor,src.height()/scalingFactor));
            Mat output = blurred.clone();

            Imgproc.medianBlur(blurred, blurred, 7);

            Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

            List<MatOfPoint> contours = new ArrayList<>();
            List<Mat> blurredChannel = new ArrayList<>();
            blurredChannel.add(blurred);
            List<Mat> gray0Channel = new ArrayList<>();
            gray0Channel.add(gray0);

            MatOfPoint2f approxCurve;
            double minArea = 16000/(scalingFactor*scalingFactor);
            double maxArea = 80000/(scalingFactor*scalingFactor);
            int maxId = -1;

            //find contours for all 3 channels
            for (int c = 0; c < 3; c++) {
                int ch[] = { c, 0 };

                Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
                Imgproc.Canny(gray0, gray, 15, 30, 3, true);
                Imgproc.dilate(gray,gray,new Mat());
                Imgproc.findContours(gray, contours, new Mat(),
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());
                    Imgproc.boundingRect(contour);
                    double area = Imgproc.contourArea(contour);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.07, true);

                    if (approxCurve.total() == 4 && area >= minArea && area <= maxArea) {
                        RotatedRect rect = Imgproc.minAreaRect(temp);
                        Point points[] = new Point[4];
                        rect.points(points);
                        if (checkRatio(points)){
                            minArea = area;
                            maxId = contours.indexOf(contour);
                        }
                    }
                }
            }

            if(maxId >= 0) {
                refObjectDetected = true;
                Imgproc.drawContours(output, contours, maxId, new Scalar(255, 0,0), 1);
                Imgproc.resize(output,output, new org.opencv.core.Size(src.width(),src.height()));
                return output;
            }

            refObjectDetected = false;
            return mats[0];
        }
    }

    //checks if the ratio of the detected card is within the actual dimension limit
    private boolean checkRatio(Point[] points) {
        double width = points[2].x - points[1].x;
        double height = points[0].y - points[1].y;
        double ratio = width / height;
        if ((1.5 < ratio && ratio < 1.7) || (0.53 < ratio && ratio < 0.73)) {
            return true;
        }
        return false;

    }

    public Mat getFrame() {
        return orignalFrame;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        orignalFrame = inputFrame.rgba().clone();
        mRgba = inputFrame.rgba();

        try {
            mRgba = new FindRefObjectTask().execute(mRgba).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Imgproc.rectangle(mRgba, p1, p2, boxColor, 3, Imgproc.LINE_AA,0);
        return mRgba;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8UC4);

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
        p1 = new Point((mRgba.size().width-boxSize)/2,(mRgba.size().height-boxSize)/2);
        p2 = new Point((mRgba.size().width+boxSize)/2, (mRgba.size().height+boxSize)/2);
        boxColor = new Scalar(255, 255,0);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        android.graphics.Rect r = new android.graphics.Rect();
        view.getDrawingRect(r);
        int touchX = (int) motionEvent.getX() - r.centerX() + (int) mRgba.size().width/2;
        int touchY = (int) motionEvent.getY() - r.centerY() + (int) mRgba.size().height/2;

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