package com.grouph.ces.carby.nutrition_data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
        Log.d(this.getClass().getName(),"Setting component:"+nutrientName+" "+content);
        if(!super.convertUnits(content,dataUnit,targetUnit)) return false;
        nutritionalInformation.put(nutrientName,content);
        return true;
    }

    @Override
    public List<String> listOfContents() {
        return listOfContents;
    }

    /**
     * set component with default measurement unit set on object init
     * @param name
     * @param value
     * @return true if value set, false if not
     */
    @Override
    public boolean setComponent(String name, double value) {
        Log.d("NutritionTable","setComponent("+name+","+value+")");
        switch (name){
            case "Energy":  return setEnergy(value,getEnergyUnit());
            case "Fat":     if(getFats()==null) {
                                return setFats(new Composite(value), getFatsUnit());
                            } else {
                                return getFats().setTotal(value);
                            }
            case "mono-unsaturates":
            case "polyunsaturates":
            case "saturates":if(getFats()==null){
                                IComposite fats = new Composite(value);
                                fats.addSubComponent(name,value);
                                return setFats(fats,getFatsUnit());
                            } else {
                                return getFats().addSubComponent(name,value);
                            }
            case "Carbohydrate":if(getCarbohydrates()==null) {
                                return setCarbohydrates(new Composite(value), getCarbohydratesUnit());
                            } else {
                                return getCarbohydrates().setTotal(value);
                            }
            case "sugars":
            case "polyols":
            case "starch":  if(getCarbohydrates()==null){
                                IComposite carbohydrates = new Composite(value);
                                carbohydrates.addSubComponent(name,value);
                                return setCarbohydrates(carbohydrates,getCarbohydratesUnit());
                            } else {
                                getCarbohydrates().addSubComponent(name, value);
                            }
            case "Fibre":   return setFibre(value,getFibreUnit());
            case "Protein": return setProtein(value,getProteinUnit());
            case "Salt":    return setSalt(value,getSaltUnit());
            default:
                return false;
        }
    }

    private void initListOfContents(){
        listOfContents = new ArrayList<>();
        listOfContents.add("Energy");
        listOfContents.add("Fat");
        listOfContents.add("mono-unsaturates");
        listOfContents.add("polyunsaturates");
        listOfContents.add("saturates");
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

    @Override
    public String toString(){
        String output = "Units: "+energy+" & "+content+"\n";
        for(String key: nutritionalInformation.keySet()){
            output += key+" "+nutritionalInformation.get(key).toString();
        }
        return output;
    }

    @Override
    public boolean setAll(JSONObject jo) throws JSONException{
        Iterator<String> keys = jo.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            nutritionalInformation.put(key, new Composite(jo.getJSONObject(key)));
        }
        return true;
    }

    @Override
    public JSONObject toJasonObject(){
        Map<String,JSONObject> temp = new HashMap<>();
        for(String key: nutritionalInformation.keySet()) {
            temp.put(key,nutritionalInformation.get(key).toJasonObject());
        }
        JSONObject jo = new JSONObject(temp);
        try {
            Log.d(this.getClass().getName(), "toJSONObject():" + jo.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject(temp);
    }
}
