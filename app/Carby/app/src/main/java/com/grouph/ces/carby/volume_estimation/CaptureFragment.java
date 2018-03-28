package com.grouph.ces.carby.volume_estimation;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.volume_estimation.DevMode.RecordFrame;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public final class CaptureFragment extends Fragment {

    private static String TAG = "VolumeCapture";

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_WRITE_PERM = 3;
    private static final int RC_HANDLE_READ_PERM = 4;

    private static final int PICK_IMAGE = 100;
    private static final int DEV_IMG = 7;

    private CameraView mOpenCvCameraView;
    private ImageProcessor imageProcessor;
    private List<Mat> userSelectedImageBitmapList = null;

    private int imagesTaken = 0;
    private SharedPreferences preferences;
    private Toast refObjectToast;

    private VolEstActivity activityRef;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.vol_capture, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

        refObjectToast = Toast.makeText(getActivity(), "No reference object detected", Toast.LENGTH_SHORT);
        FloatingActionButton captureButton = getView().findViewById(R.id.btn_capture);
        captureButton.setOnClickListener((view) -> {
            Frame frame = mOpenCvCameraView.getFrame();
            if(frame.getReferenceObjectSize() > 0)
                this.captureFrame(frame);
            else
                refObjectToast.show();
        });

        FloatingActionButton searchGallery = getView().findViewById(R.id.search_gallery);
        searchGallery.setOnClickListener((view)-> {
            openGallery();
        });

        FloatingActionButton resetButton = getView().findViewById(R.id.btn_reset);
        resetButton.setOnClickListener((view) -> {
            imagesTaken = 0;
        });

        Toast.makeText(getActivity(), "Drag the corners to fit the image", Toast.LENGTH_LONG).show();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getContext()) {
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
//        Intent gallery =
//                new Intent(Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//        startActivityForResult(gallery, PICK_IMAGE);

//        Intent rfGallery = new Intent(getActivity(), ShowFramesFragment.class);
//        startActivityForResult(rfGallery,DEV_IMG);
        activityRef.setFragmentShowFrames(new Bundle());
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (reqCode){
                case PICK_IMAGE:
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        final Mat mat = new Mat();

                        Utils.bitmapToMat(selectedImage, mat);
                        if (userSelectedImageBitmapList == null) {
                            userSelectedImageBitmapList = new ArrayList<>();
                            Toast.makeText(getActivity(), "1st image chosen", Toast.LENGTH_LONG).show();
                        }
                        userSelectedImageBitmapList.add(mat);
                        if (userSelectedImageBitmapList.size() == 2) {
                            Toast.makeText(getActivity(), "2nd image chosen", Toast.LENGTH_LONG).show();
                            mOpenCvCameraView.disableView();
                            imageProcessor.processImages();
                            userSelectedImageBitmapList = null;
                        }

    //                try {
    //                    File newFile = ProcessingAlgorithms.getOutputMediaFile(MEDIA_TYPE_IMAGE);
    //                    FileOutputStream fos = new FileOutputStream(newFile);
    //                    selectedImage.compress(Bitmap.CompressFormat.PNG, 90, fos);
    //                    fos.close();
    //                } catch (IOException e) {
    //                    e.printStackTrace();
    //                }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                    break;
                case DEV_IMG:
                    addImage(data.getStringExtra(getResources().getString(R.string.rf1)));
                    addImage(data.getStringExtra(getResources().getString(R.string.rf2)));
                    startProcessor();
                    break;
                default:Toast.makeText(getActivity(), "You haven't picked Image",Toast.LENGTH_LONG).show();
            }
        } else
            Toast.makeText(getActivity(), "You haven't picked Image",Toast.LENGTH_LONG).show();
    }

    private void addImage(String name){
        RecordFrame rf = new RecordFrame(preferences, name);
        Mat mat = new Mat();
        Utils.bitmapToMat(rf.getImage(), mat);
        imageProcessor.addImage(new Frame(mat, rf.getPixelsPerCm(), rf.getBoundingBox()));
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
        if (preferences.getBoolean(getResources().getString(R.string.key_dev_mode),false)) {
            RecordFrame rf = new RecordFrame(frame);
            rf.saveObj(preferences);
        }

        if(++imagesTaken == 1)
            Toast.makeText(getActivity(), "Captured 1st image", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(getActivity(), "Captured 2nd image", Toast.LENGTH_SHORT).show();
            startProcessor();
        }
    }

}