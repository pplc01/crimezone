package com.ppl.crimezone.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by adesudiman on 4/13/2014.
 */
public class MiniCrimeReport {

    private String title;
    @SerializedName("x_coordinate")
    private double latitude;
    @SerializedName("y_coordinate")
    private double longitude;
    @SerializedName("time_start")
    private Date crimeTimeStart;
    @SerializedName("CategoryName")
    private List<String> categories;


    public MiniCrimeReport(String title, Date start, List<String> categories, double latitude, double longitude){
        this.title = title;
        crimeTimeStart = start;
        this.categories = categories;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public MiniCrimeReport(String title, Date start, double latitude, double longitude){
        this.title = title;
        crimeTimeStart = start;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCrimeTimeStart(){
        return crimeTimeStart;
    }
    public void setCrimeTimeStart(Date start) {
        this.crimeTimeStart = start;
    }


    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }


    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getCategories(){
        return categories;
    }
    public void setCategories(List<String> categories){
        this.categories = categories;
    }


    public String printCategories(){
        String result = "";
        for(String item: categories){
            result += item +"|";
        }
        return result;
    }

    @Override
    public String toString(){
        String result = "";
        result = "title : "+title + "," + "latitude : "+ latitude+ "longitude :"+ longitude+", Date : "+ crimeTimeStart.toString()+ ", categories : "+ printCategories();
        return result;
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof MyClass))return false;
        MiniCrimeReport otherReport = (MiniCrimeReport)other;
        if(this.getLatitude() == otherReport.getLatitude() && this.getLongitude()== otherReport.getLongitude()) return true;
        return false;
    }
}

