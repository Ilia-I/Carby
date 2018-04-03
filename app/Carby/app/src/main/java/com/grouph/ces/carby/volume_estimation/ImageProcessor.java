package com.grouph.ces.carby.volume_estimation;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.grouph.ces.carby.volume_estimation.DevMode.RecordFrame;
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

    private VolEstActivity activity;

    private Frame topDown;
    private Frame side;

    public ImageProcessor(VolEstActivity activity) {
        this.activity = activity;
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

    private void saveImage(Mat image1, String name) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File dir = new File(Environment.getExternalStorageDirectory() + "/Carby/" + timeStamp);
        if(!dir.exists())
            dir.mkdirs();

        File top = new File(dir, name);
        FileOutputStream fOut;

        Bitmap bitmap = ProcessingAlgorithms.matToBitmap(image1, image1.width(), image1.height());
        try {
            fOut = new FileOutputStream(top);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class ProcessImageTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog = new ProgressDialog(activity);
        private SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        private IntegralApproximation approximator;
        private NutritionInformationCalculator calculator;

        private Mat grabCutTopMat, grabCutSideMat;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AsyncTask<Object, Void, Mat> grabCutTop = new GrabCutTask();
            AsyncTask<Object, Void, Mat> grabCutSide = new GrabCutTask();

            ProcessingAlgorithms pa = new ProcessingAlgorithms(activity);
            pa.undistort(topDown.getImage());
            pa.undistort(side.getImage());

            grabCutTop.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, topDown.getImage(), topDown.getBoundingBox());
            grabCutSide.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, side.getImage(), side.getBoundingBox());

            try {
                grabCutTopMat = grabCutTop.get();
                grabCutSideMat = grabCutSide.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            Frame topFrame = new Frame(grabCutTopMat, topDown.getReferenceObjectSize(), topDown.getBoundingBox());
            Frame sideFrame = new Frame(grabCutSideMat, side.getReferenceObjectSize(), side.getBoundingBox());
            approximator = new IntegralApproximation(activity, topFrame, sideFrame);
            double volume = approximator.getApproximation();

            if(volume == -1.0) {
                return null;
            }

            calculator = new NutritionInformationCalculator(activity, volume, NutritionInformationCalculator.FOOD_BREAD);

            //TODO implement types
            RecordFrame testTop = new RecordFrame("testTop", topFrame);
            testTop.saveObj(preferences);
            RecordFrame testSide = new RecordFrame("testSide", sideFrame);
            testSide.saveObj(preferences);

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
//            approximator.showResults();
            this.showResults();

            if(calculator != null)
                calculator.show();
            else
                Toast.makeText(activity, "Failed to detect food object",Toast.LENGTH_LONG).show();
        }


        public void showResults() {
            File out1 = new File(activity.getCacheDir(), "1.png");
            File out2 = new File(activity.getCacheDir(), "2.png");

            try {
                FileOutputStream fOut;

                Bitmap bitmap1, bitmap2;
                bitmap1 = ProcessingAlgorithms.matToBitmap(grabCutTopMat, grabCutTopMat.width(), grabCutTopMat.height());
                bitmap2 = ProcessingAlgorithms.matToBitmap(grabCutSideMat, grabCutSideMat.width(), grabCutSideMat.height());

                if(bitmap1 != null) {
                    fOut = new FileOutputStream(out1);
                    bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                }

                if(bitmap2 != null) {
                    fOut = new FileOutputStream(out2);
                    bitmap2.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bundle bundle = new Bundle();
            bundle.putString("image1", out1.getAbsolutePath());
            bundle.putString("image2", out2.getAbsolutePath());
            activity.setFragmentResults(bundle);
        }

    }
}
