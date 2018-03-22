package com.grouph.ces.carby.nutrition_data;

import org.json.JSONObject;

import java.util.List;

/**
 * Interface for the composite class to hold
 * 'of which' composition values
 *
 * Created by Martin Peev
 */

public interface IComposite {
    public boolean setTotal(double content);
    public double getTotal();
    public boolean addSubComponent(String name, double content);
    public List<String> getSubComponentNames();
    public Double getContentOf(String name);
    public JSONObject toJasonObject();
}
