package com.mcmah113.mcmah113expensesiq;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class FixerCurrencyAPI extends AsyncTask<String, Void, HashMap<String, String>> {
    FixerCurrencyAPI() {

    }

    protected HashMap<String, String> doInBackground(String... params) {
        //all currency conversions are in terms of EUR.
        //convert the two sent from the caller
        //using a HashMap so order doesn't matter
        StringBuilder httpURL = new StringBuilder("http://data.fixer.io/api/latest?access_key=");
        final String apiKey = "c5dfca79fff6a6a28e46260f1f3bf9bf";

        httpURL.append(apiKey);
        httpURL.append("&symbols=");

        for(int i = 0; i < params.length; i ++) {
            if(i == params.length -1) {
                //last one or only one
                httpURL.append(params[i]);
            }
            else {
                httpURL.append(params[i]).append(",");
            }
        }

        httpURL.append("&format=1");

        Log.d("HTTP REQUEST", httpURL.toString());

        try {
            URL url = new URL(httpURL.toString());

            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            final InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());

            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String inputLine;
            StringBuilder data = new StringBuilder();

            while((inputLine = bufferedReader.readLine()) != null) {
                data.append(inputLine).append("\n");
            }

            bufferedReader.close();
            inputStreamReader.close();
            urlConnection.disconnect();

            Log.d("HTTP REQUEST","SUCCESSFULLY COMMUNICATED WITH SERVER");

            return parseJSON(data.toString(), params);
        }
        catch(Exception exception) {
            Log.d("HTTP REQUEST FAILED", "Failed to make api request from data.fixer.io");
            Log.d("HTTP REQUEST", httpURL.toString());
            exception.printStackTrace();
            return null;
        }
    }

    private HashMap<String, String> parseJSON(String dataJSON, String[] locales) {
        if(dataJSON != null) {
            Log.d("HTTP REQUEST", dataJSON);
            Log.d("HTTP REQUEST", "SUCCESSFULLY RETRIEVED DATA FROM SERVER");

            //now these locales from the API call are in the same order as the array
            try {
                if(new JSONObject(dataJSON).getBoolean("success")) {
                    final JSONObject jsonRates = new JSONObject(dataJSON).getJSONObject("rates");

                    //+1 for the date
                    HashMap<String, String> exchangeRates = new HashMap<>();

                    exchangeRates.put("Date", new JSONObject(dataJSON).getString("date"));

                    for(String locale : locales) {
                        exchangeRates.put(locale, jsonRates.getString(locale));
                    }

                    Log.d("JSON PARSE","SUCCESSFULLY PARSED JSON");

                    return exchangeRates;
                }
                else {
                    Log.d("JSON PARSE","API CALL REJECTED BY SERVER");
                    return null;
                }
            }
            catch(Exception e) {
                Log.d("JSON PARSE","FAILED TO PARSE JSON");
                e.printStackTrace();
                return null;
            }
        }
        else {
            Log.d("GET RESULT","COULD NOT GET RESULT, VARIABLE IS <<NULL>>");
            return null;
        }
    }

    protected void onPostExecute(HashMap<String, String> exchangeRate) {

    }
}