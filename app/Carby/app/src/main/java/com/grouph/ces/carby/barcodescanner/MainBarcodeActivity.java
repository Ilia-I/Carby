/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grouph.ces.carby.barcodescanner;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.grouph.ces.carby.R;
import com.grouph.ces.carby.database.AppDatabase;
import com.grouph.ces.carby.database.NutritionDataDB;
import com.grouph.ces.carby.nutrition_data.INutritionTable;
import com.grouph.ces.carby.ocr.OcrCaptureActivity;

import java.util.concurrent.ExecutionException;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainBarcodeActivity extends AppCompatActivity implements View.OnClickListener {

    // use a compound button so either checkbox or switch widgets work.
    private TextView barcodeHeader;
    private TextView barcodeValue;
    private ProgressBar progressBar;
    private TextView productResult;
    private AppDatabase db;


    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_main);

        barcodeHeader = (TextView)findViewById(R.id.barcode_header);
        barcodeValue = (TextView)findViewById(R.id.barcode_value);

        progressBar = findViewById(R.id.load_json_bar);
        productResult = findViewById(R.id.product_result);

        findViewById(R.id.read_barcode).setOnClickListener(this);

        //TODO async if slow
        db = Room.databaseBuilder(getApplicationContext() ,AppDatabase.class,"myDB").allowMainThreadQueries().build();

        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }

    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == RESULT_OK) {
            Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

            if (requestCode == RC_BARCODE_CAPTURE) {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        //barcodeHeader.setText(R.string.barcode_success);
                        barcodeValue.setText("Barcode: " + barcode.displayValue);
                        Log.d(TAG, "Barcode read: " + barcode.displayValue);
                        processBarcode(barcode);
                    } else {
                        barcodeHeader.setText(R.string.barcode_failure);
                        Log.d(TAG, "No barcode captured, intent data is null");
                    }
                } else {
                    barcodeHeader.setText(String.format(getString(R.string.barcode_error),
                            CommonStatusCodes.getStatusCodeString(resultCode)));
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }else{
            
        }
    }

    private void processBarcode(Barcode barcode) {
        if(barcode.valueFormat==Barcode.PRODUCT){
            Log.d(this.getClass().getName(),"Barcode: " + barcode.displayValue);
            INutritionTable result = null;

            //1. check local database
            NutritionDataDB data = db.nutritionDataDao().findByBarcode(barcode.displayValue);
            if(data!=null) {
                result = data.getNt();
                if (result != null) {
                    Log.d("OcrDetectorProcessor", "Loaded Table:\n" + result);
                    //TODO display INutritionTable
                    barcodeHeader.setText(R.string.barcode_found_localDB);
                    productResult.setText(result.toString());
                    productResult.setVisibility(View.VISIBLE);
                    return;
                }
            }

            //TODO 2. check open food facts database (get info about result and set result variable)
            BarcodeLookup barcodeLookup = new BarcodeLookup();
            try {
                result = barcodeLookup.execute(barcode).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(result!=null){
                Log.e("", result.toString());
                productResult.setText(result.toString());
                productResult.setVisibility(View.VISIBLE);
                return;
            }

            //3. prompt user to scan nutrition table
            startOCR(barcode.displayValue);
            return;
        }
    }

    private void startOCR(String barcode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.dialog_message_check_barcode);
        builder.setTitle(R.string.dialog_title_check_barcode);

        builder.setPositiveButton(R.string.ok, (DialogInterface dialog, int id) -> {
            Intent i = new Intent(getApplicationContext(), OcrCaptureActivity.class);
            i.putExtra(getResources().getString(R.string.ocr_intent_barcode), barcode);
            startActivity(i);
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int id) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
