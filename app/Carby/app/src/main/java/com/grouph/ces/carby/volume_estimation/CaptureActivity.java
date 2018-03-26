package com.grouph.ces.carby.volume_estimation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.volume_estimation.DevMode.RecordFrame;
import com.grouph.ces.carby.volume_estimation.DevMode.ShowFramesActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


public final class CaptureActivity extends AppCompatActivity {

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

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.vol_capture);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        mOpenCvCameraView = findViewById(R.id.camera_preview);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(mOpenCvCameraView);
        mOpenCvCameraView.setOnTouchListener(mOpenCvCameraView);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (rc == PackageManager.PERMISSION_GRANTED)
            mOpenCvCameraView.enableView();
        else
            requestPermissions();

        refObjectToast = Toast.makeText(this, "No reference object detected", Toast.LENGTH_SHORT);
        FloatingActionButton captureButton = findViewById(R.id.btn_capture);
        captureButton.setOnClickListener((view) -> {
            if(mOpenCvCameraView.isRefObjectDetected())
                this.takePicture();
            else
                refObjectToast.show();
        });

        FloatingActionButton searchGallery = findViewById(R.id.search_gallery);
        searchGallery.setOnClickListener((view)-> {
            openGallery();
        });

        FloatingActionButton resetButton = findViewById(R.id.btn_reset);
        resetButton.setOnClickListener((view) -> {
            imagesTaken = 0;
        });

        Toast.makeText(this, "Drag the corners to fit the image", Toast.LENGTH_LONG).show();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
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
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        imageProcessor = new ImageProcessor(this);
        mOpenCvCameraView.enableView();

        imagesTaken = 0;
    }

    private void openGallery() {
//        Intent gallery =
//                new Intent(Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//        startActivityForResult(gallery, PICK_IMAGE);

        Intent rfGallery = new Intent(this, ShowFramesActivity.class);
        startActivityForResult(rfGallery,DEV_IMG);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (reqCode){
                case PICK_IMAGE:
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        final Mat mat = new Mat();

                        Utils.bitmapToMat(selectedImage, mat);
                        if (userSelectedImageBitmapList == null) {
                            userSelectedImageBitmapList = new ArrayList<Mat>();
                            Toast.makeText(this, "1st image chosen", Toast.LENGTH_LONG).show();
                        }
                        userSelectedImageBitmapList.add(mat);
                        if (userSelectedImageBitmapList.size() == 2) {
                            Toast.makeText(this, "2nd image chosen", Toast.LENGTH_LONG).show();
                            imageProcessor.addImage(userSelectedImageBitmapList.get(0), mOpenCvCameraView.getBoundingBox());
                            imageProcessor.addImage(userSelectedImageBitmapList.get(1), mOpenCvCameraView.getBoundingBox());
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
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                    break;
                case DEV_IMG:
                    addImage(data.getStringExtra(getResources().getString(R.string.rf1)));
                    addImage(data.getStringExtra(getResources().getString(R.string.rf2)));
                    startProcessor();
                    break;
                default:Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
            }
        } else
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
    }

    private void addImage(String name){
        RecordFrame rf = new RecordFrame(preferences,name);
        Mat mat = new Mat();
        Utils.bitmapToMat(rf.getFrame(), mat);
        imageProcessor.addImage(mat, rf.getBoundingBox());
    }

    private void startProcessor(){
        mOpenCvCameraView.disableView();
        imageProcessor.processImages();
    }

    private void requestPermissions() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{ Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_READ_PERM);
            return;
        }

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_WRITE_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };
    }

    public void takePicture() {
        imageProcessor.addImage(mOpenCvCameraView.getFrame(), mOpenCvCameraView.getBoundingBox());

        //save img if dev mode
        if (preferences.getBoolean(getResources().getString(R.string.key_dev_mode),false)) {
            RecordFrame rf = new RecordFrame(mOpenCvCameraView.getFrame(), mOpenCvCameraView.getBoundingBox());
            rf.saveObj(preferences);
//            Log.d(this.getClass().getName(),"compare:"+rf.equals(new RecordFrame(preferences,rf.getFileName())));
        }

        if(++imagesTaken == 2) {
            startProcessor();
        }

        if(imagesTaken == 1) {
            Toast.makeText(this, "Captured 1st image", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "Captured 2nd image", Toast.LENGTH_SHORT).show();
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Carby Images");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Carby Images", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }
}