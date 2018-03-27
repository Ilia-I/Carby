package com.grouph.ces.carby.volume_estimation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.grouph.ces.carby.volume_estimation.DevMode.RecordFrame;
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

    private Frame topDown;
    private Frame side;

    private Bitmap topDownOut;
    private Bitmap sideOut;

    private ProcessingAlgorithms algorithms;

    public ImageProcessor(Context context) {
        this.context = context;
        this.algorithms = new ProcessingAlgorithms(context);
    }

    public void addImage(Frame frame) {
        if(topDown == null)
            topDown = frame;
        else if (side == null) {
            side = frame;
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
            SharedPreferences preferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);

            AsyncTask grabCutTop = new GrabCutTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, topDown.getImage(), topDown.getBoundingBox());
            AsyncTask grabCutSide = new GrabCutTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, side.getImage(), side.getBoundingBox());

            try {
                topDownOut = algorithms.matToBitmap((Mat) grabCutTop.get());
                sideOut = algorithms.matToBitmap((Mat) grabCutSide.get());

                RecordFrame testTop = new RecordFrame("testTop",
                        new Frame((Mat) grabCutTop.get(), topDown.getPixelsPerCm(), topDown.getBoundingBox()));
                testTop.saveObj(preferences);

                RecordFrame testSide = new RecordFrame("testSide",
                        new Frame((Mat) grabCutSide.get(), side.getPixelsPerCm(), side.getBoundingBox()));
                testSide.saveObj(preferences);

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
