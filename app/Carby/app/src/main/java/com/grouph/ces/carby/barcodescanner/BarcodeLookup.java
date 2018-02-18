package com.grouph.ces.carby.barcodescanner;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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
 *
 * Class for the OpenFoodFacts look-up of scanned barcode
 */

public class BarcodeLookup extends AsyncTask<Barcode, Void, JsonElement> {

    private ProgressBar progressBar;
    private TextView productText;

    public BarcodeLookup(ProgressBar progressBar, TextView productText){
        this.progressBar = progressBar;
        this.productText = productText;
    }

    @Override
    protected  void onPreExecute(){
        progressBar.setVisibility(View.VISIBLE);

        productText.setText("Searching Open Food Facts database");
        //productText.setVisibility(View.GONE);
    }

    @Override
    protected JsonElement doInBackground(Barcode... barcode){
        String DataURL = "https://world.openfoodfacts.org/api/v0/product/";
        //DataURL += barcode.displayValue + ".json";
        DataURL += "50457236.json";
        //TODO change back to actual barcode value and not test value

        try {
            URL url = new URL(DataURL);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            root = root.getAsJsonObject(); //May be an array, may be an object.

            return root;

        } catch (Exception e){
            Log.e("Tag", "This is not good: " + e.toString());
        }

        return null;
    }

    @Override
    protected void onPostExecute(JsonElement root){
        String rootStr = root.toString();
        JsonElement product = root.getAsJsonObject().get("product");
        JsonElement nutrients = product.getAsJsonObject().get("nutriments");

        String productName = product.getAsJsonObject().get("product_name").toString().replace("\"", "");
        String servingSize = product.getAsJsonObject().get("serving_size").toString().replace("\"", "");
        String carbPer100 = nutrients.getAsJsonObject().get("carbohydrates_serving").toString();


        Log.i("Tag", "Connection established...");
        Log.i("Tag", rootStr);

        Log.i("Tag", productName);
        Log.i("Tag", servingSize);
        Log.i("Tag", carbPer100);

        String result = productName;

        productText.setText(productName);
        progressBar.setVisibility(View.GONE);
        productText.setVisibility(View.VISIBLE);
    }
}
