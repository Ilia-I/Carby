package com.grouph.ces.carby.volume_estimation;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.volume_estimation.DevMode.OnSwipeListener;
import com.grouph.ces.carby.volume_estimation.DevMode.RecordFrame;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by matthewball on 13/03/2018.
 */

public class ResultsFragment extends Fragment {
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            IMAGE_SET_ORIGINAL,
            IMAGE_SET_MASK
    })
    public @interface ImageSet {}
    public static final String IMAGE_SET_ORIGINAL = "original_";
    public static final String IMAGE_SET_MASK = "mask_";

    private List<String> imgset;
    private int current;
    private String original1;
    private String original2;

    private SharedPreferences preferences;
    private TextView tv;
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
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        tv = getView().findViewById(R.id.img_type);
        iv1 = getView().findViewById(R.id.imageView1);
        iv2 = getView().findViewById(R.id.imageView2);

        ConstraintLayout cl = getView().findViewById(R.id.result_layout);
        cl.setOnTouchListener(new OnSwipeListener(getActivity()){
            public void onSwipeRight() {
                previousImgSet();
            }
            public void onSwipeLeft() {
                nextImgSet();
            }

        });

        imgset = new ArrayList<>();
        imgset.add(IMAGE_SET_ORIGINAL);
        imgset.add(IMAGE_SET_MASK);
        current=0;
        getOriginals();
        showImages(imgset.get(0));
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

    private void getOriginals(){
        List<String> rfs = RecordFrame.recordedFrameNames(preferences);
        Collections.sort(rfs, (String o1, String o2) -> o1.compareTo(o2));
        original1 = rfs.get(rfs.size()-2);
        original2 = rfs.get(rfs.size()-1);
    }

    private void nextImgSet(){
        current = (current+1)%imgset.size();
        showImages(imgset.get(current));
    }

    private void previousImgSet(){
        current--;
        if(current<0) current = imgset.size()-1;
        showImages(imgset.get(current));
    }

    private void showImages(@ImageSet String set){
        tv.setText(set.substring(0,set.length()-1).toUpperCase());
        switch (set){
            case IMAGE_SET_ORIGINAL:
                iv1.setImageBitmap(new RecordFrame(preferences,original1).getImage());
                iv2.setImageBitmap(new RecordFrame(preferences,original2).getImage());
                break;
            case IMAGE_SET_MASK:
                iv1.setImageBitmap(new RecordFrame(preferences,set+1).getImage());
                iv2.setImageBitmap(new RecordFrame(preferences,set+2).getImage());
                break;
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
