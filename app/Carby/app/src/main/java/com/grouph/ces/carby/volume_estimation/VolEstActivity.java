package com.grouph.ces.carby.volume_estimation;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.grouph.ces.carby.nutrition_data.NutritionResultActivity;
import com.grouph.ces.carby.volume_estimation.DevMode.ShowFramesFragment;

import org.opencv.android.OpenCVLoader;

/**
 * Created by Martin Peev on 27.03.2018 г..
 * Version: 0.6
 */

public class VolEstActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setFragment(new CaptureFragment(),new Bundle(),"");
//        performTestCalculation();
        NutritionInformationCalculator calc = new NutritionInformationCalculator(getApplicationContext(),100.0,NutritionInformationCalculator.FOOD_BREAD);
        Intent result = new Intent(getApplicationContext(), NutritionResultActivity.class);
        result.putExtra("jsonNutritionTable",calc.getNutritionTable().toJasonObject().toString());
        result.putExtra("id",0);
        getApplicationContext().startActivity(result);
    }

    public void setFragmentCapture(Bundle bundle){
        Log.d(this.getClass().getName(),"setFragmentCapture");
        setFragment(new CaptureFragment(),bundle, "capture_fragment");
    }

    public void setFragmentShowFrames(Bundle bundle){
        Log.d(this.getClass().getName(),"setFragmentShowFrames");
        setFragment(new ShowFramesFragment(),bundle, "show_frames_fragment");
    }

    public void setFragmentResults(Bundle bundle){
        Log.d(this.getClass().getName(),"setFragmentResults");
        setFragment(new ResultsFragment(),bundle, "results_fragment");
    }

    private void setFragment(Fragment fragment, Bundle bundle, String backstackKey){
        FragmentManager fm = getFragmentManager();
        if (fm != null) {
            fragment.setArguments(bundle);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(android.R.id.content, fragment);
            if(!backstackKey.isEmpty())ft.addToBackStack(backstackKey);
            ft.commit();
        }
    }

    public void performTestCalculation() {
        if (!OpenCVLoader.initDebug()) {}
        IntegralApproximation a = new IntegralApproximation(this);
        if(a.loadTestMats()) {
            a.performApproximation();
            a.showResults();
        }
    }
}
