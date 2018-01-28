package com.ocr.prototype.ocr_prototype.nutrition.info;

/**
 * Created by Martin Peev on 28.01.2018 г..
 * Version: 0.1
 */

public abstract class ANutritionTable implements INutritionTable {

    protected double convertUnits(double value, @MeasurementUnit String units, @MeasurementUnit String goal){
        if(units.equals(goal)) return value;
        switch (units){
            case GRAMS_UNIT:
                if(goal.equals(MILILITERS_UNIT)) return value;
                else return -1;
            case MILILITERS_UNIT:
                if(goal.equals(GRAMS_UNIT)) return value;
                else return -1;
            case KILOJOULES_UNIT:
                if(goal.equals(KILOCALORIES_UNIT)){
                    return (0.239*value);
                } else return -1;
            case KILOCALORIES_UNIT:
                if(goal.equals(KILOCALORIES_UNIT)){
                    return (4.187*value);
                } else return -1;
            default: return -1;
        }
    }
}
