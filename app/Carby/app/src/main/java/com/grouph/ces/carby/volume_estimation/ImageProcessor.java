package com.grouph.ces.carby.volume_estimation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by matthewball on 13/03/2018.
 */

public class ImageProcessor {

    private Context context;

    private Bitmap input1;
    private Bitmap input2;
    private org.opencv.core.Rect boundingBox1;
    private org.opencv.core.Rect boundingBox2;
    private Bitmap output1;
    private Bitmap output2;

    private ProcessingAlgorithms algorithms;

    public ImageProcessor(Context context) {
        this.context = context;
        this.algorithms = new ProcessingAlgorithms(context);
    }

    public void addImage(Bitmap image, org.opencv.core.Rect boundingBox) {
        if(input1 == null) {
            input1 = image;
            boundingBox1 = boundingBox;
        }
        else if (input2 == null) {
            input2 = image;
            boundingBox2 = boundingBox;
        }
    }

    public void reset() {
        input1 = null;
        input2 = null;
    }

    public void processImages() {
        new ProcessImageTask().execute();
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
            if(input1 != null)
                output1 = algorithms.performGrabCut(input1, boundingBox1);

            if(input2 != null)
                output2 = algorithms.performGrabCut(input2, boundingBox2);
        }

        public void showResults() {
            Intent results = new Intent(context, ResultsActivity.class);

            File out1 = new File(context.getCacheDir(), "1.png");
            File out2 = new File(context.getCacheDir(), "2.png");

            try {
                FileOutputStream fOut;

                if(output1 != null) {
                    fOut = new FileOutputStream(out1);
                    output1.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                }

                if(output2 != null) {
                    fOut = new FileOutputStream(out2);
                    output2.compress(Bitmap.CompressFormat.PNG, 100, fOut);
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
