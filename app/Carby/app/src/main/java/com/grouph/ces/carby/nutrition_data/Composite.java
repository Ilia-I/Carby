package com.grouph.ces.carby.nutrition_data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Martin Peev on 28.01.2018 г..
 * Version: 0.3
 */

public class Composite implements IComposite {
    private final static String total = "Total";
    private double content;
    private Map<String, Double> components;

    public Composite(double content){
        this.content = content;
        components = new HashMap<String, Double>();
    }

    public Composite(JSONObject jo) throws JSONException {
        this(jo.getDouble(total));
        Iterator<String> keys = jo.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if(!key.equals(total)){
                components.put(key,jo.getDouble(key));
            }
        }
    }

    @Override
    public boolean setTotal(double content) {
        this.content = content;
        return true;
    }

    @Override
    public double getTotal() {
        return content;
    }

    @Override
    public boolean addSubComponent(String name, double content) {
        components.put(name,content);
        return true;
    }

    @Override
    public List<String> getSubComponentNames() {
        return new ArrayList<String>(components.keySet());
    }

    /**
     *
     * @param name
     * @return contents of specified component or -1 if not found
     */
    @Override
    public double getContentOf(String name) {
        if(components.containsKey(name)) {
            return components.get(name);
        } else {
            return 0;
        }
    }

    @Override
    public String toString(){
        String output = this.content+"\n";
        if(components.keySet().size()>0) output+="of which\n";
        for(String key: components.keySet()){
            output += key+" "+components.get(key)+"\n";
        }
        return output;
    }

    public JSONObject toJasonObject(){
        Map<String,Double> temp = new HashMap<>(components);
        temp.put(total,getTotal());
        return new JSONObject(temp);
    }
}
