package com.grouph.ces.carby.database;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import com.grouph.ces.carby.nutrition_data.INutritionTable;
import com.grouph.ces.carby.nutrition_data.NutritionTable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Martin Peev on 18.02.2018 г..
 * Version: 0.1
 */

public class NutritionTableConverter {
    @TypeConverter
    public static String fromINutritionTable(INutritionTable nt){
        if(nt == null) return null;
        return nt.toJasonObject().toString();
    }

    @TypeConverter
    public static INutritionTable toINutritionTable(String jsonObject){
        if(jsonObject == null) return null;
        INutritionTable nt = new NutritionTable();
        try {
            nt.setAll(new JSONObject(jsonObject));
        } catch (JSONException e) {
            Log.d("NutritionTableConverter","toINutritionTable("+jsonObject+")\n-> could not process JSONObject");
        }
        return nt;
    }
}
