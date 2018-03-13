package com.grouph.ces.carby.volume_estimation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.grouph.ces.carby.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;


public final class VolumeCaptureActivity extends AppCompatActivity {

    private static String TAG = "VolumeCpature";

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_WRITE_PERM = 3;
    private static final int RC_HANDLE_READ_PERM = 4;

    // Intent to capture image
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE = 100;

    private Camera mCamera;
    private CameraPreview mPreview;
    private ImageView mImageView;
    private ImageView squareOverlay;

    private ImageProcessor imageProcessor;

    private boolean imageTaken = false;

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.vol_capture);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (rc == PackageManager.PERMISSION_GRANTED) {
            mCamera = getCameraInstance();
        } else {
            requestPermissions();
        }

        mPreview = new CameraPreview(this, mCamera);

        mImageView = findViewById(R.id.vol_capture_image);
        mImageView.setVisibility(View.GONE);

        FrameLayout previewFrame = findViewById(R.id.camera_preview);
        previewFrame.addView(mPreview);

        squareOverlay = new ImageView(this.getApplicationContext());
        squareOverlay.setImageResource(R.drawable.ic_square_overlay);

        int sizeInPx = dpToPx(200);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeInPx,sizeInPx);
        params.gravity = Gravity.CENTER;

        squareOverlay.setLayoutParams(params);

        previewFrame.addView(squareOverlay);

        FloatingActionButton captureButton = findViewById(R.id.btn_capture);
        captureButton.setOnClickListener((view) -> {
            if(!imageTaken) {
                squareOverlay.setVisibility(View.INVISIBLE);
                mCamera.takePicture(null, null, new PictureCallback(mImageView, imageProcessor));
                mImageView.setVisibility(View.VISIBLE);
                mPreview.setVisibility(View.GONE);
                imageTaken = !imageTaken;
            }
        });

        FloatingActionButton searchGallery = findViewById(R.id.search_gallery);
        searchGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        FloatingActionButton resetButton = findViewById(R.id.btn_reset);
        resetButton.setOnClickListener((view) -> {
            if(imageTaken) {
                squareOverlay.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.GONE);
                mPreview.setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(null);
                imageTaken = !imageTaken;
            }
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
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);

            imageProcessor = new ImageProcessor(this);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
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

                squareOverlay.setVisibility(View.INVISIBLE);
                mImageView.setImageBitmap(imageProcessor.performGrabCut(selectedImage));
                mImageView.setRotation(90);
                mImageView.setVisibility(View.VISIBLE);
                mPreview.setVisibility(View.GONE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        } else
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
    }

    /** A safe way to get an instance of the Camera object. */

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
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

        Snackbar.make(mPreview, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }


}
