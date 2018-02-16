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

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.grouph.ces.carby.nutrition_data.INutritionTable;
import com.grouph.ces.carby.nutrition_data.NutritionTable;
import com.grouph.ces.carby.ui.camera.GraphicOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processor which gets detects and processes the nutrition table
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private boolean scan = false;

    public OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        mGraphicOverlay = ocrGraphicOverlay;
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
            //TODO table object not stored anywhere
            INutritionTable nt = tableMatcher(items);
            scan = false;
        }
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

        //split in columns
        return toTable(dataLines);
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
                if(typicalValues.substring(tempIdx).contains("%R")){
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

//        Map<String,String> valuePairs = new HashMap<>();
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
                    Log.d("OcrDetectorProcessor","idx:"+tempIdx);
//                    valuePairs.put(contents.get(i),row.substring(tempIdx));
                    setComponent(row.substring(tempIdx),contents.get(i),nt);
                    Log.d("OcrDetectorProcessor","Map<"+contents.get(i)+","+row.substring(tempIdx)+">");
                    contents.remove(i);
                    break;
                }
            }
        }

        return nt;
    }

    private boolean setComponent(String name, String value, INutritionTable nt) {
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
