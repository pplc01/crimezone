package com.ppl.crimezone.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.ReportController;
import com.ppl.crimezone.classes.CrimeReport;


public class DetailReportUI extends FragmentActivity implements View.OnClickListener  {

    TextView title, time, author, description, locationDescription;
    LinearLayout starContainer, crimeTypeContainer;
    CrimeReport detailReport;
    ImageButton back, giveRate;
    private void initView(){
        title = (TextView) findViewById(R.id.headline_detail);
        time = (TextView) findViewById(R.id.time_detail);
        author = (TextView) findViewById(R.id.author);
        description = (TextView) findViewById(R.id.description_detail);
        locationDescription = (TextView) findViewById(R.id.latlang);
        crimeTypeContainer = (LinearLayout) findViewById(R.id.type_container);
        starContainer = (LinearLayout) findViewById(R.id.star_container);
        back = (ImageButton) findViewById(R.id.back_detail);
        giveRate = (ImageButton) findViewById(R.id.rate_detail);
        back.setOnClickListener(this);
        giveRate.setOnClickListener(this);
        detailReport = new CrimeReport();
    }
    GiveRatingUI rate;
    Double rateVal = null;
    @Override
    public void onClick(View v) {
       switch (v.getId()) {
            case R.id.back_detail:
                back.setImageResource(R.drawable.ic_launcher);
                String PREFS_NAME = "ReportLocation";
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("NotHome", true);
                editor.putString("latitude", detailReport.getLatitude()+ "");
                editor.putString("longitude", detailReport.getLongitude()+"");

                // Commit the edits!
                editor.commit();
                finish();
                break;
            case R.id.rate_detail:
                giveRate.setImageResource(R.drawable.r_yesstar);
                rate = new GiveRatingUI(DetailReportUI.this);
                rate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                rate.setOnDismissListener(new DialogInterface.OnDismissListener() {
                     @Override
                     public void onDismiss(DialogInterface dialog) {
                         if(rate.isSubmit()) {
                             new FetchRatingTask().execute();

                         }
                         giveRate.setImageResource(R.drawable.r_star);
                     }
                 });
                 rate.show();
                break;
            default:
                break;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_detail_ui);
        initView();
        getDetailReport();
    }

    private void getDetailReport(){
        new FetchReportTask().execute();
    }


    private void viewDetailReport()
    {
        //init UI
        title = (TextView) findViewById(R.id.headline_detail);
        if(title == null){
            Log.d("check null", "title");
        }
        if(detailReport == null){
            Log.d("check null", "detailReport");
        }
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




    private class FetchRatingTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... placesURL){
            String MYPREFERENCES = "UserAccount";
            SharedPreferences sharedPreferences = getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);

            String username = sharedPreferences.getString("usernameKey", "");
            rateVal =  ReportController.updateRating(username, detailReport.getIdReport() + "", rate.getNewRating() + "");
            if(rateVal != null){
                detailReport.setAvgScore(rateVal.doubleValue());
                handler2();
            }else {
                //failed
            }//drawStar();
            return "";
        }
    }
    private class FetchReportTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... placesURL){
            final String PREFS_NAME = "ReportLocation";

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

            double latitude = Double.parseDouble(settings.getString("latitude", "0"));
            double longitude = Double.parseDouble(settings.getString("longitude", "0"));
            Log.d("report lat long", latitude + ", "+ longitude);
            detailReport = ReportController.fetchReportDetail(latitude, longitude);
            handler();
            return "";
        }
    }

    public void handler(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               viewDetailReport();//Your code to run in GUI thread here
            }//public void run() {
        });
    }

    public void handler2(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawStar();
            }//public void run() {
        });
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