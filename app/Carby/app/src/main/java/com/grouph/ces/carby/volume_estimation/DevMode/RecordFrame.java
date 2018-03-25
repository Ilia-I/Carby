package com.grouph.ces.carby.volume_estimation.DevMode;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;

import org.opencv.android.Utils;
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
    private Bitmap frame;
    private Rect boundingBox;

    public RecordFrame(SharedPreferences preferences, String fileName){
        this.fileName = namePrefix(fileName);
        loadObj(preferences);
    }

    public RecordFrame(Mat frame, Rect boundingBox){
        this(""+Calendar.getInstance().getTime().getTime(),frame,boundingBox);
    }

    public RecordFrame(String fileName, Mat frame, Rect boundingBox) {
        this.fileName = namePrefix(fileName);
        this.frame = matToBitmap(frame);
        this.boundingBox = boundingBox;
    }

    public RecordFrame(Bitmap frame, Rect boundingBox){
        this(""+Calendar.getInstance().getTime().getTime(),frame,boundingBox);
    }

    public RecordFrame(String fileName, Bitmap frame, Rect boundingBox) {
        this.fileName = namePrefix(fileName);
        this.frame = frame;
        this.boundingBox = boundingBox;
    }

    public void saveObj(SharedPreferences preferences){
        SharedPreferences.Editor prefsEditor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(this);
        prefsEditor.putString(fileName, json);
        prefsEditor.apply();
    }

    private boolean loadObj(SharedPreferences preferences) {
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

    public Bitmap getFrame() {
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

    @Override
    public boolean equals(Object obj){
        Log.d(this.getClass().getName(),"compare:");
        if(obj==null){
            Log.d(this.getClass().getName(),"null");
            return false;
        }
        if(!(obj instanceof RecordFrame)){
            Log.d(this.getClass().getName(),"not RF");
            return false;
        }
        RecordFrame rf = (RecordFrame) obj;
        if(!this.getFileName().equals(rf.getFileName())){
            Log.d(this.getClass().getName(),"different name");
            return false;
        }
        if(!this.getBoundingBox().equals(rf.getBoundingBox())){
            Log.d(this.getClass().getName(),"different BoundingBox");
        }
        if(!this.getFrame().equals(rf.getFrame())) {
            Log.d(this.getClass().getName(),"different frame");
            return false;
        }
        return true;
    }

    private Bitmap matToBitmap(Mat input){
        Bitmap bitmap = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(input, bitmap);
        return bitmap;
    }
}
