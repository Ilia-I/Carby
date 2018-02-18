package com.grouph.ces.carby.barcodescanner;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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

    private TextView barcodeHeader;
    private TextView productResult;
    private ProgressBar progressBar;

    public BarcodeLookup(TextView barcodeHeader, TextView productResult, ProgressBar progressBar){
        this.barcodeHeader = barcodeHeader;
        this.productResult = productResult;
        this.progressBar = progressBar;
    }

    @Override
    protected  void onPreExecute(){
        progressBar.setVisibility(View.VISIBLE);
        barcodeHeader.setText("Searching Open Food Facts database");
        productResult.setVisibility(View.GONE);
    }

    @Override
    protected JsonElement doInBackground(Barcode... barcode){
        String DataURL = "https://world.openfoodfacts.org/api/v0/product/";
        DataURL += barcode[0].displayValue + ".json";
        //DataURL += "50457236.json";
        //TODO change back to actual barcode value and not test value

        try {
            URL url = new URL(DataURL);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            root = root.getAsJsonObject(); //May be an array, may be an object.

            int status = root.getAsJsonObject().get("status").getAsInt();

            if(status==0){
                return null;
            }else{
                return root;
            }

        } catch (Exception e){
            Log.e("Tag", "This is not good: " + e.toString());
        }

        return null;
    }

    @Override
    protected void onPostExecute(JsonElement root){
        if( root!= null) {
            String rootStr = root.toString();
            JsonElement product = root.getAsJsonObject().get("product");
            JsonElement nutrients = product.getAsJsonObject().get("nutriments");

            String productName = product.getAsJsonObject().get("product_name").toString().replace("\"", "");
            String servingSize = product.getAsJsonObject().get("serving_size").toString().replace("\"", "");
            String carbsPerServing = nutrients.getAsJsonObject().get("carbohydrates_serving").toString();


            Log.i("Tag", "Connection established...");
            Log.i("Tag", rootStr);

            Log.i("Tag", productName);
            Log.i("Tag", servingSize);
            Log.i("Tag", carbsPerServing);

            String result = carbsPerServing + "g carbohydrates per " + servingSize + " serving";


            barcodeHeader.setText(productName);
            barcodeHeader.setTypeface(barcodeHeader.getTypeface(), Typeface.BOLD);
            productResult.setText(result);

            progressBar.setVisibility(View.GONE);
            barcodeHeader.setVisibility(View.VISIBLE);
            productResult.setVisibility(View.VISIBLE);
        }else{
            barcodeHeader.setText("Could not find product information");
            progressBar.setVisibility(View.GONE);
        }
    }
}
