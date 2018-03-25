package com.grouph.ces.carby.volume_estimation.DevMode;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.preferences.SettingsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Martin Peev on 25.03.2018 г..
 * Version: 0.5
 */

public class ShowFramesActivity extends AppCompatActivity {
    private List<RecordFrame> rfs;
    private List<Integer> selected;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dev_img_selector);
        getRecordFrames();
        selected = new ArrayList<>();

        GridView gridview = findViewById(R.id.gridview);
        for(RecordFrame rf:rfs){
            Log.d(this.getClass().getName(),"rf:"+rf.getFileName());
            if(rf.getBoundingBox()==null) Log.d(this.getClass().getName(),"bounding box null");
            if(rf.getFrame()==null) Log.d(this.getClass().getName(),"mat null");
        }
        gridview.setAdapter(new ImageGridAdapter(this));

        gridview.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> mark(position));
    }

    private void mark(int position) {
        Log.d(this.getClass().getName(), "Mark image "+rfs.get(position).getFileName()+" at " + position);
        if(selected.contains(position)){
            selected.remove(new Integer(position));
        } else if(selected.size()>=2){
            Toast.makeText(this, "Maximum number of images marked", Toast.LENGTH_SHORT).show();
        } else {
            selected.add(position);
        }

        for(Integer i:selected) Log.d(this.getClass().getName(),"selected:"+i);
    }

    private void getRecordFrames() {
        rfs = new ArrayList<>();
        SharedPreferences preferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);
        for(String name: RecordFrame.recordedFrameNames(preferences)){
            Log.d(this.getClass().getName(),"load "+name);
            rfs.add(new RecordFrame(preferences,name));
        }

        Collections.sort(rfs, (Object softDrinkOne, Object softDrinkTwo) ->
                ((RecordFrame)softDrinkOne).getFileName().compareTo(((RecordFrame)softDrinkTwo).getFileName()));
    }

    class ImageGridAdapter extends BaseAdapter {
        private AppCompatActivity superActivity;
        private int width;
        private int height;

        private ImageGridAdapter(AppCompatActivity superActivity){
            super();
            this.superActivity = superActivity;
            double div = 2.5;
            this.width = (int) (1280/div);
            this.height = (int) (720/div);
            Log.d(this.getClass().getName(),"init success");
        }

        @Override
        public int getCount() {
            return rfs.size();
        }

        @Override
        public Object getItem(int position) {
            return rfs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(superActivity);
                imageView.setLayoutParams(new GridView.LayoutParams(width,height));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                int pad = 10;
                imageView.setPadding(pad, pad, pad, pad);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageBitmap(rfs.get(position).getFrame());
            return imageView;
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_accept:
                if(selected.size()==2) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(getResources().getString(R.string.rf1), rfs.get(selected.get(0)).getFileName());
                    returnIntent.putExtra(getResources().getString(R.string.rf2), rfs.get(selected.get(1)).getFileName());
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    Toast.makeText(this, "Please select two images!", Toast.LENGTH_SHORT).show();
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.img_selector, menu);
        return true;
    }
}
