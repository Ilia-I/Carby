package com.grouph.ces.carby.volume_estimation;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by Martin Peev on 24.03.2018 г..
 * Version: 0.1
 */

public class RecordFrame {
    private static final String prefix = "takePicture_";
    private String fileName;
    private Mat frame;
    private Rect boundingBox;
    private SharedPreferences preferences;

    public RecordFrame(SharedPreferences preferences, String fileName){
        this.preferences = preferences;
        this.fileName = namePrefix(fileName);
        loadObj();
    }

    public RecordFrame(SharedPreferences preferences, Mat frame, Rect boundingBox){
        this(preferences, ""+Calendar.getInstance().getTime().getTime(),frame,boundingBox);
    }

    public RecordFrame(SharedPreferences preferences, String fileName, Mat frame, Rect boundingBox) {
        this.preferences = preferences;
        this.fileName = namePrefix(fileName);
        this.frame = frame;
        this.boundingBox = boundingBox;
        saveObj();
    }

    public void saveObj(){
        SharedPreferences.Editor prefsEditor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(this);
        prefsEditor.putString(fileName, json);
        prefsEditor.apply();
    }

    private boolean loadObj() {
        Gson gson = new Gson();
        String defValue = "";
        String json = preferences.getString(fileName, defValue);
        if(json.equals(defValue)) return false;
        RecordFrame obj = gson.fromJson(json, RecordFrame.class);
        this.frame = obj.getFrame();
        this.boundingBox = obj.getBoundingBox();
        return true;
    }

    public String getFileName() {
        return fileName;
    }

    public Mat getFrame() {
        return frame;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    private String namePrefix(String name){
        if(name.startsWith(prefix)){
            return name;
        } else {
            return prefix+name;
        }
    }

    /**
     *
     * @param preferences
     * @return list of recorded file names
     */
    public static List<String> recordedFrameNames(SharedPreferences preferences){
        Map<String,?> keys = preferences.getAll();
        List<String> names = new ArrayList<>();
        for(String entry : keys.keySet()){
            if(entry.startsWith(prefix)){
                names.add(entry);
            }
        }
        return names;
    }
}
