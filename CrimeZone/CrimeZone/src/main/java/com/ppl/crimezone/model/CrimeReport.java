package com.ppl.crimezone.model;

import java.util.ArrayList;
import java.util.Date;

import com.google.android.gms.internal.av;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
/**
 * Created by adesudiman on 3/29/2014.
 */
public class CrimeReport {


    private int idReport;
    private String title;
    private Date reportDate;
    private Date crimeDate;
    private String description;
    private String[] categories;
    private LatLng location;
    private double avgScore;

    public CrimeReport(int idReport, String title, Date reportDate, Date crimeDate, String description, String[] categories, double latitude, double langtitude, double avgScore){
        this.idReport = idReport;
        this.title = title;
        this.reportDate = reportDate;
        this.crimeDate = crimeDate;
        this.description = description;
        this.categories = categories;

        this.location = new LatLng(latitude, langtitude);
        this.avgScore = avgScore;
    }

    public int getIdReport(){
        return idReport;
    }

    public String getTitle(){
        return title;
    }

    public Date getReportDate(){
       return reportDate;
    }

    public String getDescription(){
        return description;
    }

    public LatLng getLocation(){
        return location;
    }

    public String[] getCategories(){
        return categories;
    }

    public double getAvgScore(){
        return avgScore;
    }

    public void setAvgScore(double newAvgScore){
        avgScore = newAvgScore;
    }
}
