package com.grouph.ces.carby.volume_estimation;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.grouph.ces.carby.volume_estimation.DevMode.ShowFramesFragment;

import org.opencv.android.OpenCVLoader;

/**
 * Created by Martin Peev on 27.03.2018 г..
 * Version: 0.6
 */

public class VolEstActivity extends AppCompatActivity{
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_WRITE_PERM = 3;
    private static final int RC_HANDLE_READ_PERM = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(this.getClass().getName(), "onCreate: NO permit");
            requestPermissions(this);
        } else {
            Log.e(this.getClass().getName(), "onCreate:  permit");
            setFragment(new CaptureFragment(), new Bundle(), "");
        }
    }

    public void setFragmentCapture(Bundle bundle){
        Log.d(this.getClass().getName(),"setFragmentCapture");
        setFragment(new CaptureFragment(),bundle, "capture_fragment");
    }

    public void setFragmentShowFrames(Bundle bundle){
        Log.d(this.getClass().getName(),"setFragmentShowFrames");
        setFragment(new ShowFramesFragment(),bundle, "show_frames_fragment");
    }

    public void setFragmentResults(Bundle bundle){
        Log.d(this.getClass().getName(),"setFragmentResults");
        setFragment(new ResultsFragment(),bundle, "results_fragment");
    }

    private void setFragment(Fragment fragment, Bundle bundle, String backstackKey){
        FragmentManager fm = getFragmentManager();
        if (fm != null) {
            fragment.setArguments(bundle);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(android.R.id.content, fragment);
            if(!backstackKey.isEmpty())ft.addToBackStack(backstackKey);
            ft.commit();
            getSupportActionBar().show();
        }
    }

    public void performTestCalculation() {
        if (!OpenCVLoader.initDebug()) {}
        IntegralApproximation a = new IntegralApproximation(this);
        if(a.loadTestMats()) {
            double volume = a.getApproximation();
            a.showResults(volume);
        }
    }

    public void requestPermissions(Activity activity) {
        Log.e(this.getClass().getName(), "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(activity, permissions, RC_HANDLE_CAMERA_PERM);
            setFragment(new CaptureFragment(),new Bundle(),"");
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e(this.getClass().getName(),"onRequestPermissionResult");
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.e(this.getClass().getName(), "Camera permission granted - initialize the camera source");
            setFragment(new CaptureFragment(), new Bundle(), "");
            return;
        } else {
            Log.e(this.getClass().getName(), "Permission not granted: results len = " + grantResults.length +
                    " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
        }
    }
}
