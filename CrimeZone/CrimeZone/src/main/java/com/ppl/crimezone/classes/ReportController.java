package com.ppl.crimezone.classes;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
import java.util.Calendar;
import java.util.List;

/**
 * Created by adesudiman on 4/15/2014.
 */
public class ReportController {


    public static String[] prepareNewReportData(Calendar time, Calendar submitTime,String username, String title, String description, LatLng location, boolean [] categories){
        String data[] = new String[7];
        data[0] = username;
        data[1] = title;
        String dateString = submitTime.get(Calendar.DATE)+"";
        if(submitTime.get(Calendar.DATE)<10)dateString = "0"+ dateString;
        String monthString = (submitTime.get(Calendar.MONTH)+1)+"";
        if((submitTime.get(Calendar.MONTH)+1)< 10)dateString = "0"+ monthString;
        String hourString = submitTime.get(Calendar.HOUR_OF_DAY)+"";
        if(submitTime.get(Calendar.HOUR_OF_DAY)<10)hourString= "0"+ hourString;
        String minuteString = submitTime.get(Calendar.MINUTE)+"";
        if(submitTime.get(Calendar.MINUTE )< 10)minuteString = "0"+ monthString;

        data[2] = dateString+ "/"+monthString+"/" + submitTime.get(Calendar.YEAR) + " " +hourString + ":"+minuteString;

        hourString = time.get(Calendar.HOUR_OF_DAY)+"";
        if(time.get(Calendar.HOUR_OF_DAY) < 10)hourString = "0" + hourString;
        minuteString = time.get(Calendar.MINUTE)+"";
        if(time.get(Calendar.MINUTE) < 10)minuteString = "0"+ minuteString;
        data[3] =  dateString+"/"+monthString+"/" + time.get(Calendar.YEAR) + " " + hourString+ ":"+ minuteString;

        data[4] = description;
        data[5] = location.latitude+"";
        data[6] = location.longitude+"";
        for(int jj=0; jj<8;  ++jj){
            data[jj+6] = categories[jj]+"";
        }
        return data;
    }
    public static String postData(String valueIWantToSend[]) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://crimezone.besaba.com/webservice/submitReport.php");
        String message = "";
        try
        {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("username", valueIWantToSend[0]));
            nameValuePairs.add(new BasicNameValuePair("title", valueIWantToSend[1]));
            nameValuePairs.add(new BasicNameValuePair("dreported", valueIWantToSend[2]));
            nameValuePairs.add(new BasicNameValuePair("time", valueIWantToSend[3]));
            nameValuePairs.add(new BasicNameValuePair("description", valueIWantToSend[5]));
            nameValuePairs.add(new BasicNameValuePair("lat", valueIWantToSend[6]));
            nameValuePairs.add(new BasicNameValuePair("long", valueIWantToSend[7]));
            if(valueIWantToSend[8].equals("true")){
                nameValuePairs.add(new BasicNameValuePair("crimetype1", 0+""));
            }
            if(valueIWantToSend[9].equals("true")) {
                nameValuePairs.add(new BasicNameValuePair("crimetype2", 1 + ""));
            }
            if(valueIWantToSend[10].equals("true")){
                nameValuePairs.add(new BasicNameValuePair("crimetype3", 2+""));
            }
            if(valueIWantToSend[11].equals("true")){
                nameValuePairs.add(new BasicNameValuePair("crimetype4", 3+""));
            }
            if(valueIWantToSend[12].equals("true")){
                nameValuePairs.add(new BasicNameValuePair("crimetype5", 4+""));
            }
            if(valueIWantToSend[13].equals("true")){
                nameValuePairs.add(new BasicNameValuePair("crimetype6", 5+""));
            }
            if(valueIWantToSend[14].equals("true")){
                nameValuePairs.add(new BasicNameValuePair("crimetype7", 6+""));
            }
            if(valueIWantToSend[15].equals("true")){
                nameValuePairs.add(new BasicNameValuePair("crimetype8", 7+""));
            }
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            message =  response.getStatusLine().getStatusCode()+"";
        }
        catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return message;
    }

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

                    CrimeReport updatedReport = gson.fromJson(reader, CrimeReport.class);
                    newRating = new Double(updatedReport.getAvgScore());
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