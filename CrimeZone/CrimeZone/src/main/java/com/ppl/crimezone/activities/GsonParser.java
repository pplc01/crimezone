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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
            Log.e("Parsing error", "Error parsing to JSON" + e.toString());
        }
        return obj;
    }
}
