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
            Bitmap scaledPicture = Bitmap.createScaledBitmap(myBitmap1, myBitmap1.getWidth()*2, myBitmap1.getHeight()*2, false);
            iv1.setImageBitmap(scaledPicture);
        }

        if(out2path != null) {
            File imgFile2 = new File(out2path);
            Bitmap myBitmap2 = BitmapFactory.decodeFile(imgFile2.getAbsolutePath());
            Bitmap scaledPicture = Bitmap.createScaledBitmap(myBitmap2, myBitmap2.getWidth()*2, myBitmap2.getHeight()*2, false);
            iv2.setImageBitmap(scaledPicture);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
            finish();

        return super.onOptionsItemSelected(item);
    }
}
