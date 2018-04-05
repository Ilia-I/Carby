package com.grouph.ces.carby.volume_estimation;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthewball on 13/03/2018.
 */

public class ResultsFragment extends Fragment {
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            IMAGE_SET_ORIGINAL,
            IMAGE_SET_MASK,
            IMAGE_SET_STRETCH
    })
    public @interface ImageSet {}
    public static final String IMAGE_SET_ORIGINAL = "original_";
    public static final String IMAGE_SET_MASK = "mask_";
    public static final String IMAGE_SET_STRETCH = "stretch_";

    private List<String> imgset;
    private int current;

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
        imgset.add(IMAGE_SET_STRETCH);
        current=0;
//        getOriginals();
        showImages(imgset.get(0));

        Toast.makeText(getActivity(), "Swipe to see results.", Toast.LENGTH_SHORT).show();
    }

    private void nextImgSet(){
        current++;
        if(current>=imgset.size()){
            Bundle bundle = getArguments();
            if(bundle!=null) {
                new NutritionInformationCalculator(getActivity(), bundle.getDouble("volume"),bundle.getInt("foodType")).show();
            } else {
                Toast.makeText(getActivity(), "Bundle not found!", Toast.LENGTH_SHORT).show();

            }
        }
        showImages(imgset.get(current));
    }

    private void previousImgSet(){
        current--;
        if(current<0) current = 0;// imgset.size()-1;
        showImages(imgset.get(current));
    }

    private void showImages(@ImageSet String set){
        tv.setText(set.substring(0,set.length()-1).toUpperCase());
        iv1.setImageBitmap(new RecordFrame(preferences,set+1).getImage());
        iv2.setImageBitmap(new RecordFrame(preferences,set+2).getImage());
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
