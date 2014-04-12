package com.ppl.crimezone.activities;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ajiutamaa on 3/30/2014.
 */

public class GsonParser {
    static InputStream is = null;
    static JSONObject obj = null;
    static String json = null;


    public JSONObject getJSONfromURL(final String url){
        try{
            //Construct client and HTTP request
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            //Execute POST request and store response locally
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            is = httpEntity.getContent();

        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        } catch(ClientProtocolException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = reader.readLine()) != null){
                sb.append(line + "\n");
            }

            is.close();
            json = sb.toString();

        }catch(Exception e){
            Log.e("Buffer error", "Error converting result" + e.toString());
        }

        //Parse String to JSON Object
        try{
            obj = new JSONObject(json);
        }catch(Exception e){
            Log.e("Parsing error", "Error parsing to JSON" + e.toString());
        }
        return obj;
    }

    public JSONObject makeHTTPRequest(String url, String method, List<NameValuePair> params){
        Log.d("Masuk", "Mulai");
        try{
            if(method.equalsIgnoreCase("POST")){
                //Request method POST
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }
            else if(method.equalsIgnoreCase("GET")){
                //Request method GET
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }catch(ClientProtocolException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = reader.readLine()) != null){
                sb.append(line + "\n");
            }

            is.close();
            json = sb.toString();

        }catch(Exception e){
            Log.e("Buffer error", "Error converting result" + e.toString());
        }

        //Parse String to JSON Object
        try{
            obj = new JSONObject(json);
        }catch(Exception e){
            Log.d("json", json);
            Log.e("Parsing error", "Error parsing to JSON" + e.toString());
        }
        return obj;
    }

    public static List<HashMap<String,String>> parseDetailPlace(JSONObject jObject){

        Double lat = Double.valueOf(0);
        Double lng = Double.valueOf(0);

        HashMap<String, String> hm = new HashMap<String, String>();
        List<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();

        try {

            lat = (Double)jObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lat");
            lng = (Double)jObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lng");

        } catch (JSONException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        hm.put("lat", Double.toString(lat));
        hm.put("lng", Double.toString(lng));

        list.add(hm);
        return list;
    }


    /** Receives a JSONObject and returns a list */
    public static List<HashMap<String,String>> parsePlace(JSONObject jObject){

        JSONArray jPlaces = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("predictions");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getPlaces(jPlaces);
    }

    private static List<HashMap<String, String>> getPlaces(JSONArray jPlaces){
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> place = null;

        /** Taking each place, parses and adds to list object */
        for(int i=0; i<placesCount;i++){
            try {
                /** Call getPlace with place JSON object to parse the place */
                place = getPlace((JSONObject)jPlaces.get(i));
                placesList.add(place);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    /** Parsing the Place JSON object */
    private static HashMap<String, String> getPlace(JSONObject jPlace){

        HashMap<String, String> place = new HashMap<String, String>();

        String id="";
        String reference="";
        String description="";

        try {
            description = jPlace.getString("description");
            id = jPlace.getString("id");
            reference = jPlace.getString("reference");

            place.put("description", description);
            place.put("_id",id);
            place.put("reference",reference);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }


    /** A method to download json data from url */
    protected static String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    protected static String getAutoCompleteUrl(String place){

        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=AIzaSyCP3fwzdW9BzrPrtAInLCgFUNSpIJrlgZo";

        // place to be be searched
        String input = "input="+place;

        // place type to be searched
        String types = "types=geocode";

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = input+"&"+types+"&"+sensor+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

        return url;
    }

    protected static String getPlaceDetailsUrl(String ref){

        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=AIzaSyCP3fwzdW9BzrPrtAInLCgFUNSpIJrlgZo";

        // reference of place
        String reference = "reference="+ref;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = reference+"&"+sensor+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/details/"+output+"?"+parameters;

        return url;
    }
}
