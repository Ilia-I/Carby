/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.grouph.ces.carby.ocr;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.grouph.ces.carby.database.AppDatabase;
import com.grouph.ces.carby.database.AppDatabase_Impl;
import com.grouph.ces.carby.database.NutritionDataDB;
import com.grouph.ces.carby.nutrition_data.INutritionTable;
import com.grouph.ces.carby.nutrition_data.NutritionTable;
import com.grouph.ces.carby.ui.camera.GraphicOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Processor which gets detects and processes the nutrition table
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private Context context;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private boolean scan = false;

    public OcrDetectorProcessor(Context applicationContext, GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        this.context = applicationContext;
        this.mGraphicOverlay = ocrGraphicOverlay;
    }

    // Once this implements Detector.Processor<TextBlock>, implement the abstract methods.
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        if(scan) {
            mGraphicOverlay.clear();
            SparseArray<TextBlock> items = detections.getDetectedItems();
            Log.d("Processor", "num items:" + items.size());
            for (int i = 0; i < items.size(); ++i) {
                TextBlock item = items.valueAt(i);
                if (item != null && item.getValue() != null) {
                    Log.d("Processor", "Text detected! " + item.getValue());
                }
                OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
                mGraphicOverlay.add(graphic);
            }
            //TODO provide a correct barcode
            INutritionTable nt = tableMatcher(items);
            Log.v("OcrDetectorProcessor","NutritionTable:\n"+nt);
            record(100,nt);
            scan = false;
        }
    }

    private void record(int barcode, INutritionTable nt) {
        AppDatabase db = Room.databaseBuilder(context ,AppDatabase.class,"myDB").allowMainThreadQueries().build();

        //TODO remove the auto delete for loop
        // for test reasons delete all previous content
        for(NutritionDataDB nd: db.nutritionDataDao().getAll()){
            db.nutritionDataDao().delete(nd);
        }

        db.nutritionDataDao().insertAll(new NutritionDataDB(barcode,nt));
        Log.d("OcrDetectorProcessor","loaded table:\n"+db.nutritionDataDao().findByBarcode(barcode).getNt());
    }

    @Override
    public void release() {
        mGraphicOverlay.clear();
    }

    /**
     * Uses by the controller to enable scanning
     */
    public void scan() {
        scan = true;
    }

    /**
     * Table matcher algorithm
     * @param items
     */
    private INutritionTable tableMatcher(SparseArray<TextBlock> items){
        Map<Integer,List<Element>> scannedData = new HashMap<>();
        for(int i=0;i<items.size();i++) {
            //every text block
            for(Line line: (List<Line>) items.valueAt(i).getComponents()){
                //every line in the text block
                for(Element e: (List<Element>)line.getComponents()){
                    //every element in the text block
                    boolean added = false;
                    int yE = e.getBoundingBox().centerY();
                    int heightE = e.getBoundingBox().height();

                    for(Integer y: scannedData.keySet()){
                        //if element withing the accepted line limits, then add it
                        if(Math.abs(y-yE)<(heightE/2)){
                            scannedData.get(y).add(e);
                            added = true;
                            break;
                        }
                    }
                    if(!added){
                        List<Element> list = new ArrayList<>();
                        list.add(e);
                        scannedData.put(yE,list);
                    }
                }
            }
        }

        //compile lines
        List<String> dataLines = orderElements(scannedData);

        //error correction
        dataLines = errorCorrection(dataLines);

        //split in columns
        return toTable(dataLines);
    }

    /**
     * Perform error correction on provided collection
     * @param dataLines - Note, this will be changed using the algorithm
     * @return corrected lines
     */
    private List<String> errorCorrection(List<String> dataLines) {
        //TODO test algoirthm
        List<String> result = new ArrayList<>();
        List<String> contents = errCorrectionContents();

        //find all correct lines and move them to the result list
        for(String str: dataLines){
            if(str.contains("per")){
                Log.d(this.getClass().getName(),"errorCorrection() -> per line:"+str);
                result.add(str);
            } else {
                for (int i = 0; i < contents.size(); i++) {
                    if (str.contains(contents.get(i))) {
                        Log.d(this.getClass().getName(),"errorCorrection() -> correct:"+contents.get(i)+" in "+str);
                        result.add(str);
                        contents.remove(i);
                        break;
                    }
                }
            }
        }
        dataLines.removeAll(result);

        //process the rest
        for(String in:dataLines){
            String[] tokens = in.split(" ");
            String comp = "";
            for (String token : tokens) {
                if(!isVal(token)) {
                    comp+=token+" ";
                } else break;
            }
            comp = comp.trim();

            List<Double> dist = new ArrayList<>();
            for(String s: contents) {
                dist.add(similarity(s,comp));
            }
            Log.d(this.getClass().getName(),comp+" closest:"+contents.get(dist.indexOf(Collections.max(dist)))+" s:"+Collections.max(dist));
            if(Collections.max(dist)>=0.5) {
                in = contents.get(dist.indexOf(Collections.max(dist))) +" "+ in;
            }
            result.add(in);
            Log.d(this.getClass().getName(),in);
        }

        return result;
    }

    /**
     * utility method for getting list of strings for error correction
     * @return
     */
    private List<String> errCorrectionContents(){
        List<String> listOfContents = new NutritionTable().listOfContents();
        listOfContents.add("of which mono-unsaturates");
        listOfContents.add("of which polyunsaturates");
        listOfContents.add("of which saturates");
        listOfContents.add("of which sugars");
        listOfContents.add("of which polyols");
        listOfContents.add("of which starch");
        return listOfContents;
    }

    /**
     * Utility method for calculating distance between two object using Levenshtein Distance metric
     * @param expected
     * @param compared
     * @return 0-1 where 1 is identical
     */
    private double similarity(String expected, String compared) {
        String longer = expected, shorter = compared;
        if (expected.length() < compared.length()) { // longer should always have greater length
            longer = compared; shorter = expected;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
        LevenshteinDistance ld = new LevenshteinDistance(expected.length());//(int)(expected.length()/2)+1);
        System.out.println("lev:"+ld.apply(longer, shorter)+" "+ld.apply(shorter,longer));
        int distance = ld.apply(longer, shorter);
        if(distance<0) return 0;
        return (longerLength - distance) / (double) longerLength;
    }

    /**
     * utility method for checking if the provided string is a number with possible unit appended
     * @param s - string to be processed
     * @return boolean result
     */
    private boolean isVal(String s) {
        String pattern= "\\d+(\\.\\d)?(\\p{ASCII})*";
        return s.matches(pattern);
    }

    private INutritionTable toTable(List<String> dataLines) {
        int numCols = 0;
        INutritionTable nt = new NutritionTable();
        List<String> contents = nt.listOfContents();

        boolean ri = false;
        boolean g100 = false;
        for(String typicalValues: dataLines){
            if(typicalValues.contains("per")){
                //TODO support non 100g
                if(typicalValues.contains("100g")) g100 = true;
                int tempIdx = typicalValues.lastIndexOf(" ");
                //check if table has %RI (reference intake of average adult)
                if(typicalValues.substring(tempIdx).matches("(?s).*\\p{Space}.{0,1}RI.*|(?s).*\\p{Space}\\p{Punct}\\wI.*|(?s).*\\p{Punct}R\\w.*")){//.contains("%R")){
                    Log.d(this.getClass().getName(),"%RI detected");
                    typicalValues = typicalValues.substring(0,tempIdx);
                    ri = true;
                }
                do {
                    tempIdx = typicalValues.lastIndexOf("per");
                    numCols++;
                    if(g100 && typicalValues.substring(tempIdx).contains("100g")){
                        break;
                    }
                    typicalValues = typicalValues.substring(0,tempIdx);

                }while(typicalValues.contains("per"));
                break;
            }
        }

        for(String row: dataLines){
            for(int i=0; i<contents.size();i++) {
                if (row.contains(contents.get(i))&&(row.indexOf(contents.get(i))==0||row.charAt(row.indexOf(contents.get(i))-1)==' ')){
                    Log.d("OcrDetectorProcessor","Cols:"+numCols+" Content:"+contents.get(i)+" Row:"+row);
                    if(ri && row.endsWith("%")){
                        row = row.substring(0,row.lastIndexOf(" "));
                        Log.d("OcrDetectorProcessor","remove %RI");
                    }
                    for(int k=1;k<numCols;k++){
                        int idx =row.lastIndexOf(" ");
                        Log.d("OcrDetectorProcessor","remove col:"+row.substring(idx));
                        row = row.substring(0,idx);
                    }
                    int tempIdx = row.lastIndexOf(" ");
//                    Log.d("OcrDetectorProcessor","idx:"+tempIdx);
                    setComponent(contents.get(i),row.substring(tempIdx),nt);
                    Log.d("OcrDetectorProcessor","Map<"+contents.get(i)+","+row.substring(tempIdx)+">");
                    contents.remove(i);
                    break;
                }
            }
        }

        return nt;
    }

    private boolean setComponent(String name, String value, INutritionTable nt) {
        Log.d(this.getClass().getName(),"setComponent:"+name+" - "+value);
        if(name.equals("Energy")){
            //make adjustments for energy 5.5kJ/5.5kcal format
            String[] strAr = value.split("/");
            for(String val: strAr){
                if(val.contains("kcal")){
                    return nt.setComponent(name, Double.valueOf(val.replaceAll("[^\\.0123456789]","")));
                }
            }
        } else {
            //everything else uses the same 5.5g or 5.5ml format
            return nt.setComponent(name, Double.valueOf(value.replaceAll("[^\\.0123456789]","")));
        }
        return false;
    }

    /**
     * Group input by rows and order them in strings
     * @param scannedData
     * @return
     */
    private List<String> orderElements(Map<Integer, List<Element>> scannedData) {
        List<String> result = new ArrayList<>();

        for(Integer i: scannedData.keySet()){
            String line = "";
            List<Element> list = scannedData.get(i);
            Collections.sort(list, new Comparator<Element>() { //sort by place in line (x coordinate)
                @Override
                public int compare(Element lhs, Element rhs) {
                    // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    int x1 = lhs.getBoundingBox().centerX();
                    int x2 = rhs.getBoundingBox().centerX();
                    return x1 < x2 ? -1 : (x2 < x1) ? 1 : 0;
                }
            });
            for(Element e: list){
                line+=e.getValue();
                if(addSpace(e.getValue())){
                    line+=" ";
                }
            }
            Log.d("OcrDetectorProcessor","string line "+i+":"+line);
            result.add(line.trim());
        }
        return result;
    }

    /**
     * utility method for processing rows
     * @param value
     * @return
     */
    private boolean addSpace(String value) {
//        "\\d++\\s{1}+\\p{Punct}\\s{1}+\\d++"
        String pattern= "\\d|\\p{Punct}";
        return !value.matches(pattern);
    }
}
