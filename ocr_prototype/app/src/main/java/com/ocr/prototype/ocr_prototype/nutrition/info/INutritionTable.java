package com.ocr.prototype.ocr_prototype.nutrition.info;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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


    public boolean setEnergy(int value, @MeasurementUnit String unit);
    public boolean setFats(IComposite fats, @MeasurementUnit String unit);
    public boolean setCarbohydrates(IComposite carbohydrates, @MeasurementUnit String unit);
    public boolean setFibre(int value, @MeasurementUnit String unit);
    public boolean setProtein(int value, @MeasurementUnit String unit);
    public boolean setSalt(int value, @MeasurementUnit String unit);

    public @MeasurementUnit String getEnergyUnit();
    public int getEnergy();
    public @MeasurementUnit String getFatsUnit();
    public IComposite getFats();
    public @MeasurementUnit String getCarbohydratesUnit();
    public IComposite getCarbohydrates();
    public @MeasurementUnit String getFibreUnit();
    public int getFibre();
    public @MeasurementUnit String getProteinUnit();
    public int getProtein();
    public @MeasurementUnit String getSaltUnit();
    public int getSalt();
}
