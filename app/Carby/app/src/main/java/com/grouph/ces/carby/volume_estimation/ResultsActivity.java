package com.grouph.ces.carby.volume_estimation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.grouph.ces.carby.R;

import java.io.File;

/**
 * Created by matthewball on 13/03/2018.
 */

public class ResultsActivity extends AppCompatActivity {

    private ImageView iv1;
    private ImageView iv2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vol_results);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        iv1 = findViewById(R.id.imageView1);
        iv2 = findViewById(R.id.imageView2);

        displayImages();
    }

    private void displayImages() {
        Intent i = getIntent();
        String out1path = i.getStringExtra("image1");
        String out2path = i.getStringExtra("image2");

        if(out1path != null) {
            File imgFile1 = new File(out1path);
            Bitmap myBitmap1 = BitmapFactory.decodeFile(imgFile1.getAbsolutePath());
            iv1.setImageBitmap(myBitmap1);
        }

        if(out1path != null) {
            File imgFile2 = new File(out2path);
            Bitmap myBitmap2 = BitmapFactory.decodeFile(imgFile2.getAbsolutePath());
            iv2.setImageBitmap(myBitmap2);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
            finish();

        return super.onOptionsItemSelected(item);
    }
}
