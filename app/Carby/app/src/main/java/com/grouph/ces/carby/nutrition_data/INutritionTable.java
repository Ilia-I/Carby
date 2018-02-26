package com.grouph.ces.carby.nutrition_data;

import android.support.annotation.StringDef;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Interface for storage of the nutrition information
 * VALUES STORED WILL BE PER 100g OR 100ml OF VOLUME
 * Created by Martin Peev
 */

public interface INutritionTable {
    //ENUMs are bad in Android, so we use StringDef
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            GRAMS_UNIT,
            MILILITERS_UNIT,
            KILOJOULES_UNIT,
            KILOCALORIES_UNIT
    })
    public @interface MeasurementUnit {}
    public static final String GRAMS_UNIT = "g";
    public static final String MILILITERS_UNIT = "ml";
    public static final String KILOJOULES_UNIT = "kJ";
    public static final String KILOCALORIES_UNIT = "kcal";

    public List<String> listOfContents();
    public boolean setComponent(String name, double value);

    public boolean setEnergy(double value, @MeasurementUnit String unit);
    public boolean setFats(IComposite fats, @MeasurementUnit String unit);
    public boolean setCarbohydrates(IComposite carbohydrates, @MeasurementUnit String unit);
    public boolean setFibre(double value, @MeasurementUnit String unit);
    public boolean setProtein(double value, @MeasurementUnit String unit);
    public boolean setSalt(double value, @MeasurementUnit String unit);

    public @MeasurementUnit String getEnergyUnit();
    public double getEnergy();
    public @MeasurementUnit String getFatsUnit();
    public IComposite getFats();
    public @MeasurementUnit String getCarbohydratesUnit();
    public IComposite getCarbohydrates();
    public @MeasurementUnit String getFibreUnit();
    public double getFibre();
    public @MeasurementUnit String getProteinUnit();
    public double getProtein();
    public @MeasurementUnit String getSaltUnit();
    public double getSalt();

    public boolean setAll(JSONObject jo) throws JSONException;
    public JSONObject toJasonObject();
}
