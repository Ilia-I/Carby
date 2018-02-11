package com.grouph.ces.carby.ocr;

import android.support.design.widget.FloatingActionButton;
import android.view.View;

/**
 * Created by Martin Peev on 11.02.2018 г..
 * Version: 0.1
 */

class OcrController {
    private OcrCaptureActivity activity;
    private FloatingActionButton scanBtn;

    public OcrController(OcrCaptureActivity ocrCaptureActivity, FloatingActionButton scanBtn) {
        this.activity = ocrCaptureActivity;
        this.scanBtn = scanBtn;
        setupListeners();
    }

    private void setupListeners() {
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.scan();
            }
        });
    }
}
