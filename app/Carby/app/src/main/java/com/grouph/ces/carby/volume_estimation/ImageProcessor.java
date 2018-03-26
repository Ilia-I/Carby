package com.grouph.ces.carby.volume_estimation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;

import com.grouph.ces.carby.volume_estimation.ImageTasks.FindPoundTask;
import com.grouph.ces.carby.volume_estimation.ImageTasks.GrabCutTask;

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Created by matthewball on 13/03/2018.
 */

public class ImageProcessor {

    private static String TAG = "ImageProcessor";

    private Context context;

    private Mat topDownIn = null;
    private Mat sideIn = null;
    private org.opencv.core.Rect boundingBox1;
    private org.opencv.core.Rect boundingBox2;
    private Bitmap topDownOut;
    private Bitmap sideOut;
    private Bitmap refObj1;
    private Bitmap refObj2;

    private ProcessingAlgorithms algorithms;

    public ImageProcessor(Context context) {
        this.context = context;
        this.algorithms = new ProcessingAlgorithms(context);
    }

    public ProcessingAlgorithms getAlgorithms() {
        return algorithms;
    }

    public void addImage(Mat image, org.opencv.core.Rect boundingBox) {
        if(topDownIn == null) {
            topDownIn = image.clone();
            boundingBox1 = boundingBox;
        }
        else if (sideIn == null) {
            sideIn = image.clone();
            boundingBox2 = boundingBox;
        }
    }

    public void processImages() {
        new ProcessImageTask().execute();
    }

    private void saveImages() {


        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File dir = new File(Environment.getExternalStorageDirectory() + "/Carby/" + timeStamp);
        if(!dir.exists())
            dir.mkdirs();

        File top = new File(dir, "top.png");
        File side = new File(dir, "side.png");
        File topReference = new File(dir, "topRef.png");
        File sideReference =  new File(dir, "sideRef.png");

        FileOutputStream fOut;
        try {
            if(topDownOut != null) {
                fOut = new FileOutputStream(top);
                topDownOut.compress(Bitmap.CompressFormat.PNG, 100, fOut);//PNG does not compress as it is a lossless format
                fOut.flush();
            }

            if(sideOut != null) {
                fOut = new FileOutputStream(side);
                sideOut.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
            }

            if(refObj1 != null) {
                fOut = new FileOutputStream(topReference);
                refObj1.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
            }

            if(refObj2 != null) {
                fOut = new FileOutputStream(sideReference);
                refObj2.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class ProcessImageTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog = new ProgressDialog(context);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            runVolumeCapture();

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            showResults();
        }

        public void runVolumeCapture() {
            // Do grab cut
            // Feature matching
            // ...

            AsyncTask grabCutTop = new GrabCutTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, topDownIn, boundingBox1);
            AsyncTask grabCutSide = new GrabCutTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sideIn, boundingBox2);
            AsyncTask refDetectTop = new FindPoundTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, topDownIn);
            AsyncTask refDetectSide = new FindPoundTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sideIn);

            try {
                FindPoundTask.Result r1 = (FindPoundTask.Result) refDetectTop.get();
                FindPoundTask.Result r2 = (FindPoundTask.Result) refDetectSide.get();

                topDownOut = algorithms.matToBitmap((Mat) grabCutTop.get());
                sideOut = algorithms.matToBitmap((Mat) grabCutSide.get());
                refObj1 = algorithms.matToBitmap(r1.refObject);
                refObj2 = algorithms.matToBitmap(r2.refObject);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


        public void showResults() {
            Intent results = new Intent(context, ResultsActivity.class);

            File out1 = new File(context.getCacheDir(), "1.png");
            File out2 = new File(context.getCacheDir(), "2.png");

            //saveImages();

            try {
                FileOutputStream fOut;

                if(topDownOut != null) {
                    fOut = new FileOutputStream(out1);
                    topDownOut.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                }

                if(sideOut != null) {
                    fOut = new FileOutputStream(out2);
                    sideOut.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            results.putExtra("image1", out1.getAbsolutePath());
            results.putExtra("image2", out2.getAbsolutePath());

            context.startActivity(results);
        }

    }
}
