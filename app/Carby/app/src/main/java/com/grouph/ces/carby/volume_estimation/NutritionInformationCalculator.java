package com.grouph.ces.carby.volume_estimation;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.util.Log;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.database.AppDatabase;
import com.grouph.ces.carby.database.NutritionDataDB;
import com.grouph.ces.carby.nutrition_data.INutritionTable;
import com.grouph.ces.carby.nutrition_data.NutritionResultActivity;
import com.grouph.ces.carby.nutrition_data.NutritionTable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Martin Peev on 31.03.2018 г..
 * Version: 0.4
 */

public class NutritionInformationCalculator {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            FOOD_BREAD,
            FOOD_OATS,
            FOOD_PASTA_BOILED,
            FOOD_NOODLES_BOILED,
            FOOD_RICE_BOILED,
            FOOD_POTATO_BOILED,
            FOOD_POTATO_SWEET,
            FOOD_EGG_BOILED
    })
    public @interface FoodType {}
    public static final int FOOD_BREAD = R.string.density_bread;
    public static final int FOOD_OATS = R.string.density_oats;
    public static final int FOOD_PASTA_BOILED = R.string.density_pasta_boiled;
    public static final int FOOD_NOODLES_BOILED = R.string.density_noodles_boiled;
    public static final int FOOD_RICE_BOILED = R.string.density_rice_boiled;
    public static final int FOOD_POTATO_BOILED = R.string.density_potato_boiled;
    public static final int FOOD_POTATO_SWEET = R.string.density_potato_sweet;
    public static final int FOOD_EGG_BOILED = R.string.density_egg_boiled;

    private Context context;
    private double volume;
    private double mass;
    private @FoodType int foodType;
    private INutritionTable nutritionTable;

    /**
     * Constructor for the Nutrition Information Calculator
     * @param context app context
     * @param volume in cubic centimeters
     * @param foodType the measured food
     */
    public NutritionInformationCalculator(Context context, double volume, @FoodType int foodType){
        this.context = context;
        this.volume = volume;
        this.foodType = foodType;
        performCalculations();
    }

    private void performCalculations(){
        this.mass = this.volume * Double.valueOf(context.getResources().getString(foodType));
        Log.d(this.getClass().getName(),"Calculated mass:"+mass+"g");
        nutritionTable = new NutritionTable();

        //create a holder temporary nutrition table to display the results
        nutritionTable.setComponent("Energy",getEnergyVal()*mass/100.0);
        nutritionTable.setComponent("Fat",getFatVal()*mass/100.0);
        nutritionTable.setComponent("saturates",getSaturatesVal()*mass/100.0);
        nutritionTable.setComponent("polyunsaturates",getPolyunsaturatesVal()*mass/100.0);
        nutritionTable.setComponent("mono-unsaturates",getMonounsaturatesVal()*mass/100.0);
        nutritionTable.setComponent("Carbohydrate",getCarbohydrateVal()*mass/100.0);
        nutritionTable.setComponent("sugars",getSugarsVal()*mass/100.0);
        nutritionTable.setComponent("Protein",getProteinVal()*mass/100.0);
        nutritionTable.setComponent("Salt",getSaltVal()*mass/100.0);
    }

    private double getEnergyVal(){
        switch (foodType){
            case FOOD_BREAD: return Double.valueOf(context.getResources().getString(R.string.kcal_bread));
            case FOOD_OATS: return Double.valueOf(context.getResources().getString(R.string.kcal_oats));
            case FOOD_PASTA_BOILED: return Double.valueOf(context.getResources().getString(R.string.kcal_pasta_boiled));
            case FOOD_NOODLES_BOILED: return Double.valueOf(context.getResources().getString(R.string.kcal_noodles_boiled));
            case FOOD_RICE_BOILED: return Double.valueOf(context.getResources().getString(R.string.kcal_rice_boiled));
            case FOOD_POTATO_BOILED: return Double.valueOf(context.getResources().getString(R.string.kcal_potato_boiled));
            case FOOD_POTATO_SWEET: return Double.valueOf(context.getResources().getString(R.string.kcal_potato_sweet));
            case FOOD_EGG_BOILED: return Double.valueOf(context.getResources().getString(R.string.kcal_egg_boiled));
            default:return 0;
        }
    }

    private double getFatVal(){
        switch (foodType){
            case FOOD_BREAD: return Double.valueOf(context.getResources().getString(R.string.fat_bread));
            case FOOD_OATS: return Double.valueOf(context.getResources().getString(R.string.fat_oats));
            case FOOD_PASTA_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_pasta_boiled));
            case FOOD_NOODLES_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_noodles_boiled));
            case FOOD_RICE_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_rice_boiled));
            case FOOD_POTATO_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_potato_boiled));
            case FOOD_POTATO_SWEET: return Double.valueOf(context.getResources().getString(R.string.fat_potato_sweet));
            case FOOD_EGG_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_egg_boiled));
            default:return 0;
        }
    }

    private double getSaturatesVal(){
        switch (foodType){
            case FOOD_BREAD: return Double.valueOf(context.getResources().getString(R.string.fat_saturates_bread));
            case FOOD_OATS: return Double.valueOf(context.getResources().getString(R.string.fat_saturates_oats));
            case FOOD_PASTA_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_saturates_pasta_boiled));
            case FOOD_NOODLES_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_saturates_noodles_boiled));
            case FOOD_RICE_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_saturates_rice_boiled));
            case FOOD_POTATO_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_saturates_potato_boiled));
            case FOOD_POTATO_SWEET: return Double.valueOf(context.getResources().getString(R.string.fat_saturates_potato_sweet));
            case FOOD_EGG_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_saturates_egg_boiled));
            default:return 0;
        }
    }

    private double getPolyunsaturatesVal(){
        switch (foodType){
            case FOOD_BREAD: return Double.valueOf(context.getResources().getString(R.string.fat_polyunsaturates_bread));
            case FOOD_OATS: return Double.valueOf(context.getResources().getString(R.string.fat_polyunsaturates_oats));
            case FOOD_PASTA_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_polyunsaturates_pasta_boiled));
            case FOOD_NOODLES_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_polyunsaturates_noodles_boiled));
            case FOOD_RICE_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_polyunsaturates_rice_boiled));
            case FOOD_POTATO_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_polyunsaturates_potato_boiled));
            case FOOD_POTATO_SWEET: return Double.valueOf(context.getResources().getString(R.string.fat_polyunsaturates_potato_sweet));
            case FOOD_EGG_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_polyunsaturates_egg_boiled));
            default:return 0;
        }
    }

    private double getMonounsaturatesVal(){
        switch (foodType){
            case FOOD_BREAD: return Double.valueOf(context.getResources().getString(R.string.fat_monounsaturates_bread));
            case FOOD_OATS: return Double.valueOf(context.getResources().getString(R.string.fat_monounsaturates_oats));
            case FOOD_PASTA_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_monounsaturates_pasta_boiled));
            case FOOD_NOODLES_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_monounsaturates_noodles_boiled));
            case FOOD_RICE_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_monounsaturates_rice_boiled));
            case FOOD_POTATO_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_monounsaturates_potato_boiled));
            case FOOD_POTATO_SWEET: return Double.valueOf(context.getResources().getString(R.string.fat_monounsaturates_potato_sweet));
            case FOOD_EGG_BOILED: return Double.valueOf(context.getResources().getString(R.string.fat_monounsaturates_egg_boiled));
            default:return 0;
        }
    }

    private double getCarbohydrateVal(){
        switch (foodType){
            case FOOD_BREAD: return Double.valueOf(context.getResources().getString(R.string.carbs_bread));
            case FOOD_OATS: return Double.valueOf(context.getResources().getString(R.string.carbs_oats));
            case FOOD_PASTA_BOILED: return Double.valueOf(context.getResources().getString(R.string.carbs_pasta_boiled));
            case FOOD_NOODLES_BOILED: return Double.valueOf(context.getResources().getString(R.string.carbs_noodles_boiled));
            case FOOD_RICE_BOILED: return Double.valueOf(context.getResources().getString(R.string.carbs_rice_boiled));
            case FOOD_POTATO_BOILED: return Double.valueOf(context.getResources().getString(R.string.carbs_potato_boiled));
            case FOOD_POTATO_SWEET: return Double.valueOf(context.getResources().getString(R.string.carbs_potato_sweet));
            case FOOD_EGG_BOILED: return Double.valueOf(context.getResources().getString(R.string.carbs_egg_boiled));
            default:return 0;
        }
    }

    private double getSugarsVal(){
        switch (foodType){
            case FOOD_BREAD: return Double.valueOf(context.getResources().getString(R.string.carbs_sugar_bread));
            case FOOD_OATS: return Double.valueOf(context.getResources().getString(R.string.carbs_sugar_oats));
            case FOOD_PASTA_BOILED: return Double.valueOf(context.getResources().getString(R.string.carbs_sugar_pasta_boiled));
            case FOOD_NOODLES_BOILED: return Double.valueOf(context.getResources().getString(R.string.carbs_sugar_noodles_boiled));
            case FOOD_RICE_BOILED: return Double.valueOf(context.getResources().getString(R.string.carbs_sugar_rice_boiled));
            case FOOD_POTATO_BOILED: return Double.valueOf(context.getResources().getString(R.string.carbs_potato_boiled));
            case FOOD_POTATO_SWEET: return Double.valueOf(context.getResources().getString(R.string.carbs_sugar_potato_sweet));
            case FOOD_EGG_BOILED: return Double.valueOf(context.getResources().getString(R.string.carbs_sugar_egg_boiled));
            default:return 0;
        }
    }

    private double getProteinVal(){
        switch (foodType){
            case FOOD_BREAD: return Double.valueOf(context.getResources().getString(R.string.protein_bread));
            case FOOD_OATS: return Double.valueOf(context.getResources().getString(R.string.protein_oats));
            case FOOD_PASTA_BOILED: return Double.valueOf(context.getResources().getString(R.string.protein_pasta_boiled));
            case FOOD_NOODLES_BOILED: return Double.valueOf(context.getResources().getString(R.string.protein_noodles_boiled));
            case FOOD_RICE_BOILED: return Double.valueOf(context.getResources().getString(R.string.protein_rice_boiled));
            case FOOD_POTATO_BOILED: return Double.valueOf(context.getResources().getString(R.string.protein_potato_boiled));
            case FOOD_POTATO_SWEET: return Double.valueOf(context.getResources().getString(R.string.protein_potato_sweet));
            case FOOD_EGG_BOILED: return Double.valueOf(context.getResources().getString(R.string.protein_egg_boiled));
            default:return 0;
        }
    }

    private double getSaltVal(){
        switch (foodType){
            case FOOD_BREAD: return Double.valueOf(context.getResources().getString(R.string.salt_bread));
            case FOOD_OATS: return Double.valueOf(context.getResources().getString(R.string.salt_oats));
            case FOOD_PASTA_BOILED: return Double.valueOf(context.getResources().getString(R.string.salt_pasta_boiled));
            case FOOD_NOODLES_BOILED: return Double.valueOf(context.getResources().getString(R.string.salt_noodles_boiled));
            case FOOD_RICE_BOILED: return Double.valueOf(context.getResources().getString(R.string.salt_rice_boiled));
            case FOOD_POTATO_BOILED: return Double.valueOf(context.getResources().getString(R.string.salt_potato_boiled));
            case FOOD_POTATO_SWEET: return Double.valueOf(context.getResources().getString(R.string.salt_potato_sweet));
            case FOOD_EGG_BOILED: return Double.valueOf(context.getResources().getString(R.string.salt_egg_boiled));
            default:return 0;
        }
    }

    public static String getName(@FoodType int foodType){
        switch (foodType){
            case FOOD_BREAD: return "FOOD_BREAD";
            case FOOD_OATS: return "FOOD_OATS";
            case FOOD_PASTA_BOILED: return "FOOD_PASTA_BOILED";
            case FOOD_NOODLES_BOILED: return "FOOD_NOODLES_BOILED";
            case FOOD_RICE_BOILED: return "FOOD_RICE_BOILED";
            case FOOD_POTATO_BOILED: return "FOOD_POTATO_BOILED";
            case FOOD_POTATO_SWEET: return "FOOD_POTATO_SWEET";
            case FOOD_EGG_BOILED: return "FOOD_EGG_BOILED";
            default:return "";
        }
    }

    private int getID(){
        AppDatabase db = Room.databaseBuilder(context ,AppDatabase.class,"myDB").allowMainThreadQueries().build();
        NutritionDataDB nd = db.nutritionDataDao().findByName(getName(foodType));
        if(nd==null){
            return record(db).getKey();
        } else {
            return nd.getKey();
        }
    }

    private NutritionDataDB record(AppDatabase db){
        NutritionTable nt = new NutritionTable();
        nt.setComponent("Energy",getEnergyVal());
        nt.setComponent("Fat",getFatVal());
        nt.setComponent("saturates",getSaturatesVal());
        nt.setComponent("polyunsaturates",getPolyunsaturatesVal());
        nt.setComponent("mono-unsaturates",getMonounsaturatesVal());
        nt.setComponent("Carbohydrate",getCarbohydrateVal());
        nt.setComponent("sugars",getSugarsVal());
        nt.setComponent("Protein",getProteinVal());
        nt.setComponent("Salt",getSaltVal());

        NutritionDataDB nd = new NutritionDataDB(getName(foodType),"",nt,NutritionDataDB.SOURCE_VOLUME_ESTIMATION);
        db.nutritionDataDao().insertAll(nd);
        nd = db.nutritionDataDao().findByData(nd.getName(),nd.getBarcode(),nd.getNt());//fetch the correct key
        return nd;
    }

    /**
     * Open the NutritionResultActivity to show the results
     */
    public void show(){
        Intent result = new Intent(context, NutritionResultActivity.class);
        result.putExtra("jsonNutritionTable", nutritionTable.toJasonObject().toString());
        result.putExtra("id",getID());
        result.putExtra("per100g",false);
        result.putExtra("mass",mass);
        result.putExtra("source",NutritionResultActivity.SOURCE_VOLUME_ESTIMATION);
        context.startActivity(result);
    }
}
