package com.grouph.ces.carby.nutrition_data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Martin Peev on 28.01.2018 г..
 * Version: 0.2
 */

public class NutritionTable extends ANutritionTable{
    private @MeasurementUnit String energy;
    private @MeasurementUnit String content;
    private Map<String,IComposite> nutritionalInformation;
    private List<String> listOfContents;

    public NutritionTable(){
        this(KILOCALORIES_UNIT, GRAMS_UNIT);
    }

    public NutritionTable(@MeasurementUnit String energy, @MeasurementUnit String content){
        this.energy = energy;
        this.content = content;
        nutritionalInformation = new HashMap<String,IComposite>();
        initListOfContents();
    }


    //setter methods
    private boolean setter(String nutrientName,
                           IComposite content,
                           @MeasurementUnit String dataUnit,
                           @MeasurementUnit String targetUnit){
        if(!super.convertUnits(content,dataUnit,targetUnit)) return false;
        nutritionalInformation.put(nutrientName,content);
        return true;
    }

    @Override
    public List<String> listOfContents() {
        return listOfContents;
    }

    private void initListOfContents(){
        listOfContents = new ArrayList<>();
        listOfContents.add("Energy");
        listOfContents.add("Fat");
        listOfContents.add("saturates");
        listOfContents.add("mono-unsaturates");
        listOfContents.add("polysaturates");
        listOfContents.add("Carbohydrate");
        listOfContents.add("sugars");
        listOfContents.add("polyols");
        listOfContents.add("starch");
        listOfContents.add("Fibre");
        listOfContents.add("Protein");
        listOfContents.add("Salt");
    }

    @Override
    public boolean setEnergy(double value,@MeasurementUnit String unit) {
        return setter("Energy",new Composite(value),unit,energy);
    }

    @Override
    public boolean setFats(IComposite fats,@MeasurementUnit String unit) {
        return setter("Fat", fats, unit, content);
    }

    @Override
    public boolean setCarbohydrates(IComposite carbohydrates,@MeasurementUnit String unit) {
        return setter("Carbohydrate", carbohydrates, unit, content);
    }

    @Override
    public boolean setFibre(double value,@MeasurementUnit String unit) {
        return setter("Fibre", new Composite(value), unit, content);
    }

    @Override
    public boolean setProtein(double value,@MeasurementUnit String unit) {
        return setter("Protein", new Composite(value), unit, content);
    }

    @Override
    public boolean setSalt(double value,@MeasurementUnit String unit) {
        return setter("Salt", new Composite(value), unit, content);
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
