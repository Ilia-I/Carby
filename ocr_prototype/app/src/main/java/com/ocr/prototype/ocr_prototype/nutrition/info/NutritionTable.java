package com.ocr.prototype.ocr_prototype.nutrition.info;

import com.ocr.prototype.ocr_prototype.activities.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Martin Peev on 28.01.2018 г..
 * Version: 0.1
 */

public class NutritionTable extends ANutritionTable{
    private @MeasurementUnit String energy;
    private @MeasurementUnit String content;
    private Map<String,IComposite> nutritionalInformation;

    public NutritionTable(){
        this(KILOCALORIES_UNIT, GRAMS_UNIT);
    }

    public NutritionTable(@MeasurementUnit String energy, @MeasurementUnit String content){
        this.energy = energy;
        this.content = content;
        nutritionalInformation = new HashMap<String,IComposite>();
    }


    //setter methods
    @Override
    public boolean setEnergy(double value,@MeasurementUnit String unit) {
        double convertedValue = super.convertUnits(value,unit,energy);
        if(convertedValue==-1) return false;
        IComposite compositeEnergy = new Composite(convertedValue);
        nutritionalInformation.put("Energy", compositeEnergy);
        return true;
    }

    @Override
    public boolean setFats(IComposite fats,@MeasurementUnit String unit) {
        //TODO unit conversion
        nutritionalInformation.put("Fat", fats);
        return true;
    }

    @Override
    public boolean setCarbohydrates(IComposite carbohydrates,@MeasurementUnit String unit) {
        //TODO unit conversion
        nutritionalInformation.put("Carbohydrate", carbohydrates);
        return true;
    }

    @Override
    public boolean setFibre(double value,@MeasurementUnit String unit) {
        double convertedValue = super.convertUnits(value,unit,content);
        if(convertedValue==-1) return false;
        IComposite compositeFibre = new Composite(convertedValue);
        nutritionalInformation.put("Fibre", compositeFibre);
        return true;
    }

    @Override
    public boolean setProtein(double value,@MeasurementUnit String unit) {
        double convertedValue = super.convertUnits(value,unit,content);
        if(convertedValue==-1) return false;
        IComposite compositeProtein = new Composite(convertedValue);
        nutritionalInformation.put("Protein", compositeProtein);
        return true;
    }

    @Override
    public boolean setSalt(double value,@MeasurementUnit String unit) {
        double convertedValue = super.convertUnits(value,unit,content);
        if(convertedValue==-1) return false;
        IComposite compositeSalt = new Composite(convertedValue);
        nutritionalInformation.put("Salt", compositeSalt);
        return true;
    }

    //getter methods
    private @MeasurementUnit String getContentUnit(){
        return content;
    }

    @Override
    public @MeasurementUnit String getEnergyUnit() {
        return energy;
    }

    @Override
    public double getEnergy() {
        return nutritionalInformation.get("Energy").getTotal();
    }

    @Override
    public @MeasurementUnit String getFatsUnit() {
        return getContentUnit();
    }

    @Override
    public IComposite getFats() {
        return nutritionalInformation.get("Fat");
    }

    @Override
    public @MeasurementUnit String getCarbohydratesUnit() {
        return getContentUnit();
    }

    @Override
    public IComposite getCarbohydrates() {
        return nutritionalInformation.get("Carbohydrate");
    }

    @Override
    public @MeasurementUnit String getFibreUnit() {
        return getContentUnit();
    }

    @Override
    public double getFibre() {
        return nutritionalInformation.get("Fibre").getTotal();
    }

    @Override
    public @MeasurementUnit String getProteinUnit() {
        return getContentUnit();
    }

    @Override
    public double getProtein() {
        return nutritionalInformation.get("Protein").getTotal();
    }

    @Override
    public @MeasurementUnit String getSaltUnit() {
        return getContentUnit();
    }

    @Override
    public double getSalt() {
        return nutritionalInformation.get("Salt").getTotal();
    }
}
