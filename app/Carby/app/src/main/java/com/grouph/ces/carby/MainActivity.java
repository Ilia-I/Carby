package com.grouph.ces.carby;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.grouph.ces.carby.barcodescanner.MainBarcodeActivity;
import com.grouph.ces.carby.camera_calibration.CameraCalibrationActivity;
import com.grouph.ces.carby.ocr.OcrCaptureActivity;
import com.grouph.ces.carby.volume_estimation.VolumeCaptureActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        CardView barcodeCard = findViewById(R.id.barcode_card);
        CardView ocrCard = findViewById(R.id.ocr_card);
        CardView volumeCard = findViewById(R.id.volume_card);
        CardView calibrationCard = findViewById(R.id.calibration_card);

        barcodeCard.setOnClickListener(this);
        ocrCard.setOnClickListener(this);
        volumeCard.setOnClickListener(this);
        calibrationCard.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), "Settings not implemented", Snackbar.LENGTH_LONG);
            snackbar.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_barcode) {
            Intent myIntent1 = new Intent(this, MainBarcodeActivity.class);
            startActivity(myIntent1);
            // Handle the camera action
        } else if (id == R.id.nav_ocr) {
            Intent ocr = new Intent(this, OcrCaptureActivity.class);
            startActivity(ocr);
        } else if (id == R.id.nav_volume) {
            Intent volume = new Intent(this, VolumeCaptureActivity.class);
            startActivity(volume);
        } else if (id == R.id.nav_calibration) {
            Intent camCal = new Intent(this, CameraCalibrationActivity.class);
            startActivity(camCal);
        } /*else if (id == R.id.nav_feature) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.barcode_card) {
            Intent myIntent1 = new Intent(this, MainBarcodeActivity.class);
            startActivity(myIntent1);
            // Handle the camera action
        } else if (id == R.id.ocr_card) {
            Intent ocr = new Intent(this, OcrCaptureActivity.class);
            startActivity(ocr);
        } else if (id == R.id.volume_card) {
            Intent volume = new Intent(this, VolumeCaptureActivity.class);
            startActivity(volume);
        } else if (id == R.id.calibration_card) {
            Intent camCal = new Intent(this, CameraCalibrationActivity.class);
            startActivity(camCal);
        }
    }
}
