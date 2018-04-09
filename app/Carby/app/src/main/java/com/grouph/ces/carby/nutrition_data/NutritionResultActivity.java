package com.grouph.ces.carby.nutrition_data;

import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.database.AppDatabase;
import com.grouph.ces.carby.database.ConsumptionDB;
import com.grouph.ces.carby.volume_estimation.DevMode.OnSwipeListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NutritionResultActivity extends AppCompatActivity {

    private Bundle extras;
    private INutritionTable nutritionTable;
    private List<RadioButton> rBtns;
    private Double mass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nutrition_result);
        extras = getIntent().getExtras();
        nutritionTable = new NutritionTable();

        if (extras.getBoolean("per100g", true)) {
            initConsumption();
        } else {
            TextView per = findViewById(R.id.per);
            mass = extras.getDouble("mass");
            per.setText("Total weight " + formatDouble(mass) + "g");
            initAlternateConsumption();
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.key_dev_mode), false)) {
                setupSwipeListener();
            }
        }

        if (extras.getInt("id", -1) < 0) {
            hideConsumption();
        }

        if (extras != null) {
            try {
                JSONObject jsonNutritionTable = new JSONObject(extras.getString("jsonNutritionTable"));
                nutritionTable.setAll(jsonNutritionTable);
            } catch (JSONException e) {
                Log.e("Error", "JSONException in NutritionResultActivity");
            }
        }

        setValues();
    }

    private void setValues(){
        if(nutritionTable != null) {
            TextView mEnergyVal = findViewById(R.id.energy_val);
            TextView mFatVal = findViewById(R.id.fat_val);
            TextView mMonoUnsat = findViewById(R.id.monounsaturated_val);
            TextView mPolyUnsat = findViewById(R.id.polyunsaturated_val);
            TextView mSaturatedVal = findViewById(R.id.saturated_val);
            TextView mCarbVal = findViewById(R.id.carb_val);
            TextView mCarbSugarsVal = findViewById(R.id.carb_sugars_val);
            TextView mCarbStarchVal = findViewById(R.id.carb_starch_val);
            TextView mCarbPolyolsVal = findViewById(R.id.carb_starch_val);
            TextView mFibreVal = findViewById(R.id.fibre_val);
            TextView mProteinVal = findViewById(R.id.protein_val);
            TextView mSaltVal = findViewById(R.id.salt_val);

            setComponent(mEnergyVal,"Energy",nutritionTable.getEnergyUnit());

            setComponent(mFatVal,"Fat",nutritionTable.getFatsUnit());
            setComponent(mMonoUnsat,"mono-unsaturates",nutritionTable.getFatsUnit());
            setComponent(mPolyUnsat,"polyunsaturates",nutritionTable.getFatsUnit());
            setComponent(mSaturatedVal,"saturates",nutritionTable.getFatsUnit());

            setComponent(mCarbVal,"Carbohydrate",nutritionTable.getCarbohydratesUnit());
            setComponent(mCarbSugarsVal,"sugars",nutritionTable.getCarbohydratesUnit());
            setComponent(mCarbPolyolsVal,"polyols",nutritionTable.getCarbohydratesUnit());
            setComponent(mCarbStarchVal,"starch",nutritionTable.getCarbohydratesUnit());

            setComponent(mFibreVal,"Fibre",nutritionTable.getFibreUnit());
            setComponent(mProteinVal,"Protein",nutritionTable.getProteinUnit());
            setComponent(mSaltVal,"Salt",nutritionTable.getSaltUnit());
        }
    }

    private void setComponent(TextView textView, String componentName, @INutritionTable.MeasurementUnit String unit){
        if(nutritionTable.getComponentValue(componentName) !=null){
            textView.setText(formatDouble(nutritionTable.getComponentValue(componentName)) + unit);
        }
    }

    private void hideConsumption() {
        findViewById(R.id.radioGroup).setVisibility(View.GONE);
        findViewById(R.id.add_all_btn).setVisibility(View.GONE);
        findViewById(R.id.consumeLabel).setVisibility(View.GONE);
    }

    private void setupSwipeListener() {
        findViewById(R.id.nutrition_result_layout).setOnTouchListener(new OnSwipeListener(this){
            public void onSwipeRight() {
                onBackPressed();
            }
        });
    }

    private String formatDouble(double val){
        return String.format("%.1f", val);
    }

    private void initAlternateConsumption(){
        findViewById(R.id.radioGroup).setVisibility(View.GONE);
        Button addAll = findViewById(R.id.add_all_btn);
        addAll.setVisibility(View.VISIBLE);
        addAll.setOnClickListener((View v) -> {
            if(extras!=null) {
                int id = extras.getInt("id", -1);
                if (id >= 0) {
                    AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "myDB").allowMainThreadQueries().build();
                    db.consumptionDataDao().insert(new ConsumptionDB(id, mass));
                    Toast.makeText(getApplicationContext(), String.format(Locale.ENGLISH, "Added %.1fg consumption", mass), Toast.LENGTH_SHORT).show();
                    addAll.setEnabled(false);
                }
            }
        });
    }

    private void initConsumption() {
        rBtns = new ArrayList<>();
        rBtns.add(findViewById(R.id.radioButton100));
        rBtns.add(findViewById(R.id.radioButton150));
        rBtns.add(findViewById(R.id.radioButton200));
        rBtns.add(findViewById(R.id.radioButton250));
        rBtns.add(findViewById(R.id.radioButton300));
        rBtns.add(findViewById(R.id.radioButton400));
        rBtns.add(findViewById(R.id.radioButton500));
        rBtns.add(findViewById(R.id.radioButton600));
        rBtns.add(findViewById(R.id.radioButtonCustom));
        for(RadioButton rb:rBtns){
            rb.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> check(rb));
        }

        Button addConsumed = findViewById(R.id.add_btn);
        addConsumed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(this.getClass().getName(),"add");
                if(extras!=null){
                    int id = extras.getInt("id",-1);
                    if(id>=0){
                        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,"myDB").allowMainThreadQueries().build();
                        int quantity = getQuantity();
                        if(quantity>0) {
                            Log.d(this.getClass().getName(),"id:"+id+"\nquantity:"+quantity+"\nnt:"+db.nutritionDataDao().findByID(id));
                            db.consumptionDataDao().insert(new ConsumptionDB(id, quantity));
                            Toast.makeText(getApplicationContext(), "Added "+quantity+"g consumption!", Toast.LENGTH_SHORT).show();
                            addConsumed.setEnabled(false);
                        }
                    }
                } else {
                    Log.d(this.getClass().getName(),"no valid id");
                    addConsumed.setEnabled(false);
                }
            }
        });
    }

    private int getQuantity(){
        for(RadioButton rb: rBtns){
            if(rb.isChecked()){
                String content = rb.getText().toString().replaceAll("[^\\d.]", "");
                if(content.length()>0) {
                    return Integer.parseInt(content);
                } else {
                    try {
                        return Integer.parseInt(((EditText) findViewById(R.id.userValue)).getText().toString());
                    } catch (NumberFormatException e){
                        Toast.makeText(this, "Invalid number!", Toast.LENGTH_SHORT).show();
                        return 0;
                    }
                }
            }
        }
        return 0;
    }

    private void check(RadioButton rb) {
        if(rb.isChecked()) {
            Log.d(this.getClass().getName(), "check:" + rb.getText());
            for (RadioButton r : rBtns) {
                if (!r.equals(rb)) {
                    r.setChecked(false);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
