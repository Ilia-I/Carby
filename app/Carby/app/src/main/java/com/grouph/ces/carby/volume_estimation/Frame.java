package com.grouph.ces.carby.volume_estimation;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.Locale;

/**
 * Created by matthewball on 26/03/2018.
 */

public class Frame {
    private Mat image = new Mat();
    private Rect boundingBox = new Rect();
    private double pixelsPerCm = -1.0;

    public Frame() {}

    public Frame(Mat image, double pixelsPerCm, Rect boundingBox) {
        this.image = image.clone();
        this.pixelsPerCm = pixelsPerCm;
        this.boundingBox = boundingBox;
    }

    public Mat getImage() {
        return image;
    }

    public double getPixelsPerCm() {
        return pixelsPerCm;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public void setImage(Mat image) {
        this.image = image;
    }

    public void setPixelsPerCm(double pixelsPerCm) {
        this.pixelsPerCm = pixelsPerCm;
    }

    public void setBoundingBox(Rect boundingBox) {
        this.boundingBox = boundingBox;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,
                "Mat: %s\nBounding box %s\nPixel density of image: %f",
                image,
                boundingBox,
                pixelsPerCm);
    }
}
