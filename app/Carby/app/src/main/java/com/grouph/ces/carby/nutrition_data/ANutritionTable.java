package com.grouph.ces.carby.nutrition_data;

import java.util.List;

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

    protected boolean convertUnits(IComposite composite, @MeasurementUnit String units, @MeasurementUnit String goal){
        double temp = convertUnits(composite.getTotal(), units, goal);
        if(temp==-1) return false;
        composite.setTotal(temp);

        for(String componentName: composite.getSubComponentNames()){
            temp = convertUnits(composite.getContentOf(componentName), units, goal);
            if(temp==-1) return false;
            if(!composite.addSubComponent(componentName,temp)) return false;
        }
        return true;
    }

    protected boolean convertUnits(List<IComposite> composite, @MeasurementUnit String units, @MeasurementUnit String goal){
        boolean result = true;
        for(IComposite component: composite){
            result = result && convertUnits(component, units, goal);
        }
        return result;
    }
}
