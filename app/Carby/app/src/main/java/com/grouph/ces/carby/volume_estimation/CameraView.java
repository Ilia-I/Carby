package com.grouph.ces.carby.volume_estimation;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import android.content.Context;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CameraView extends JavaCameraView implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private static final String TAG = "myCameraView";
    private static final Size FRAME_SIZE = new Size(1280,720);

    private enum Corner { TP_LEFT, TP_RIGHT, BTM_LEFT, BTM_RIGHT, CENTRE }
    private Point p1, p2;
    private Frame frame;
    private OnCameraFrameRenderer frameRenderer;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setResolution(int width, int height) {
        disconnectCamera();
        mMaxHeight = height;
        mMaxWidth = width;
        connectCamera(getWidth(), getHeight());
    }

    //TODO Better fix for frame/slowdown bug
    public Frame getFrame() {
        Frame captured = new Frame(frame.getImage().clone(), frame.getReferenceObjectSize(), frame.getBoundingBox());
        frame.getImage().release();
        frame = new Frame();
        return captured;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.setResolution(1280,720);

        // Initialise bounding box
        final int BOX_SIZE = 300;
        p1 = new Point((FRAME_SIZE.width - BOX_SIZE)/2,(FRAME_SIZE.height - BOX_SIZE)/2);
        p2 = new Point((FRAME_SIZE.width + BOX_SIZE)/2, (FRAME_SIZE.height + BOX_SIZE)/2);

        frame = new Frame();
        frameRenderer = new OnCameraFrameRenderer();
        frameRenderer.updateBoundingBox(p1, p2);
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mRGBA = inputFrame.rgba();

        Mat frameImage = frame.getImage();
        mRGBA.copyTo(frameImage);
        frame.setBoundingBox(new Rect(p1, p2));
        frame.setReferenceObjectSize(frameRenderer.findPound(mRGBA));

        System.gc();
        return frameRenderer.render(mRGBA);
    }

    @Override
    public void onCameraViewStopped() {}

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
        int[] coordinates = toPreviewCoordinates((CameraView) view, motionEvent);
        int touchX = coordinates[0];
        int touchY = coordinates[1];

        touchX = touchX >= 1280 ? 1280 : touchX;
        touchX = touchX <= 0 ? 0 : touchX;
        touchY= touchY >= 720 ? 720 : touchY;
        touchY = touchY <= 0 ? 0 : touchY;

        Corner c = getCornerTouch(touchX, touchY);
        if(c == null)
            return true;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE:
                frameRenderer.setBoundingBoxColour(new Scalar(255,0,0));
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
                            return true;
                        case CENTRE:
                            double xDist = (p2.x-p1.x)/2;
                            double yDist = (p2.y-p1.y)/2;
                            touchX = touchX >= 1280-(int)xDist ? 1280-(int)xDist : touchX;
                            touchX = touchX <= 0+(int)xDist ? 0+(int)xDist: touchX;
                            touchY= touchY >= 720-(int)yDist  ? 720-(int)yDist: touchY;
                            touchY = touchY <= 0+(int)yDist ? 0+(int)yDist: touchY;

                            p1.x = touchX - xDist;
                            p1.y = touchY - yDist;
                            p2.x = touchX + xDist;
                            p2.y = touchY + yDist;
                    }
                    frameRenderer.updateBoundingBox(p1, p2);
                break;
            case MotionEvent.ACTION_UP:
                frameRenderer.setBoundingBoxColour(new Scalar(255,255,0));
                break;
        }
        return true;
    }

    @Nullable
    private Corner getCornerTouch(int touchX, int touchY) {
        if(isWithinRegion(touchX, touchY, p1))
            return Corner.TP_LEFT;
        else if (isWithinRegion(touchX, touchY, new Point(p2.x, p1.y)))
            return Corner.TP_RIGHT;
        else if (isWithinRegion(touchX, touchY, new Point(p1.x, p2.y)))
            return Corner.BTM_LEFT;
        else if (isWithinRegion(touchX, touchY, p2))
            return Corner.BTM_RIGHT;
        else if (isWithinRegion(touchX, touchY, new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)))
            return Corner.CENTRE;
        else
            return null;
    }

    private boolean isWithinRegion(int touchX, int touchY, @NonNull Point p) {
        final int max_distance = 75;

        double dist = Math.sqrt((((touchX - p.x) * (touchX - p.x)) + (touchY - p.y)
                * (touchY - p.y)));

        return dist <= max_distance;

    }


}