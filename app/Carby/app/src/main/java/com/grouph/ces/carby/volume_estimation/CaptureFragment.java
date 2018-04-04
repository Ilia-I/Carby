package com.grouph.ces.carby.volume_estimation;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.volume_estimation.DevMode.RecordFrame;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


public final class CaptureFragment extends Fragment {

    private static String TAG = "VolumeCapture";

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_WRITE_PERM = 3;
    private static final int RC_HANDLE_READ_PERM = 4;

    private CameraView mOpenCvCameraView;
    private ImageProcessor imageProcessor;

    private int imagesTaken = 0;
    private SharedPreferences preferences;
    private Toast toast;
    private VolEstActivity activityRef;
    private Handler handler = new Handler();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.vol_capture, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.food_selection, menu);
        super.onCreateOptionsMenu(menu, inflater);
        switch (preferences.getInt("foodType", 0)) {
            case NutritionInformationCalculator.FOOD_OATS:
                menu.findItem(R.id.food_oats).setChecked(true);
            case NutritionInformationCalculator.FOOD_PASTA_BOILED:
                menu.findItem(R.id.food_pasta_boiled).setChecked(true);
                break;
            case NutritionInformationCalculator.FOOD_NOODLES_BOILED:
                menu.findItem(R.id.food_noodles_boiled).setChecked(true);
                break;
            case NutritionInformationCalculator.FOOD_RICE_BOILED:
                menu.findItem(R.id.food_rice_boiled).setChecked(true);
                break;
            case NutritionInformationCalculator.FOOD_POTATO_BOILED:
                menu.findItem(R.id.food_potato_boiled).setChecked(true);
                break;
            case NutritionInformationCalculator.FOOD_POTATO_SWEET:
                menu.findItem(R.id.food_potato_sweet).setChecked(true);
                break;
            case NutritionInformationCalculator.FOOD_EGG_BOILED:
                menu.findItem(R.id.food_egg_boiled).setChecked(true);
                break;
            default:
                preferences.edit().putInt("foodType", NutritionInformationCalculator.FOOD_BREAD).apply();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.food_bread:
                preferences.edit().putInt("foodType", NutritionInformationCalculator.FOOD_BREAD).apply();
                item.setChecked(true);
                return true;
            case R.id.food_oats:
                preferences.edit().putInt("foodType", NutritionInformationCalculator.FOOD_OATS).apply();
                item.setChecked(true);
                return true;
            case R.id.food_egg_boiled:
                preferences.edit().putInt("foodType", NutritionInformationCalculator.FOOD_EGG_BOILED).apply();
                item.setChecked(true);
                return true;
            case R.id.food_noodles_boiled:
                preferences.edit().putInt("foodType", NutritionInformationCalculator.FOOD_NOODLES_BOILED).apply();
                item.setChecked(true);
                return true;
            case R.id.food_pasta_boiled:
                preferences.edit().putInt("foodType", NutritionInformationCalculator.FOOD_PASTA_BOILED).apply();
                item.setChecked(true);
                return true;
            case R.id.food_potato_boiled:
                preferences.edit().putInt("foodType", NutritionInformationCalculator.FOOD_POTATO_BOILED).apply();
                item.setChecked(true);
                return true;
            case R.id.food_potato_sweet:
                preferences.edit().putInt("foodType", NutritionInformationCalculator.FOOD_POTATO_SWEET).apply();
                item.setChecked(true);
                return true;
            case R.id.food_rice_boiled:
                preferences.edit().putInt("foodType", NutritionInformationCalculator.FOOD_RICE_BOILED).apply();
                item.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        activityRef = (VolEstActivity) getActivity();

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);

        mOpenCvCameraView = getView().findViewById(R.id.camera_preview);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(mOpenCvCameraView);
        mOpenCvCameraView.setOnTouchListener(mOpenCvCameraView);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (rc == PackageManager.PERMISSION_GRANTED)
            mOpenCvCameraView.enableView();
        else
            requestPermissions();


        FloatingActionButton captureButton = getView().findViewById(R.id.btn_capture);
        captureButton.setOnClickListener((view) -> {
            captureButton.setEnabled(false);
            toast = Toast.makeText(getActivity(), "Capturing image", Toast.LENGTH_SHORT);
            toast.show();
            Runnable checkForReferenceObject = new Runnable() {
                @Override
                public void run() {
                    try {
                        Frame frame = mOpenCvCameraView.getFrame();
                        if (frame.getReferenceObjectSize() > 0) {
                            captureFrame(frame);
                            captureButton.setEnabled(true);
                        }
                        else {
                            handler.postDelayed(this, 50);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            checkForReferenceObject.run();
        });

        FloatingActionButton searchGallery = getView().findViewById(R.id.search_gallery);
        searchGallery.setOnClickListener((view)-> {
            openGallery();
        });

        FloatingActionButton resetButton = getView().findViewById(R.id.btn_reset);
        resetButton.setOnClickListener((view) -> {
            imagesTaken = 0;
        });

        Toast.makeText(getActivity(), "Drag the corners to fit the image", Toast.LENGTH_SHORT).show();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getActivity(), mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        imageProcessor = new ImageProcessor(activityRef);
        mOpenCvCameraView.enableView();

        imagesTaken = 0;
    }

    private void openGallery() {
        activityRef.setFragmentShowFrames(new Bundle());
    }

    private void startProcessor() {
        mOpenCvCameraView.disableView();
        imageProcessor.processImages();
    }

    private void requestPermissions() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{ Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(getActivity(), permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(getActivity(), permissions, RC_HANDLE_READ_PERM);
            return;
        }

        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(getActivity(), permissions, RC_HANDLE_WRITE_PERM);
            return;
        }

        final Activity thisActivity = getActivity();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };
    }

    public void captureFrame(Frame frame) {
        imageProcessor.addImage(frame);

        //save img if dev mode
        if (preferences.getBoolean(getResources().getString(R.string.key_dev_mode), false)) {
            RecordFrame rf = new RecordFrame(frame);
            rf.saveObj(preferences);
        }

        if (++imagesTaken == 1) {
            toast.setText("Captured 1st image");
            toast.show();
        }
        else {
            toast.setText("Captured 2nd image");
            toast.show();
            startProcessor();
        }
    }

}