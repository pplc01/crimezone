package com.ppl.crimezone.classes;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by adesudiman on 4/15/2014.
 */
public class ReportController {


    public static CrimeReport fetchReportDetail(String reportId){

        String url;

        url = "http://crimezone.besaba.com/webservice/crimeDetail.php?"
                +"reportId="+reportId;
        Log.d("String url", url);

        CrimeReport detailReport = null;
        //execute search
        HttpClient placesClient = new DefaultHttpClient();
        try {
            //try to fetch the data
            HttpGet placesGet = new HttpGet(url);
            HttpResponse placesResponse = placesClient.execute(placesGet);
            StatusLine placeSearchStatus = placesResponse.getStatusLine();
            Log.d("Status Code Connection ", placeSearchStatus.getStatusCode()+"");
            if (placeSearchStatus.getStatusCode() == 200) {
                //we have an OK response
                HttpEntity entity = placesResponse.getEntity();
                InputStream content = entity.getContent();

                try {
                    //Read the server response and attempt to parse it as JSON
                    Reader reader = new InputStreamReader(content);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.setDateFormat("d/MM/yyyy HH:mm");
                    Gson gson = gsonBuilder.create();
                    Log.d("gson : ", "sebelum gson from json");
                    List<CrimeReport> reports = new ArrayList<CrimeReport>();
                    reports = Arrays.asList(gson.fromJson(reader, CrimeReport[].class));
                    for(CrimeReport report:reports){
                        detailReport = report;
                    }
                    if(reports == null) Log.d("reportnull", "null");
                    content.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("Exception", "Error " +ex);
                }
            }
        }
        catch(Exception e){
            Log.d("Error", e.toString());
            e.printStackTrace();
        }

        return detailReport;
    }

    private class RatingTransfer{
        @SerializedName("rate_value")
        double rateScore;
    }


    public static Double updateRating(String username, String reportID, String rateVal){
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://crimezone.besaba.com/webservice/inUpRateValue.php");
        String message = "";
        Log.d("updateRating", username+" "+ reportID + " "+ rateVal);

        Double newRating = null;
        try
        {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("username", username));
            nameValuePairs.add(new BasicNameValuePair("reportID", reportID));
            nameValuePairs.add(new BasicNameValuePair("rateVal", rateVal));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            message =  response.getStatusLine().getStatusCode()+"";
            StatusLine placeSearchStatus = response.getStatusLine();
            Log.d("Status Code Connection ", placeSearchStatus.getStatusCode() + "");
            if (placeSearchStatus.getStatusCode() == 200) {
                //we have an OK response
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();

                try {
                    //Read the server response and attempt to parse it as JSON
                    Reader reader = new InputStreamReader(content);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.setDateFormat("d/MM/yyyy HH:mm");
                    Gson gson = gsonBuilder.create();
                    Log.d("gson : ", "sebelum gson from json");

                    RatingTransfer temp = gson.fromJson(reader, RatingTransfer.class);
                    newRating = new Double(temp.rateScore);
                    if(newRating != null){
                        Log.d("new rating", newRating.toString());

                    }else {
                        Log.d("rating null", "null");
                    }
                    content.close();
                } catch (Exception ex) {
                    Log.e("Exception", "Error " + ex);
                    //failedLoadingPosts();
                }

            }
        }
        catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return newRating;
    }
}
