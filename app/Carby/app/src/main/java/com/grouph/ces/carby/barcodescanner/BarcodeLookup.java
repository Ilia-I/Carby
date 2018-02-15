package com.grouph.ces.carby.barcodescanner;

import android.util.Log;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by George on 15/02/2018.
 */

public class BarcodeLookup {

    public BarcodeLookup(){

    }

    public void getDatabaseItem(final Barcode barcode){

        new Thread(new Runnable(){
            @Override
            public void run() {
                String Url = "https://world.openfoodfacts.org/api/v0/product/";
                Url += barcode.displayValue + ".json";

                try {
                    URL url = new URL(Url);
                    HttpURLConnection request = (HttpURLConnection) url.openConnection();
                    request.connect();

                    // Convert to a JSON object to print data
                    JsonParser jp = new JsonParser(); //from gson
                    JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
                    JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.

                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String json = gson.toJson(rootobj);

                    Log.i("Tag", "rootobj grabbed");

                    Log.i("Tag", json);


                } catch (Exception e){
                    Log.e("Tag", "This is not good: " + e.toString());
                }
            }
        }).start();
    }
}
