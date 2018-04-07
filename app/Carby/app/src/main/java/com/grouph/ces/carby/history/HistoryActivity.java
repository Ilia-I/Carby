package com.grouph.ces.carby.history;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.grouph.ces.carby.volume_estimation.ResultsFragment;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFragment(new HistoryMasterFragment(), savedInstanceState, "");
    }

    public void setFragmentHistoryMaster(Bundle bundle){
        Log.d(this.getClass().getName(),"setFragmentMaster");
        setFragment(new HistoryMasterFragment(), bundle, "master_fragment");
    }

    public void setFragmentHistoryDetail(Bundle bundle){
        Log.d(this.getClass().getName(),"setFragmentDetail");
        setFragment(new HistoryDetailFragment(), bundle, "detail_fragment");
    }

    private void setFragment(Fragment fragment, Bundle bundle, String backstackKey){
        FragmentManager fm = getFragmentManager();
        if (fm != null) {
            fragment.setArguments(bundle);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(android.R.id.content, fragment);
            if(!backstackKey.isEmpty())ft.addToBackStack(backstackKey);
            ft.commit();
            getSupportActionBar().show();
        }
    }

}
