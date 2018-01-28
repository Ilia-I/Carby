package com.ocr.prototype.ocr_prototype.nutrition.info;

import java.util.Map;

/**
 * Created by Martin Peev on 28.01.2018 г..
 * Version: 0.1
 */

public class NutritionTable implements INutritionTable{
    private @MeasurementUnit String energy;
    private @MeasurementUnit String content;
    private Map<String,IComposite> nutritionalInformation;

    public NutritionTable(){
        this(KILOCALORIES_UNIT, GRAMS_UNIT);
    }

    public NutritionTable(@MeasurementUnit String energy, @MeasurementUnit String content){
        this.energy = energy;
        this.content = content;
    }


    //setter methods
    @Override
    public boolean setEnergy(int value, String unit) {
        //TODO Composite
        IComposite compositeEnergy = null;
        nutritionalInformation.put("Energy", compositeEnergy);
        return true;
    }

    @Override
    public boolean setFats(IComposite fats, String unit) {
        nutritionalInformation.put("Fat", fats);
        return true;
    }

    @Override
    public boolean setCarbohydrates(IComposite carbohydrates, String unit) {
        nutritionalInformation.put("Carbohydrate", carbohydrates);
        return true;
    }

    @Override
    public boolean setFibre(int value, String unit) {
        //TODO Composite
        IComposite compositeFibre = null;
        nutritionalInformation.put("Fibre", compositeFibre);
        return true;
    }

    @Override
    public boolean setProtein(int value, String unit) {
        //TODO Composite
        IComposite compositeProtein = null;
        nutritionalInformation.put("Protein", compositeProtein);
        return true;
    }

    @Override
    public boolean setSalt(int value, String unit) {
        //TODO Composite
        IComposite compositeSalt = null;
        nutritionalInformation.put("Salt", compositeSalt);
        return true;
    }

    //getter methods
    private @MeasurementUnit String getContentUnit(){
        return content;
    }

    @Override
    public String getEnergyUnit() {
        return energy;
    }

    @Override
    public int getEnergy() {
        //TODO
        return 0;
    }

    @Override
    public String getFatsUnit() {
        return getContentUnit();
    }

    @Override
    public IComposite getFats() {
        return nutritionalInformation.get("Fat");
    }

    @Override
    public String getCarbohydratesUnit() {
        return getContentUnit();
    }

    @Override
    public IComposite getCarbohydrates() {
        return nutritionalInformation.get("Carbohydrate");
    }

    @Override
    public String getFibreUnit() {
        return getContentUnit();
    }

    @Override
    public int getFibre() {
        //TODO
        return 0;
    }

    @Override
    public String getProteinUnit() {
        return getContentUnit();
    }

    @Override
    public int getProtein() {
        //TODO
        return 0;
    }

    @Override
    public String getSaltUnit() {
        return getContentUnit();
    }

    @Override
    public int getSalt() {
        //TODO
        return 0;
    }
}
