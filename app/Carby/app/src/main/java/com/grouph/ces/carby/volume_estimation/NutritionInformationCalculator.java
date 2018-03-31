package com.grouph.ces.carby.volume_estimation;

import android.content.Context;
import android.support.annotation.IntDef;
import android.util.Log;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.nutrition_data.INutritionTable;
import com.grouph.ces.carby.nutrition_data.NutritionTable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Martin Peev on 31.03.2018 г..
 * Version: 0.1
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
        double mass = this.volume * Double.valueOf(context.getResources().getString(foodType));
        Log.d(this.getClass().getName(),"mass:"+mass);
        mass=50.0;
        nutritionTable = new NutritionTable();
        //Energy (in Kcal) = 4x (Proteins and carbohydrates mass in grams) + 9 x mass of fat in grams.
        nutritionTable.setComponent("Energy",getEnergyVal()*mass/100.0);
        nutritionTable.setComponent("Fat",getFatVal()*mass/100.0);
        nutritionTable.setComponent("Carbohydrate",getCarbohydrateVal()*mass/100.0);
        nutritionTable.setComponent("Protein",getProteinVal()*mass/100.0);
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
            case FOOD_BREAD:
            case FOOD_OATS:
            case FOOD_PASTA_BOILED:
            case FOOD_NOODLES_BOILED:
            case FOOD_RICE_BOILED:
            case FOOD_POTATO_BOILED:
            case FOOD_POTATO_SWEET:
            case FOOD_EGG_BOILED:
            default:return 0;
        }
    }

    public INutritionTable getNutritionTable(){
        return nutritionTable;
    }
}
