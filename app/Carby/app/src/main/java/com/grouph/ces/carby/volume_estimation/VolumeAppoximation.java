package com.grouph.ces.carby.volume_estimation;

import org.opencv.core.Mat;

/**
 * Created by matthewball on 22/03/2018.
 */

public class VolumeAppoximation {

    private int numParts = 10;

    private Mat side;
    private Mat top;

    public VolumeAppoximation(Mat side, Mat top) {
        this.side = side;
        this.top = top;
    }


}
