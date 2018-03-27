package com.grouph.ces.carby.volume_estimation.DevMode;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
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
 * Version: 0.8
 */

public class ShowFramesActivity extends AppCompatActivity {
    private List<RecordFrame> rfs;
    private List<Bitmap> images;
    private List<Integer> selected;
    private ImageGridAdapter iga;

    private final double downscaleFactor = 2.5;
    private final int bitmapHeight = (int) (720/downscaleFactor);
    private final int bitmapWidth = (int) (1280/downscaleFactor);

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dev_img_selector);
        getRecordFrames();
        decodeImages();
        selected = new ArrayList<>();

        GridView gridview = findViewById(R.id.gridview);
        iga = new ImageGridAdapter(this);
        gridview.setAdapter(iga);
        gridview.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> mark(position));
    }

    private void mark(int position) {
        Log.d(this.getClass().getName(), "Mark image "+rfs.get(position).getFileName()+" at " + position);
        int idx = selected.indexOf(Integer.valueOf(position));
        if(idx>=0){
            selected.remove(idx);
        } else if(selected.size()>=2){
            Toast.makeText(this, "Maximum number of images marked", Toast.LENGTH_SHORT).show();
        } else {
            selected.add(position);
        }
        iga.notifyDataSetInvalidated();
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

    private void decodeImages(){
        images = new ArrayList<>();
        for(RecordFrame rf: rfs){
            images.add(Bitmap.createScaledBitmap ( rf.getFrame(), bitmapWidth, bitmapHeight, true));
        }
    }

    class ImageGridAdapter extends BaseAdapter {
        private AppCompatActivity superActivity;
//        private Bitmap markedOverlay;

        private ImageGridAdapter(AppCompatActivity superActivity){
            super();
            this.superActivity = superActivity;
//            markedOverlay = makeTransparent(BitmapFactory.decodeResource(superActivity.getResources(),R.drawable.img_overlay),170);
            Log.d(this.getClass().getName(),"init success");
        }

        public Bitmap makeTransparent(Bitmap src, int value) {
//            int width = src.getWidth();
//            int height = src.getHeight();
            Bitmap transBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(transBitmap);
            canvas.drawARGB(0, 0, 0, 0);
            // config paint
            final Paint paint = new Paint();
            paint.setAlpha(value);
            canvas.drawBitmap(src, 0, 0, paint);
            return transBitmap;
        }

        private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
            Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
            Canvas canvas = new Canvas(bmOverlay);
            canvas.drawBitmap(bmp1, new Matrix(), null);
            canvas.drawBitmap(bmp2, new Matrix(), null);
            return bmOverlay;
        }

        @Override
        public int getCount() {
            return images.size();
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
                imageView.setLayoutParams(new GridView.LayoutParams(bitmapWidth,bitmapHeight));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                int pad = 10;
                imageView.setPadding(pad, pad, pad, pad);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageBitmap(images.get(position));
            if(selected.contains(position)){
                imageView.setColorFilter( 0x6f000000, PorterDuff.Mode.SRC_OVER );
            } else {
                imageView.clearColorFilter();
            }
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

            case R.id.action_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage(R.string.dialog_message_delete_img);
                builder.setTitle(R.string.dialog_title_delete_img);

                builder.setPositiveButton(R.string.ok, (DialogInterface dialog, int id) -> {
                    Collections.sort(selected, (Integer o1, Integer o2) -> Integer.compare(o1,o2));
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    for(int k=selected.size()-1;k>=0;k--){
                        Integer i = selected.get(k);
                        rfs.get(i).delete(preferences);
                        rfs.remove(i.intValue());
                        images.remove(i.intValue());
                    }
                    selected = new ArrayList<>();
                    iga.notifyDataSetChanged();
                    dialog.dismiss();
                });
                builder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int id) -> {
                    dialog.dismiss();
                });

                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
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
