package com.grouph.ces.carby.volume_estimation.DevMode;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.grouph.ces.carby.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Peev on 25.03.2018 г..
 * Version: 0.3
 */

public class ShowFramesActivity extends AppCompatActivity {
    private List<RecordFrame> rfs;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dev_img_selector);
        getRecordFrames();

        GridView gridview = findViewById(R.id.gridview);
        for(RecordFrame rf:rfs){
            Log.d(this.getClass().getName(),"rf:"+rf.getFileName());
            if(rf.getBoundingBox()==null) Log.d(this.getClass().getName(),"bounding box null");
            if(rf.getFrame()==null) Log.d(this.getClass().getName(),"mat null");
        }
        gridview.setAdapter(new ImageGridAdapter(this));

        gridview.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> {
            Log.d(this.getClass().getName(), "Image "+rfs.get(position).getFileName()+" at " + position);
        });
    }

    private void getRecordFrames() {
        rfs = new ArrayList<>();
        SharedPreferences preferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);
        for(String name: RecordFrame.recordedFrameNames(preferences)){
            Log.d(this.getClass().getName(),"load "+name);
            rfs.add(new RecordFrame(preferences,name));
        }
    }

    class ImageGridAdapter extends BaseAdapter {
        private AppCompatActivity superActivity;
        private int width;
        private int height;

        public ImageGridAdapter(AppCompatActivity superActivity){
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
                Log.d(this.getClass().getName(),"convertView is null");
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
}
