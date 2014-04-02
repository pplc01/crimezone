package com.ppl.crimezone.activities;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ppl.crimezone.R;
import com.ppl.crimezone.model.CrimeReport;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This is controller for HomeMap UI
 */
public class MapController extends ActionBarActivity {

    //the map
    private GoogleMap map = null;

    private LatLng location;

    private Location locs;
    //provide the data
    LocationManager locationManager;

    private CrimeReport[] locationList;

    //for filter
    private ArrayList<CrimeReport> filterList = new ArrayList<CrimeReport>();

    boolean dateFilter;
    boolean crimeTypeFilter;

    Date dateStart;
    Date dateEnd;

    boolean crimeTypeBugrlar;
    boolean crimeTypeHomicide;
    boolean crimeTypeKidnap;
    boolean crimeTypeSexAssaust;
    boolean crimeTypeTheft;
    boolean crimeTypeVehicleTheft;
    boolean crimeTypeViolence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_map_ui);


        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);

    }

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location loc) {
            // Called when a new location is found by the network location provider.
            locs = loc;
            double lat =  locs.getLatitude();
            double lng = locs.getLongitude();
            location = new LatLng(lat, lng);

            map.addMarker(new MarkerOptions()
                            .position(location)
                            .title("Marker")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.mk_burglary))
            );

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(location)      // Sets the center of the map to Mountain View
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            locationManager.removeUpdates(locationListener);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };


    //set up the action bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_controller_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //set up the response for action bar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_report:
                openReport();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openReport()
    {

    }


    public void openSettings()
    {

    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

}
