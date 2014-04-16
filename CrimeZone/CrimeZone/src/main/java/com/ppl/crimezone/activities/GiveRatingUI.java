package com.ppl.crimezone.activities;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by adesudiman on 4/15/2014.
 */
public class GiveRatingUI extends Dialog{
    ImageButton star1 ;
    ImageButton star2 ;
    ImageButton star3 ;
    ImageButton star4 ;
    ImageButton star5 ;
    Button submitRating;
    Button cancelRating;

    double ratingScore;


    public void initView(){
        this.setContentView(R.layout.fragment_give_rating);
        this.setTitle("Give Rating");
        ratingScore = 0;
    }



    public void initStart(){
        star1 = (ImageButton) this.findViewById(R.id.star1);
        star2 = (ImageButton) this.findViewById(R.id.star2);
        star3 = (ImageButton) this.findViewById(R.id.star3);
        star4 = (ImageButton) this.findViewById(R.id.star4);
        star5 = (ImageButton) this.findViewById(R.id.star5);
        submitRating = (Button) this.findViewById(R.id.submitrate);
        cancelRating = (Button) this.findViewById(R.id.cancelrate);
        submitRating = (Button) this.findViewById(R.id.submitrate);
        cancelRating = (Button) this.findViewById(R.id.cancelrate);

    }



    public setStarOnClickListener(){
        star1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_nostar);
                star3.setImageResource(R.drawable.r_nostar);
                star4.setImageResource(R.drawable.r_nostar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore = 1;
            }
        });

        star2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_nostar);
                star4.setImageResource(R.drawable.r_nostar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore =2;
            }
        });

        star3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_yesstar);
                star4.setImageResource(R.drawable.r_nostar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore= 3;}
        });
        star4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_yesstar);
                star4.setImageResource(R.drawable.r_yesstar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore = 4;
            }
        });
        star5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_yesstar);
                star4.setImageResource(R.drawable.r_yesstar);
                star5.setImageResource(R.drawable.r_yesstar);
                ratingScore = 5;
            }
        });
    }


    public void submitOnClickListener(){
        submitRating.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View v){

            }
        });
    }
}
