package com.ppl.crimezone.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.Date;


/**
 * Created by adesudiman on 4/6/2014.
 */
public class CrimeReport {

    private int idReport;
    @SerializedName("data_created")
    private Date reportDate;
    private String description;
    private double avgScore;

    private String title;

    @SerializedName("time_start")
    private Date crimeTimeStart;
    @SerializedName("time_end")
    private Date crimeTimeEnd;
    private String [] categories;

    private Date crimeDate;
    @SerializedName("x_coordinate")
    private double latitude;
    @SerializedName("y_coordinate")
    private double longitude;

    public CrimeReport(int idReport, String title, Date reportDate, Date start, Date end, String description, String[] categories, double latitude, double longitude, double avgScore){
        this.title = title;
        crimeDate = start;

        this.categories = categories;
        this.latitude = latitude;
        this.longitude = longitude;
        this.idReport = idReport;
        this.reportDate = reportDate;
        this.description = description;
        this.avgScore = avgScore;
    }

    public CrimeReport(){

    }

    public int getIdReport(){
        return idReport;
    }

    public Date getReportDate(){
        return reportDate;
    }

    public String getDescription(){
        return description;
    }

    public double getAvgScore(){
        return avgScore;
    }

    public void setAvgScore(double newAvgScore){
        avgScore = newAvgScore;
    }

    public String getTitle(){
        return title;
    }

    public Date getCrimeDateStart(){
        return crimeTimeStart;
    }

    public Date getCrimeDateEnd(){
        return crimeTimeEnd;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public String[] getCategories(){
        return categories;
    }

}
