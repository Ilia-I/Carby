package com.grouph.ces.carby.volume_estimation;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.volume_estimation.DevMode.ShowFramesFragment;

/**
 * Created by Martin Peev on 27.03.2018 г..
 * Version: 0.2
 */

public class VolEstActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFragmentCapture();
    }

    public void setFragmentCapture(){
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new CaptureFragment())
                .commit();
    }

    public void setFragmentShowFrames(){
        FragmentManager fm = getFragmentManager();

        if (fm != null) {
            // Perform the FragmentTransaction to load in the list tab content.
            // Using FragmentTransaction#replace will destroy any Fragments
            // currently inside R.id.fragment_content and add the new Fragment
            // in its place.
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(android.R.id.content, new ShowFramesFragment());
            ft.commit();
        }
    }
}
