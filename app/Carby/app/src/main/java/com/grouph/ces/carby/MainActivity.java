package com.grouph.ces.carby;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.grouph.ces.carby.barcodescanner.BarcodeCaptureActivity;
import com.grouph.ces.carby.ocr.OcrCaptureActivity;
import com.grouph.ces.carby.volume_estimation.CaptureActivity;

public class MainActivity extends BaseActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        LayoutInflater layoutInflater = getLayoutInflater();
        View activityContent = layoutInflater.inflate(R.layout.content_main, null);
        contentFrame.addView(activityContent);

        View mBarcodeTile = findViewById(R.id.colored_bar1);
        View mOcrTile = findViewById(R.id.colored_bar2);
        View mVolumeTile = findViewById(R.id.colored_bar3);

        mBarcodeTile.setOnClickListener(this);
        mOcrTile.setOnClickListener(this);
        mVolumeTile.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.colored_bar1) {
            Intent myIntent = new Intent(this, BarcodeCaptureActivity.class);
            startActivity(myIntent);
            // Handle the camera action
        } else if (id == R.id.colored_bar2) {
            Intent ocr = new Intent(this, OcrCaptureActivity.class);
            startActivity(ocr);
        } else if (id == R.id.colored_bar3) {
            Intent volume = new Intent(this, CaptureActivity.class);
            startActivity(volume);
        }
    }
}
