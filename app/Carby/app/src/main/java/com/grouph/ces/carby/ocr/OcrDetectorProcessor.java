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
import com.grouph.ces.carby.ui.camera.GraphicOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 * Make this implement Detector.Processor<TextBlock> and add text to the GraphicOverlay
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
            tableMatcher(items);
            scan = false;
        }
    }

    @Override
    public void release() {
        mGraphicOverlay.clear();
    }

    public void scan() {
        scan = true;
    }

    private void tableMatcher(SparseArray<TextBlock> items){
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
        //TODO split each line into columns [some text][[number][unit]]++
    }

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
            result.add(line);
        }
        return result;
    }

    private boolean addSpace(String value) {
//        "\\d++\\s{1}+\\p{Punct}\\s{1}+\\d++"
        String pattern= "\\d|\\p{Punct}";
        return !value.matches(pattern);
    }
}
