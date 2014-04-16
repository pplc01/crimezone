package com.ppl.crimezone.classes;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by adesudiman on 3/30/2014.
 */
public class crimeZone {
    //private List<DraggableCircle> mCircles = new ArrayList<DraggableCircle>(1);


    private int mStrokeColor;
    private int mFillColor = 100;
    private static final double DEFAULT_RADIUS = 1000000;
    public static final double RADIUS_OF_EARTH_METERS = 6371009;

    /** Generate LatLng of radius marker */
    private static LatLng toRadiusLatLng(LatLng center, double radius) {
        double radiusAngle = Math.toDegrees(radius / RADIUS_OF_EARTH_METERS) /
                Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

    private static double toRadiusMeters(LatLng center, LatLng radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                radius.latitude, radius.longitude, result);
        return result[0];
    }



}
