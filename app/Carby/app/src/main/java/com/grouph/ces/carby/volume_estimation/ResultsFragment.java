package com.grouph.ces.carby.volume_estimation;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.grouph.ces.carby.R;

import java.io.File;

/**
 * Created by matthewball on 13/03/2018.
 */

public class ResultsFragment extends Fragment {

    private ImageView iv1;
    private ImageView iv2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.vol_results, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        iv1 = getView().findViewById(R.id.imageView1);
        iv2 = getView().findViewById(R.id.imageView2);

        displayImages();
    }

    private void displayImages() {
        Bundle bundle = getArguments();
        if(bundle!=null) {
            String out1path = bundle.getString("image1");
            String out2path = bundle.getString("image2");

            if (out1path != null) {
                File imgFile1 = new File(out1path);
                Bitmap myBitmap1 = BitmapFactory.decodeFile(imgFile1.getAbsolutePath());
                Bitmap scaledPicture = Bitmap.createScaledBitmap(myBitmap1, myBitmap1.getWidth() * 2, myBitmap1.getHeight() * 2, false);
                iv1.setImageBitmap(scaledPicture);
            }

            if (out2path != null) {
                File imgFile2 = new File(out2path);
                Bitmap myBitmap2 = BitmapFactory.decodeFile(imgFile2.getAbsolutePath());
                Bitmap scaledPicture = Bitmap.createScaledBitmap(myBitmap2, myBitmap2.getWidth() * 2, myBitmap2.getHeight() * 2, false);
                iv2.setImageBitmap(scaledPicture);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// Press Back Icon
            ((VolEstActivity) getActivity()).setFragmentCapture(new Bundle());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
