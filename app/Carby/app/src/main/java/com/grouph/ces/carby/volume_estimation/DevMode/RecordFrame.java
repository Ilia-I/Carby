package com.grouph.ces.carby.volume_estimation.DevMode;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.grouph.ces.carby.volume_estimation.Frame;
import com.grouph.ces.carby.volume_estimation.ResultsFragment;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by Martin Peev on 24.03.2018 г..
 * Version: 0.8
 */

public class RecordFrame {
    private static final String prefix = "takePicture_";
    private String fileName;
    private double pixelsPerCm = -1.0;
    private Rect boundingBox;
    private String encodedImg;

    public RecordFrame(SharedPreferences preferences, String fileName){
        this.fileName = namePrefix(fileName);
        loadObj(preferences);
    }

    public RecordFrame(Frame frame){
        this(""+Calendar.getInstance().getTime().getTime(), frame);
    }

    public RecordFrame(String fileName, Frame frame) {
        this.fileName = namePrefix(fileName);
        this.encodedImg = getStringFromBitmap(matToBitmap(frame.getImage()));
        this.boundingBox = frame.getBoundingBox();
        this.pixelsPerCm = frame.getReferenceObjectSize();
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
        this.encodedImg = obj.getEncodedImg();
        this.boundingBox = obj.getBoundingBox();
        this.pixelsPerCm = obj.getPixelsPerCm();
        return true;
    }

    public String getFileName() {
        return fileName;
    }

    public Bitmap getImage() {
        return getBitmapFromString(encodedImg);
    }

    public double getPixelsPerCm() {
        return pixelsPerCm;
    }

    private String getEncodedImg(){return encodedImg;}

    public Rect getBoundingBox() {
        return boundingBox;
    }

    private String namePrefix(String name){
        if(name.startsWith(prefix)
                ||name.startsWith(ResultsFragment.IMAGE_SET_ORIGINAL)
                ||name.startsWith(ResultsFragment.IMAGE_SET_MASK)
                ||name.startsWith(ResultsFragment.IMAGE_SET_STRETCH)
                ){
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
        if(!this.getEncodedImg().equals(rf.getEncodedImg())) {
            Log.d(this.getClass().getName(),"different encoded string");
            return false;
        }
        return true;
    }

    private Bitmap matToBitmap(Mat input){
        Bitmap bitmap = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(input, bitmap);
        return bitmap;
    }

    /**
     * This functions converts Bitmap picture to a string which can be
     * JSONified.
     * @param bitmapPicture Bitmap to encode
     * @return encoded image string
     */
    private String getStringFromBitmap(Bitmap bitmapPicture) {
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    /**
     * This Function converts the String back to Bitmap
     * @param stringPicture encoded image string
     * @return decoded Bitmap
     */
    private Bitmap getBitmapFromString(String stringPicture) {
        byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public void delete(SharedPreferences preferences) {
        SharedPreferences.Editor prefsEditor = preferences.edit();
        prefsEditor.remove(getFileName());
        prefsEditor.apply();
    }
}
