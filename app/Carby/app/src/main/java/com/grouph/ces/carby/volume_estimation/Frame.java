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
    private double referenceObjectSize = -1.0;

    public Frame() {}

    public Frame(Mat image, double size, Rect boundingBox) {
        this.image = image.clone();
        this.referenceObjectSize = size;
        this.boundingBox = boundingBox;
    }

    public Mat getImage() {
        return image;
    }

    public double getReferenceObjectSize() {
        return referenceObjectSize;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public void setImage(Mat image) {
        this.image = image;
    }

    public void setBoundingBox(Rect boundingBox) {
        this.boundingBox = boundingBox;
    }

    public void setReferenceObjectSize(double referenceObjectSize) {
        this.referenceObjectSize = referenceObjectSize;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,
                "Mat: %s\nBounding box %s\nPixel density of image: %f",
                image,
                boundingBox,
                referenceObjectSize);
    }
}
