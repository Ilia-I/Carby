package com.grouph.ces.carby.volume_estimation;

import android.view.MotionEvent;
import android.view.View;

import com.grouph.ces.carby.volume_estimation.ImageTasks.FindPoundTask;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.ExecutionException;

/**
 * Created by matthewball on 28/03/2018.
 */

public class OnCameraFrameRenderer {

    private Scalar boxColor = new Scalar(255, 255,0);
    private Point p1 = new Point();
    private Point p2 = new Point();

    private Point circleCenter;
    private double circleRadius = -1.0;

    private Point prevCenter;
    private double prevRadius = -1.0;

    private int frameResetCount = 0;

    public OnCameraFrameRenderer() {}

    public double findPound(Mat inputFrame) {
        try {
            FindPoundTask.Result result = new FindPoundTask().execute(inputFrame).get();
            circleCenter = result.center;
            circleRadius = result.radius;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return circleRadius;
    }

    public void updateBoundingBox(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public void setBoundingBoxColour(Scalar s) {
        boxColor = s;
    }


    public Mat render(Mat inputFrame, boolean drawRefObject) {
        if(drawRefObject)
            if(circleRadius != -1.0) {
                // circle center
                Imgproc.circle(inputFrame, circleCenter, 3, new Scalar(0, 255, 0), -1);
                // circle outline
                Imgproc.circle(inputFrame, circleCenter, (int) circleRadius, new Scalar(255, 0, 0), 3);

                prevCenter = circleCenter;
                prevRadius = circleRadius;

                frameResetCount = 0;
            } else {
                if(prevRadius != -1.0 && frameResetCount < 10) {
                    // circle center
                    Imgproc.circle(inputFrame, prevCenter, 3, new Scalar(0, 255, 0), -1);
                    // circle outline
                    Imgproc.circle(inputFrame, prevCenter, (int) prevRadius, new Scalar(255, 0, 0), 3);
                }
                frameResetCount++;
            }

        // Draw bounding box
        Imgproc.rectangle(inputFrame, p1, p2, boxColor, 3, Imgproc.LINE_AA,0);
        Imgproc.circle(inputFrame, p1, 5, boxColor, 34);
        Imgproc.circle(inputFrame, new Point(p2.x, p1.y), 5, boxColor, 34);
        Imgproc.circle(inputFrame, new Point(p1.x, p2.y), 5, boxColor, 34);
        Imgproc.circle(inputFrame, p2, 5, boxColor, 34);

        return inputFrame;
    }

}
