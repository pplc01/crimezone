package com.ppl.crimezone.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.DatePickerUI;
import com.ppl.crimezone.classes.GsonParser;
import com.ppl.crimezone.classes.ReportController;
import com.ppl.crimezone.classes.TimePickerDialogFragment;
import com.ppl.crimezone.classes.CrimeReport;

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

public class DetailReportUI extends FragmentActivity {

    TextView title;
    TextView time;
    TextView author;
    TextView description;
    LinearLayout starContainer;
    LinearLayout crimeTypeContainer;
    ImageButton back;
    ImageButton giveRating;
    TextView locationDescription;
    CrimeReport detailReport;

    private void init(){
        giveRating = (ImageButton) findViewById(R.id.b_rate);
        back = (ImageButton)findViewById(R.id.back_detail);
        title = (TextView) findViewById(R.id.headline_detail);
        time = (TextView) findViewById(R.id.time_detail);
        author = (TextView) findViewById(R.id.author);
        description = (TextView) findViewById(R.id.description_detail);
        locationDescription = (TextView) findViewById(R.id.latlang);
        crimeTypeContainer = (LinearLayout) findViewById(R.id.type_container);
    }

    private void backListener(){
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void giveRatingListener(){
        giveRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // custom dialog
                //dialog.dismiss();
                //Log.d("message", message);
                //send to server
            }
        });


    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_detail_ui);
        init();
        backListener();
        giveRatingListener();
        initDetailReport();
        handleDetailReportList();
    }

    private void initDetailReport(){
        final String PREFS_NAME = "ReportLocation";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        double latitude = Double.parseDouble(settings.getString("latitude", "0"));
        double longitude = Double.parseDouble(settings.getString("longitude", "0"));

        detailReport = ReportController.fetchReportDetail(latitude, longitude);

    }


    private void handleDetailReportList()
    {

        //init UI
        title.setText(detailReport.getTitle());
        String timeText = detailReport.getCrimeDateStart().getDate()+"/"+ (detailReport.getCrimeDateStart().getMonth()+1)+"/"+ (detailReport.getCrimeDateStart().getYear()+1900)+ " "+ detailReport.getCrimeDateStart().getHours()+":"+detailReport.getCrimeDateStart().getMinutes()+"->"+ detailReport.getCrimeDateEnd().getHours()+":"+detailReport.getCrimeDateEnd().getMinutes();
        time.setText(timeText);
        author.setText(detailReport.getUsername());
        description.setText(detailReport.getDescription());

            for(String x: detailReport.getCategories()){
                ImageView typeCrime = new ImageView(this);
                int idType = Integer.parseInt(x);
                switch (idType){
                    case 0:
                        typeCrime.setImageResource(R.drawable.ic_drugs);
                        break;
                    case 1:
                        typeCrime.setImageResource(R.drawable.ic_burglary);
                        break;
                    case 2:
                        typeCrime.setImageResource(R.drawable.ic_homicide);
                        break;
                    case 3:
                        typeCrime.setImageResource(R.drawable.ic_kidnap);
                        break;
                    case 4:
                        typeCrime.setImageResource(R.drawable.ic_sxassault);
                        break;
                    case 5:
                        typeCrime.setImageResource(R.drawable.ic_theft);
                        break;
                    case 6:
                        typeCrime.setImageResource(R.drawable.ic_vehicletheft);
                        break;
                    case 7:
                        typeCrime.setImageResource(R.drawable.ic_violence);
                        break;
                }
                crimeTypeContainer.addView(typeCrime);
            }

            locationDescription.setText("lat: "+detailReport.getLatitude()+ ", long "+ detailReport.getLongitude());

            drawStar();
    }


    private void drawStar()
    {
        starContainer.removeAllViewsInLayout();
        int x = (int)detailReport.getAvgScore();
        for(int ii=0; ii< x; ++ii){
            ImageView starii = new ImageView(this);
            starii.setImageResource(R.drawable.r_yesstar);
            if(ii==0){
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(90, 0, 0, 0);
                starii.setLayoutParams(lp);
            }
            starContainer.addView(starii);

        }
        if(Math.abs(detailReport.getAvgScore()-(x+0.5))< Math.abs(Math.round(detailReport.getAvgScore()) - detailReport.getAvgScore())){
            ImageView halfStar = new ImageView(this);
            halfStar.setImageResource(R.drawable.r_halfstar);
            starContainer.addView(halfStar);
        }else {
            if(Math.round(detailReport.getAvgScore()) == x+1){
                ImageView fullStar = new ImageView(this);
                fullStar.setImageResource(R.drawable.r_yesstar);
                starContainer.addView(fullStar);
            }
        }
    }
}