package com.grouph.ces.carby.volume_estimation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.grouph.ces.carby.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


public final class CaptureActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, Camera.PictureCallback{

    private static String TAG = "VolumeCpature";

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_WRITE_PERM = 3;
    private static final int RC_HANDLE_READ_PERM = 4;

    private static final int PICK_IMAGE = 100;

    private CameraView mOpenCvCameraView;
    private ImageProcessor imageProcessor;

    private Mat mRgba;
    private Mat mRgbaF;
    private Mat mRgbaT;

    private int imagesTaken = 0;
    private Bitmap b1;
    private Bitmap b2;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.vol_capture);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        mOpenCvCameraView = findViewById(R.id.camera_preview);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(1280,720);

        if (rc == PackageManager.PERMISSION_GRANTED)
            mOpenCvCameraView.enableView();
        else
            requestPermissions();

        FloatingActionButton captureButton = findViewById(R.id.btn_capture);
        captureButton.setOnClickListener((view) -> {
            mOpenCvCameraView.takePicture("Hello", this);
        });

        FloatingActionButton searchGallery = findViewById(R.id.search_gallery);
        searchGallery.setOnClickListener((view)-> {
            openGallery();
        });

        FloatingActionButton resetButton = findViewById(R.id.btn_reset);
        resetButton.setOnClickListener((view) -> {
            imagesTaken = 0;
        });
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
        Intent gallery =
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

//                try {
//                    File newFile = ProcessingAlgorithms.getOutputMediaFile(MEDIA_TYPE_IMAGE);
//                    FileOutputStream fos = new FileOutputStream(newFile);
//                    selectedImage.compress(Bitmap.CompressFormat.PNG, 90, fos);
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                imageProcessor.addImage(selectedImage);
                imagesTaken++;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        } else
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
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


    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8UC4);
        mRgbaF = new Mat(height,width, CvType.CV_8UC4);
        mRgbaT = new Mat(height,width, CvType.CV_8UC4);

        mOpenCvCameraView.setResolution(1280,720);
        mOpenCvCameraView.getPreviewSize();
        mOpenCvCameraView.getPictureSize();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

//        Core.transpose(mRgba, mRgbaT);
//        Imgproc.resize(mRgbaT,mRgbaF,mRgbaF.size(),0,0,0);
//        Core.flip(mRgbaF, mRgba,1);

        int box = 300;

        org.opencv.core.Point p1 = new org.opencv.core.Point((mRgba.size().width-box)/2,(mRgba.size().height-box)/2);
        org.opencv.core.Point p2 = new org.opencv.core.Point((mRgba.size().width+box)/2, (mRgba.size().height+box)/2);
        Imgproc.rectangle(mRgba, p1, p2, new Scalar(255,255,0));
        return mRgba;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap pictureTaken = BitmapFactory.decodeByteArray(data, 0, data.length);

        imageProcessor.addImage(pictureTaken);

        if(++imagesTaken == 2) {
            mOpenCvCameraView.disableView();
            imageProcessor.processImages();
        }

        if(imagesTaken == 1)
            Toast.makeText(this, "Captured 1st image", Toast.LENGTH_SHORT).show();
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
