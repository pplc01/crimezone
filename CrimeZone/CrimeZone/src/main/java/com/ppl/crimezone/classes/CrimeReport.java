package com.ppl.crimezone.classes;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by adesudiman on 4/6/2014.
 */
public class CrimeReport{

    private String username;
    @SerializedName("reportID")
    private int idReport;
    @SerializedName("data_created")
    private Date reportDate;
    private String description;
    @SerializedName("avg_rating")
    private double avgScore;
    private String title;
    @SerializedName("time")
    private Date crimeTime;
    @SerializedName("CategoryName")
    private List<String> categories;

    @SerializedName("x_coordinate")
    private double latitude;
    @SerializedName("y_coordinate")
    private double longitude;

    public CrimeReport(int idReport, String title, Date reportDate, Date time,  String description, List<String> categories, double latitude, double longitude, double avgScore){
        this.title = title;
        this.crimeTime= time;
        this.categories = categories;
        this.latitude = latitude;
        this.longitude = longitude;
        this.idReport = idReport;
        this.reportDate = reportDate;
        this.description = description;
        this.avgScore = avgScore;
    }

    public CrimeReport(int idReport, String title, Date time, List<String> categories, double latitude, double longitude){
        this.idReport = idReport;
        this.title = title;
        this.crimeTime = time;
        this.categories = categories;
        this.latitude = latitude;
        this.longitude = longitude;
        this.idReport = idReport;
    }
    public CrimeReport(){
    }

    public void setCrimeTime(long miliseconds){
        crimeTime.setTime(miliseconds);
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

    public Date getCrimeTime(){
        return crimeTime;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public List<String> getCategories(){
        return categories;
    }

    public String printCategories(){
        String result="";
        for(String x:categories){
            result+= x+",";
        }
        return result;
    }

    @Override
    public String toString(){
        return getIdReport()+" "+ getReportDate()+" "+ getDescription()+" "+ getAvgScore()+" " + getCrimeTime()+" "+" "+getLatitude()+" "+getLongitude()+ " "+ printCategories();
    }

    public String getUsername(){
        return username;
    }
}
