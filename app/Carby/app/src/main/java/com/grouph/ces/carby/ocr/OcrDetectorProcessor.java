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

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.grouph.ces.carby.database.AppDatabase;
import com.grouph.ces.carby.database.NutritionDataDB;
import com.grouph.ces.carby.nutrition_data.INutritionTable;
import com.grouph.ces.carby.nutrition_data.NutritionResultActivity;
import com.grouph.ces.carby.nutrition_data.NutritionTable;
import com.grouph.ces.carby.ui.camera.GraphicOverlay;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Processor which gets detects and processes the nutrition table
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private static final int NUM_SCANS = 5;
    private Context context;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private int scan;
    private String barcode;
    private boolean scanComplete = false;
    private List<List<String>> lineCollector;
    private long startTime;

    public OcrDetectorProcessor(Context applicationContext, GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        this.context = applicationContext;
        this.mGraphicOverlay = ocrGraphicOverlay;
        this.scan = 0;
        this.barcode = null;
        lineCollector = new ArrayList<>();
    }

    public OcrDetectorProcessor(Context applicationContext, GraphicOverlay<OcrGraphic> ocrGraphicOverlay, String barcode) {
        this(applicationContext,ocrGraphicOverlay);
        this.barcode = barcode;
    }

    /**
     * Once this implements Detector.Processor<TextBlock>, implement the abstract methods.
     * @param detections
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        if(scan>0) {
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
            lineCollector.add(lineBuilder(items));
            scan--;
            if(scan<=0){
                scanComplete = true;
            }
        } else if(scanComplete){
            Log.d(this.getClass().getName(),"LineCorrector:"+lineCollector.size());
            INutritionTable nt = tableMatcher(errorCorrectNums());
            Log.v("OcrDetectorProcessor","NutritionTable:\n"+nt);
            int key = record(barcode,nt);
            scanComplete = false;
            Log.d(this.getClass().getName(),"exec time:"+(System.currentTimeMillis() - startTime)+"ms");
            showResult(nt,key);
        }
    }

    private void showResult(INutritionTable nt,int key) {
        Intent result = new Intent(context, NutritionResultActivity.class);
        result.putExtra("jsonNutritionTable",nt.toJasonObject().toString());
        result.putExtra("id",key);
        context.startActivity(result);
    }

    /**
     * reduce error after 1st column
     * @return
     */
    private List<String> errorCorrectNums() {
        List<String> result = new ArrayList<>();
        List<String> contents = errCorrectionContents();
        contents.add("per");
        for(String content:contents){
            Log.d(this.getClass().getName(),"---------------\nerrorCorrectNums():"+content);
            List<String> lines = new ArrayList<>();
            for(List<String> scanedData: lineCollector){
                for(String s: scanedData){
                    if(s.startsWith(content) || (content.equals("per") && s.contains("per"))){
                        lines.add(s);
                        break;
                    }
                }
            }
            if(lines.size()>0){
                result.add(correct(lines));
            } else {
                Log.d(this.getClass().getName(),"None found");
            }
        }
        return result;
    }

    /**
     * find the best read line
     * @param lines
     * @return
     */
    private String correct(List<String> lines) {
        String result = "";
        removeShorter(lines);

        //if only one left or "per" line, return
        if(lines.size()==1){// || lines.get(0).contains("per")){
            Log.d(this.getClass().getName(),"result - 0:"+lines.get(0)+"\n");
            return lines.get(0);
        }

        //check word per word
        List<List<String>> words = new ArrayList<>();
        for(String line: lines){
            String[] tokens = line.split(" ");
            for(int i=0; i<tokens.length; i++){
                if(i>=words.size()) {
                    words.add(new ArrayList<>());
                }
                words.get(i).add(tokens[i]);
            }
        }

        for(List<String> list: words){
            int maxLen = removeShorter(list);
            if(list.size()==1){
                result+=list.get(0);
            } else {
                for (int i = 0; i < maxLen; i++) {
                    Map<Character,Integer> counter = new HashMap<>();
                    for (String line : list) {
                        if (counter.get(line.charAt(i)) == null) {
                            counter.put(line.charAt(i), 1);
                        } else {
                            counter.put(line.charAt(i), counter.get(line.charAt(i)).intValue() + 1);
                        }
                    }

                    Character largestKey = null;
                    for (Character key : counter.keySet()) {
                        if (largestKey == null) {
                            largestKey = key;
                        } else if (counter.get(largestKey) < counter.get(key)) {
                            largestKey = key;
                        }
                    }
                    result += largestKey;
                }
            }
            result += " ";
        }
        result = result.trim();

        Log.d(this.getClass().getName(),"result:"+result+"\n");
        return result;
    }

    /**
     * find longest String and remove all shorter ones
     * @param lines
     * @return
     */
    private int removeShorter(List<String> lines) {
        int maxLen = 0;
        //get length of largest
        for(String line: lines){
            Log.d(this.getClass().getName(),"line:"+line);
            if(line.length()>maxLen){
                maxLen = line.length();
            }
        }

        //remove all shorter as they are incomplete
        for(int i=lines.size()-1; i>=0; i--){
            if(lines.get(i).length()<maxLen){
                lines.remove(i);
            }
        }

        return maxLen;
    }

    /**
     * Associate nutrition table with barcode in database
     * @param barcode - barcode identifier for the nutrition table
     * @param nt - nutrition table to store
     */
    private int record(String barcode, INutritionTable nt) {
        AppDatabase db = Room.databaseBuilder(context ,AppDatabase.class,"myDB").allowMainThreadQueries().build();
        if(barcode==null){
            barcode = "";
        }
        NutritionDataDB nd = new NutritionDataDB(barcode,nt);
        db.nutritionDataDao().insertAll(nd);
        Log.d(this.getClass().getName(),"Barcode: "+barcode+"\nLoaded Table:\n"+db.nutritionDataDao().findByBarcode(barcode).getNt());
        return nd.getKey();
    }

    @Override
    public void release() {
        mGraphicOverlay.clear();
    }

    /**
     * Uses by the controller to enable scanning
     */
    public void scan() {
        lineCollector = new ArrayList<>();
        scan = NUM_SCANS;
        startTime = System.currentTimeMillis();
    }

    /**
     * Line builder algorithm
     * @param items
     */
    private List<String> lineBuilder(SparseArray<TextBlock> items){
        Map<Integer, List<Element>> scannedData = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            //every text block
            for (Line line : (List<Line>) items.valueAt(i).getComponents()) {
                //every line in the text block
                for (Element e : (List<Element>) line.getComponents()) {
                    //every element in the text block
                    boolean added = false;
                    int yE = e.getBoundingBox().centerY();
                    int heightE = e.getBoundingBox().height();

                    for (Integer y : scannedData.keySet()) {
                        //if element withing the accepted line limits, then add it
                        if (Math.abs(y - yE) < (heightE / 2)) {
                            scannedData.get(y).add(e);
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        List<Element> list = new ArrayList<>();
                        list.add(e);
                        scannedData.put(yE, list);
                    }
                }
            }
        }

        //compile lines
        List<String> dataLines = orderElements(scannedData);

        //error correction
        dataLines = errorCorrection(dataLines);

        return dataLines;
    }

    private INutritionTable tableMatcher(List<String> dataLines) {
        //split in columns
        return toTable(dataLines);
    }

    /**
     * Perform error correction on provided collection
     * @param dataLines - Note, this will be changed using the algorithm
     * @return corrected lines
     */
    private List<String> errorCorrection(List<String> dataLines) {
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
            List<String> splitLines = lineSplitter(in);
            String comp = splitLines.get(0);
            String rest = splitLines.get(1);

            List<Double> dist = new ArrayList<>();
            for(String s: contents) {
                dist.add(similarity(s,comp));
            }
            Log.d(this.getClass().getName(),comp+" closest:"+contents.get(dist.indexOf(Collections.max(dist)))+" s:"+Collections.max(dist));
            if(Collections.max(dist)>=0.5) {
                Log.d(this.getClass().getName(),"errorCorrection("+in+")");
                in = contents.get(dist.indexOf(Collections.max(dist))) +" "+ rest;
                Log.d(this.getClass().getName(),"to:"+in);
            }
            result.add(in);
        }

        return result;
    }

    /**
     * Split data line
     * @param in
     * @return
     */
    private List<String> lineSplitter(String in) {
        String[] tokens = in.split(" ");
        String comp = "";
        String rest = "";
        boolean col1 = true;
        for (String token : tokens) {
            if(col1&&!isVal(token)) {
                comp+=token+" ";
            } else {
                rest+=token+" ";
                col1=false;
            }
        }
        comp = comp.trim();
        rest = rest.trim();

        List<String> res = new ArrayList<>();
        res.add(comp);
        res.add(rest);
        return res;
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
        LevenshteinDistance ld = new LevenshteinDistance(expected.length());
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
                //TODO support non 100g/100ml
                if(typicalValues.contains("100g")||typicalValues.contains("100ml")) g100 = true;
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
                    if(g100 && (typicalValues.substring(tempIdx).contains("100g")||typicalValues.substring(tempIdx).contains("100ml"))){
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
                    if(row.endsWith("%")){//&& ri
                        row = row.substring(0,row.lastIndexOf(" "));
                        Log.d("OcrDetectorProcessor","remove %RI");
                    }
                    for(int k=1;k<numCols;k++){
                        int idx =row.lastIndexOf(" ");
                        if(idx>0) {
                            Log.d("OcrDetectorProcessor", "remove col:" + row.substring(idx));
                            row = row.substring(0, idx);
                        }
                    }
                    int tempIdx = row.lastIndexOf(" ");
                    if(tempIdx>0) {
                        try {
                            setComponent(contents.get(i), row.substring(tempIdx), nt);
                            Log.d("OcrDetectorProcessor", "Map<" + contents.get(i) + "," + row.substring(tempIdx) + ">");
                        } catch (NumberFormatException e){
                            Log.d(this.getClass().getName(),"Column miss-match!");
                        }
                    } else {
                        Log.d(this.getClass().getName(),"Invalid input!");
                    }
                    contents.remove(i);
                    break;
                }
            }
        }

        return nt;
    }

    private boolean setComponent(String name, String value, INutritionTable nt) throws NumberFormatException{
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

        //sort rows form top to bottom (needed for multi frame work)
        List<Integer> keys = new ArrayList<>(scannedData.keySet());
//        Collections.sort(keys, new Comparator<Integer>() {
//            @Override
//            public int compare(Integer x1, Integer x2) {
//                return x1 < x2 ? -1 : (x2 < x1) ? 1 : 0;
//            }
//        });

        for(Integer i: keys){
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
