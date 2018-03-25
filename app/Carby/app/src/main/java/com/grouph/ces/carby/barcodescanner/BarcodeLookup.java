package com.grouph.ces.carby.barcodescanner;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.grouph.ces.carby.nutrition_data.INutritionTable;
import com.grouph.ces.carby.nutrition_data.NutritionTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by George on 15/02/2018.
 * <p>
 * Class for the OpenFoodFacts look-up of scanned barcode
 */

public class BarcodeLookup extends AsyncTask<Barcode, Void, INutritionTable> {

    @Override
    protected INutritionTable doInBackground(Barcode... barcode) {
        String DataURL = "https://world.openfoodfacts.org/api/v0/product/";
        DataURL += barcode[0].displayValue + ".json";
        //DataURL += "50457236.json";

        try {
            URL url = new URL(DataURL);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            root = root.getAsJsonObject(); //May be an array, may be an object.

            int status = root.getAsJsonObject().get("status").getAsInt();

            if (status == 0) {
                return null;
            } else {
                if (root != null) {
                    try {
                        JsonElement product = root.getAsJsonObject().get("product");
                        JsonElement nutrients = product.getAsJsonObject().get("nutriments");

                        String productName = product.getAsJsonObject().get("product_name").toString().replace("\"", "");
                        String servingSize = product.getAsJsonObject().get("serving_size").toString().replace("\"", "");

                        double energy100g = getNutrientValue(nutrients, "energy_100g");

                        double fat100g = getNutrientValue(nutrients, "fat_100g");
                        double monoUnsatFat100g = getNutrientValue(nutrients, "monounsaturated-fat_100g");
                        double polyUnsatFat100g = getNutrientValue(nutrients, "polyunsaturated-fat_100g");
                        double saturatedFat100g = getNutrientValue(nutrients, "satured-fat_100g");

                        double carbohydrate100g = getNutrientValue(nutrients, "carbohydrates_100g");
                        double sugars100g = getNutrientValue(nutrients, "sugars_100g");
                        double polyols100g = getNutrientValue(nutrients, "polyols_100g");
                        double starch100g = getNutrientValue(nutrients, "starch_100g");

                        double fibre100g = getNutrientValue(nutrients, "fiber_100g");
                        double protein100g = getNutrientValue(nutrients, "proteins_100g");
                        double salt100g = getNutrientValue(nutrients, "salt_100g");

                        INutritionTable nutritionTable = new NutritionTable();
                        if(energy100g != -1.0) {
                            nutritionTable.setComponent("Energy", energy100g);
                        }
                        if(fat100g!=-1.0){
                            nutritionTable.setComponent("Fat", fat100g);
                        }
                        if(monoUnsatFat100g!=-1.0) {
                            nutritionTable.setComponent("mono-unsaturates", monoUnsatFat100g);
                        }
                        if(polyUnsatFat100g!=-1.0) {
                            nutritionTable.setComponent("polyunsaturates", polyUnsatFat100g);
                        }
                        if(saturatedFat100g!=-1.0) {
                            nutritionTable.setComponent("saturates", saturatedFat100g);
                        }
                        if(carbohydrate100g!=-1.0) {
                            nutritionTable.setComponent("Carbohydrate", carbohydrate100g);
                        }

                        if(sugars100g!=-1.0) {
                            nutritionTable.setComponent("sugars", sugars100g);
                        }
                        if(polyols100g!=-1.0) {
                            nutritionTable.setComponent("polyols", polyols100g);
                        }

                        if(starch100g>=0) {
                            nutritionTable.setComponent("starch", starch100g);
                        }

                        if(fibre100g!=-1.0) {
                            nutritionTable.setComponent("Fibre", fibre100g);
                        }
                        if(protein100g!=-1.0) {
                            nutritionTable.setComponent("Protein", protein100g);
                        }
                        if(salt100g!=-1.0) {
                            nutritionTable.setComponent("Salt", salt100g);
                        }

                        return nutritionTable;

                    } catch (NumberFormatException e) {
                        Log.e("Tag", "This is not good, product may be missing information: " + e.toString() + "\n");
                        return null;
                    }
                } else {
                    return null;
                }
            }

        } catch (IOException e) {
            Log.e("Tag", "Likely could not connect to internet/server: " + e.toString());
        }
        return null;
    }

    /* Returns -1.0 if null value */
    private double getNutrientValue(JsonElement nutrients, String jsonName) {
        JsonElement nullableText = nutrients.getAsJsonObject().get(jsonName);
        return (nullableText == null) ? -1 : Double.parseDouble(nullableText.toString().replace("\"", ""));
    }

    @Override
    protected void onPostExecute(INutritionTable nutritionTable) {
        super.onPostExecute(nutritionTable);
    }
}
