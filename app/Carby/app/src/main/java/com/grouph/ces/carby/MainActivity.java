package com.grouph.ces.carby;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.grouph.ces.carby.barcodescanner.MainBarcodeActivity;
import com.grouph.ces.carby.camera_calibration.CameraCalibrationActivity;
import com.grouph.ces.carby.ocr.OcrCaptureActivity;
import com.grouph.ces.carby.volume_estimation.CaptureActivity;

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

        View mBarcodeTile = findViewById(R.id.colored_bar1);
        View mOcrTile = findViewById(R.id.colored_bar2);
        View mVolumeTile = findViewById(R.id.colored_bar3);

        mBarcodeTile.setOnClickListener(this);
        mOcrTile.setOnClickListener(this);
        mVolumeTile.setOnClickListener(this);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_calibration) {
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

        if (id == R.id.colored_bar1) {
            Intent myIntent1 = new Intent(this, MainBarcodeActivity.class);
            startActivity(myIntent1);
            // Handle the camera action
        } else if (id == R.id.colored_bar2) {
            Intent ocr = new Intent(this, OcrCaptureActivity.class);
            startActivity(ocr);
        } else if (id == R.id.colored_bar3) {
            Intent volume = new Intent(this, CaptureActivity.class);
            startActivity(volume);
        }
    }
}
