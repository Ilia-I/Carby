package com.grouph.ces.carby.nutrition_data;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.grouph.ces.carby.R;

import org.json.JSONException;
import org.json.JSONObject;

public class NutritionResultActivity extends AppCompatActivity {

    Bundle extras;
    INutritionTable nutritionTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nutrition_result);
        extras = getIntent().getExtras();
        nutritionTable = new NutritionTable();

        if(extras!=null){
            try {
                JSONObject jsonNutritionTable = new JSONObject(extras.getString("jsonNutritionTable"));
                nutritionTable.setAll(jsonNutritionTable);
            } catch (JSONException e){
                Log.e("Error", "JSONException in NutritionResultActivity");
            }
        }

        TextView mCarbVal = findViewById(R.id.carb_val);
        TextView mCarbSugarsVal = findViewById(R.id.carb_sugars_val);

        mCarbVal.setText(Double.toString(nutritionTable.getCarbohydrates().getTotal()) + nutritionTable.getCarbohydratesUnit());
        mCarbSugarsVal.setText(Double.toString(nutritionTable.getCarbohydrates().getContentOf("sugars")) + nutritionTable.getCarbohydratesUnit());
    }
}
