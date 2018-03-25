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

        if(nutritionTable != null) {
            TextView mEnergyVal = findViewById(R.id.energy_val);
            TextView mFatVal = findViewById(R.id.carb_val);
            TextView mSaturatesVal = findViewById(R.id.carb_sugars_val);
            TextView mCarbVal = findViewById(R.id.carb_val);
            TextView mCarbSugarsVal = findViewById(R.id.carb_sugars_val);
            TextView mCarbStarchVal = findViewById(R.id.carb_starch_val);
            TextView mCarbPolyolsVal = findViewById(R.id.carb_starch_val);
            TextView mFibreVal = findViewById(R.id.fibre_val);
            TextView mProteinVal = findViewById(R.id.protein_val);
            TextView mSaltVal = findViewById(R.id.salt_val);

            if(nutritionTable.getComponentValue("Energy") !=null){
                mEnergyVal.setText(Double.toString(nutritionTable.getComponentValue("Energy")) + nutritionTable.getEnergyUnit());
            }

            if(nutritionTable.getComponentValue("Fat") !=null){
                mFatVal.setText(Double.toString(nutritionTable.getComponentValue("Fat")) + nutritionTable.getFatsUnit());
            }

            if(nutritionTable.getComponentValue("saturates") !=null){
                mSaturatesVal.setText(Double.toString(nutritionTable.getComponentValue("saturates")) + nutritionTable.getFatsUnit());
            }

            if(nutritionTable.getComponentValue("Carbohydrate") !=null){
                mCarbVal.setText(Double.toString(nutritionTable.getComponentValue("Carbohydrate")) + nutritionTable.getCarbohydratesUnit());
            }

            if(nutritionTable.getComponentValue("sugars") !=null){
                mCarbSugarsVal.setText(Double.toString(nutritionTable.getComponentValue("sugars")) + nutritionTable.getCarbohydratesUnit());
            }

            if(nutritionTable.getComponentValue("polyols") !=null){
                mCarbPolyolsVal.setText(Double.toString(nutritionTable.getComponentValue("polyols")) + nutritionTable.getCarbohydratesUnit());
            }

            if(nutritionTable.getComponentValue("starch") !=null){
                mCarbStarchVal.setText(Double.toString(nutritionTable.getComponentValue("starch")) + nutritionTable.getCarbohydratesUnit());
            }

            if(nutritionTable.getComponentValue("Fibre") !=null){
                mFibreVal.setText(Double.toString(nutritionTable.getComponentValue("Fibre")) + nutritionTable.getFibreUnit());
            }

            if(nutritionTable.getComponentValue("Protein") !=null){
                mProteinVal.setText(Double.toString(nutritionTable.getComponentValue("Protein")) + nutritionTable.getProteinUnit());
            }

            if(nutritionTable.getComponentValue("Salt") !=null){
                mSaltVal.setText(Double.toString(nutritionTable.getComponentValue("Salt")) + nutritionTable.getSaltUnit());
            }
        }
    }
}
