package com.grouph.ces.carby.volume_estimation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Martin Peev on 27.03.2018 г..
 * Version: 0.1
 */

public class VolEstActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new CaptureFragment())
                .commit();
    }

}
