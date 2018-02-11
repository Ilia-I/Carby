package com.grouph.ces.carby.nutrition_data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Martin Peev on 28.01.2018 г..
 * Version: 0.1
 */

public class Composite implements IComposite {
    private double content;
    private Map<String, Double> components;

    public Composite(double content){
        this.content = content;
        components = new HashMap<String, Double>();
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
}
