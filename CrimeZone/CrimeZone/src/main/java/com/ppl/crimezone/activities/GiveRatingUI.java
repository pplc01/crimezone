package com.ppl.crimezone.activities;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.ReportController;
import com.ppl.crimezone.classes.User;

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
public class GiveRatingUI extends Dialog implements
        android.view.View.OnClickListener{
    Activity detail;
    ImageButton star1, star2,star3,star4,star5;
    Button submitRating, cancelRating;
    double ratingScore;

    public GiveRatingUI(Activity a){
        super(a);
        this.detail = a;
        ratingScore = 0;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.star1:
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_nostar);
                star3.setImageResource(R.drawable.r_nostar);
                star4.setImageResource(R.drawable.r_nostar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore = 1;
                break;
            case R.id.star2:
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_nostar);
                star4.setImageResource(R.drawable.r_nostar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore =2;
                break;
            case R.id.star3:
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_yesstar);
                star4.setImageResource(R.drawable.r_nostar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore= 3;
                break;
            case R.id.star4:
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_yesstar);
                star4.setImageResource(R.drawable.r_yesstar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore = 4;
                break;
            case R.id.star5:
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_yesstar);
                star4.setImageResource(R.drawable.r_yesstar);
                star5.setImageResource(R.drawable.r_yesstar);
                ratingScore = 5;
                break;
            case R.id.submitrate:
                dismiss();
                break;
            case R.id.cancelrate:
                dismiss();
            default:
                break;
        }
        dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_give_rating);
        setTitle("Give Rating");
        star1 = (ImageButton) findViewById(R.id.star1);
        star2 = (ImageButton) findViewById(R.id.star2);
        star3 = (ImageButton) findViewById(R.id.star3);
        star4 = (ImageButton) findViewById(R.id.star4);
        star5 = (ImageButton) findViewById(R.id.star5);
        submitRating = (Button) findViewById(R.id.submitrate);
        cancelRating = (Button) findViewById(R.id.cancelrate);
        submitRating.setOnClickListener(this);
        cancelRating.setOnClickListener(this);
        star1.setOnClickListener(this);
        star2.setOnClickListener(this);
        star3.setOnClickListener(this);
        star4.setOnClickListener(this);
        star5.setOnClickListener(this);
    }

    public double getNewRating(){
        return ratingScore;
    }
}