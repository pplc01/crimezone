package com.ppl.crimezone.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ppl.crimezone.R;
import com.ppl.crimezone.model.CrimeReport;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReportController extends ActionBarActivity {

    //variable for point to the map
    private GoogleMap reportMap = null;
    private LatLng location;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Marker placeMarker;
    private MarkerOptions place;


    //for filter
    AutoCompleteTextView searchLocation;
    DownloadTask placesDownloadTask;
    DownloadTask placeDetailsDownloadTask;
    ParserTask placesParserTask;
    ParserTask placeDetailsParserTask;

    final int PLACES=0;
    final int PLACES_DETAILS=1;

    public static final String PREFS_NAME = "ReporControllerMode";


    public boolean newReportMode = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        newReportMode = settings.getBoolean("NewReportMode", false);

        Log.d("Report Mode", newReportMode+"");


        if(newReportMode){
            setContentView(R.layout.report_form_ui);
            showReportForm();
        }else{

        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        // Inflate the menu items for use in the action bar
        if(newReportMode) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.new_report_menu_bar, menu);
            return super.onCreateOptionsMenu(menu);
        }
        else{
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.map_controller_actions, menu);
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_report:
                openReport();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openReport()
    {
        String PREFS_NAME = "ReporControllerMode";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("NewReportMode", true);

        // Commit the edits!
        editor.commit();

        Intent intent = new Intent(this, ReportController.class);
        startActivity(intent);
    }

    public void openSettings()
    {

    }


    public void showReportForm(){
        if(reportMap == null) {
            reportMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.new_report_map)).getMap();
            reportMap.setMyLocationEnabled(true);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            updateLocationUser();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            setUpSearchLocation();
        }
    }


    //set the map to point to current user location
    private void updateLocationUser(){
        locationListener = new LocationListener() {
            public void onLocationChanged(Location userLocation) {
                // Called when a new location is found by the network location provider.


                location = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(location)      // Sets the center of the map to Mountain View
                        .zoom(15)                   // Sets the zoom level
                        .bearing(0)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                reportMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


                if(placeMarker != null)placeMarker.remove();
                place = new MarkerOptions();
                place.position(location);
                place.title("Crime Location");
                place.snippet("Latitude:"+location.latitude+",Longitude:"+location.longitude);

                // Adding the marker in the Google Map
                placeMarker = reportMap.addMarker(place);

                locationManager.removeUpdates(locationListener);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
    }


    //initialize autocomplete view
    private void setUpSearchLocation(){
        if(searchLocation == null) {
            searchLocation = (AutoCompleteTextView) findViewById(R.id.crime_location);
            setUpSearchLocationListener();
            searchLocation.setThreshold(1);
        }
    }

    private void setUpSearchLocationListener(){
        // Adding textchange listener
        searchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Creating a DownloadTask to download Google Places matching "s"
                placesDownloadTask = new DownloadTask(PLACES);

                // Getting url to the Google Places Autocomplete api
                String url = GsonParser.getAutoCompleteUrl(s.toString());

                // Start downloading Google Places
                // This causes to execute doInBackground() of DownloadTask class
                placesDownloadTask.execute(url);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        // Setting an item click listener for the AutoCompleteTextView dropdown list
        searchLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long id) {

                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();

                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);

                // Creating a DownloadTask to download Places details of the selected place
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);

                // Getting url to the Google Places details api
                String url = GsonParser.getPlaceDetailsUrl(hm.get("reference"));

                // Start downloading Google Place Details
                // This causes to execute doInBackground() of DownloadTask class
                placeDetailsDownloadTask.execute(url);
            }
        });
    }




    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        private int downloadType=0;

        // Constructor
        public DownloadTask(int type){
            this.downloadType = type;
        }

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = GsonParser.downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            Log.d("Hasil Data ", data);
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            switch(downloadType){
                case PLACES:
                    // Creating ParserTask for parsing Google Places
                    placesParserTask = new ParserTask(PLACES);

                    // Start parsing google places json data
                    // This causes to execute doInBackground() of ParserTask class
                    placesParserTask.execute(result);
                    break;

                case PLACES_DETAILS :
                    // Creating ParserTask for parsing Google Places
                    placeDetailsParserTask = new ParserTask(PLACES_DETAILS);

                    // Starting Parsing the JSON string
                    // This causes to execute doInBackground() of ParserTask class
                    placeDetailsParserTask.execute(result);
            }
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        int parserType = 0;

        public ParserTask(int type){
            this.parserType = type;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<HashMap<String, String>> list = null;

            try{
                jObject = new JSONObject(jsonData[0]);

                switch(parserType){
                    case PLACES :
                        // Getting the parsed data as a List construct
                        list = GsonParser.parsePlace(jObject);
                        break;
                    case PLACES_DETAILS :

                        // Getting the parsed data as a List construct
                        list = GsonParser.parseDetailPlace(jObject);
                }

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            switch(parserType){
                case PLACES :
                    String[] from = new String[] { "description"};
                    int[] to = new int[] { android.R.id.text2 };



                    // Creating a SimpleAdapter for the AutoCompleteTextView
                    SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, R.layout.autocomplete, from, to);
                    // Setting the adapter
                    searchLocation.setAdapter(adapter);
                    break;
                case PLACES_DETAILS :
                    HashMap<String, String> hm = result.get(0);

                    // Getting latitude from the parsed data
                    double latitude = Double.parseDouble(hm.get("lat"));

                    // Getting longitude from the parsed data
                    double longitude = Double.parseDouble(hm.get("lng"));

                    // Getting reference to the SupportMapFragment of the activity_main.xml
                    SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

                    // Getting GoogleMap from SupportMapFragment

                    location = new LatLng(latitude, longitude);

                    CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(location);
                    CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(5);

                    // Showing the user input location in the Google Map
                    reportMap.moveCamera(cameraPosition);
                    reportMap.animateCamera(cameraZoom);

                    if(placeMarker != null)placeMarker.remove();


                    place = new MarkerOptions();
                    place.position(location);
                    place.title("Crime Location");
                    place.snippet("Latitude:"+location.latitude+",Longitude:"+location.longitude);

                    // Adding the marker in the Google Map
                    placeMarker = reportMap.addMarker(place);
                    break;
            }
        }
    }

    private boolean submitForm(){
        return false;
    }
}
