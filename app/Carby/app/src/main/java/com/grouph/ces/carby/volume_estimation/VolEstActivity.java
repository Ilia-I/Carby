package com.grouph.ces.carby.volume_estimation;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.grouph.ces.carby.volume_estimation.DevMode.ShowFramesFragment;

/**
 * Created by Martin Peev on 27.03.2018 г..
 * Version: 0.4
 */

public class VolEstActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFragmentCapture(new Bundle());
    }

    public void setFragmentCapture(Bundle bundle){
        Log.d(this.getClass().getName(),"setFragmentCapture");
        setFragment(new CaptureFragment(),bundle);
    }

    public void setFragmentShowFrames(Bundle bundle){
        Log.d(this.getClass().getName(),"setFragmentShowFrames");
        setFragment(new ShowFramesFragment(),bundle);
    }

    public void setFragmentResults(Bundle bundle){
        Log.d(this.getClass().getName(),"setFragmentResults");
        setFragment(new ResultsFragment(),bundle);
    }

    private void setFragment(Fragment fragment, Bundle bundle){
        FragmentManager fm = getFragmentManager();
        if (fm != null) {
            fragment.setArguments(bundle);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(android.R.id.content, fragment);
            ft.commit();
        }
    }
}
