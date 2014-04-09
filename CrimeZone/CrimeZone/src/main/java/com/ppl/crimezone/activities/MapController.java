package com.ppl.crimezone.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.annotations.SerializedName;
import com.ppl.crimezone.R;
import com.ppl.crimezone.model.CrimeReport;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.google.maps.android.SphericalUtil;
/**
 com.google.maps.android:android-maps-utils
 * This is controller for HomeMap UI
 */
public class MapController extends ActionBarActivity {


    //the map variable
    private GoogleMap map = null;
    private Location location;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Marker[] placeMarkers;
    private final int MAX_PLACES = 20;
    private MarkerOptions[] places;

    //data variable
    private CrimeReport[] locationList;

    //for filter
    private ArrayList<CrimeReport> filterList = new ArrayList<CrimeReport>();
    AutoCompleteTextView searchLocation;
    DownloadTask placesDownloadTask;
    DownloadTask placeDetailsDownloadTask;
    ParserTask placesParserTask;
    ParserTask placeDetailsParserTask;

    final int PLACES=0;
    final int PLACES_DETAILS=1;

    boolean dateFilter;
    boolean crimeTypeFilter;

    Date dateStart;
    Date dateEnd;

    boolean crimeCategories[] = new boolean[8];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_map_ui);

        placeMarkers = new Marker[MAX_PLACES];

        setUpMap();

        setUpSearchLocation();

    }

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
    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //updateLocationUser();

        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
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

    //method for action bar moving to other activities
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

    //initialize map
    private void setUpMap(){
        if(map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMyLocationEnabled(true);
        }
    }
    //add listener if user swap or zoom the map
    private void setUpMapListener(){
        map.setOnCameraChangeListener
                (   new GoogleMap.OnCameraChangeListener()
                    {
                        @Override
                        public void onCameraChange(CameraPosition cameraPosition)
                        {
                            String latitude = String.valueOf(cameraPosition.target.latitude);
                            String longitude = String.valueOf(cameraPosition.target.latitude);
                            LatLng far = map.getProjection().getVisibleRegion().farLeft;
                            double distance = SphericalUtil.computeDistanceBetween(far, cameraPosition.target);
                            updateCrimeMarker(latitude, longitude, distance);
                        }
                    }
                );


    }

    //set the map to point to current user location
    private void updateLocationUser(){
        locationListener = new LocationListener() {
            public void onLocationChanged(Location userLocation) {
                // Called when a new location is found by the network location provider.
                location = userLocation;

                LatLng target = new LatLng(location.getLatitude(), location.getLongitude());

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(target)      // Sets the center of the map to Mountain View
                        .zoom(15)                   // Sets the zoom level
                        .bearing(0)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


                LatLng far = map.getProjection().getVisibleRegion().farLeft;
                double distance = SphericalUtil.computeDistanceBetween(far,  target);
                String latVal=String.valueOf(location.getLatitude());
                String lngVal=String.valueOf(location.getLongitude());
                updateCrimeMarker(latVal, lngVal, distance );
                locationManager.removeUpdates(locationListener);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
    }

    private void updateCrimeMarker(String latitude, String longitude, double distance){
        String url;
        try {
            url = "http:/http://crimezone.besaba.com/webservice/crimeLocation.php?distance="
                    + URLEncoder.encode(String.valueOf(distance), "UTF-8")
                    +"&latitude"
                    +URLEncoder.encode(latitude, "UTF-8")
                    +"&longitudee="
                    +URLEncoder.encode(longitude, "UTF-8");
            new GetCrimeReport().execute(url);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //initialize autocomplete view
    private void setUpSearchLocation(){
        if(searchLocation == null) {
            searchLocation = (AutoCompleteTextView) findViewById(R.id.atv_places);
            searchLocation.setThreshold(1);
            setUpSearchLocationListener();
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


    //fetch and parse crime report data
    private class GetCrimeReport extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... placesURL) {
            StringBuilder placesBuilder = new StringBuilder();
            //process search parameter string(s)
            for (String placeSearchURL : placesURL) {
                //execute search
                HttpClient placesClient = new DefaultHttpClient();
                try {
                    //try to fetch the data
                    HttpGet placesGet = new HttpGet(placeSearchURL);
                    HttpResponse placesResponse = placesClient.execute(placesGet);
                    StatusLine placeSearchStatus = placesResponse.getStatusLine();
                    Log.d("Status Code Connection ", placeSearchStatus.getStatusCode()+"");
                    if (placeSearchStatus.getStatusCode() == 200) {
                        //we have an OK response
                        HttpEntity placesEntity = placesResponse.getEntity();
                        InputStream placesContent = placesEntity.getContent();
                        InputStreamReader placesInput = new InputStreamReader(placesContent);
                        BufferedReader placesReader = new BufferedReader(placesInput);
                        String lineIn;
                        while ((lineIn = placesReader.readLine()) != null) {
                            placesBuilder.append(lineIn);
                        }
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            return placesBuilder.toString();
        }
        @Override
        protected void onPostExecute(String result) {
            Log.d("Result on Post: ", result);
            //parse place data returned from Google Places
            if(placeMarkers!=null){
                for(int pm=0; pm<placeMarkers.length; pm++){
                    if(placeMarkers[pm]!=null)
                        placeMarkers[pm].remove();
                }
            }
            try {
                //parse JSON
                JSONObject resultObject = new JSONObject(result);
                JSONArray placesArray = resultObject.getJSONArray("results");
                places = new MarkerOptions[placesArray.length()];

                boolean missingValue=false;
                LatLng placeLL=null;
                String placeName="";
                String vicinity="";
                int currIcon = R.drawable.mk_sxassault;

                    Log.d("Length ", placesArray.length()+"");

                    //loop through places
                    for (int p=0; p<placesArray.length(); p++) {
                        try{
                                //attempt to retrieve place data values

                            //parse each place
                            missingValue=false;
                            JSONObject placeObject = placesArray.getJSONObject(p);
                            JSONObject loc = placeObject.getJSONObject("geometry").getJSONObject("location");
                            placeLL = new LatLng(
                                    Double.valueOf(loc.getString("lat")),
                                    Double.valueOf(loc.getString("lng")));
                            JSONArray types = placeObject.getJSONArray("types");
                            for(int t=0; t<types.length(); t++){
                                String thisType=types.get(t).toString();   //what type is it
                                if(thisType.contains("food")){
                                    currIcon = R.drawable.mk_burglary;
                                    break;
                                }
                                else if(thisType.contains("bar")){
                                    currIcon = R.drawable.mk_drugs;
                                    break;
                                }
                                else if(thisType.contains("store")){
                                    currIcon = R.drawable.mk_kidnap;
                                    break;
                                }
                            }
                            vicinity = placeObject.getString("vicinity");
                            placeName = placeObject.getString("name");
                            }
                        catch(JSONException jse){
                            missingValue=true;
                            jse.printStackTrace();
                        }
                        if(missingValue)places[p]=null;
                        else
                            places[p]=new MarkerOptions()
                                    .position(placeLL)
                                    .title(placeName)
                                    .icon(BitmapDescriptorFactory.fromResource(currIcon))
                                    .snippet(vicinity);
                    }


            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if(places!=null && placeMarkers!=null){
                for(int p=0; p<places.length && p<placeMarkers.length; p++){
                    //will be null if a value was missing
                    if(places[p]!=null)
                        placeMarkers[p]=map.addMarker(places[p]);
                }
            }
        }
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

                    LatLng point = new LatLng(latitude, longitude);

                    CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(point);
                    CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(5);

                    // Showing the user input location in the Google Map
                    map.moveCamera(cameraPosition);
                    map.animateCamera(cameraZoom);

                    MarkerOptions options = new MarkerOptions();
                    options.position(point);
                    options.title("Position");
                    options.snippet("Latitude:"+latitude+",Longitude:"+longitude);

                    // Adding the marker in the Google Map
                    map.addMarker(options);

                    break;
            }
        }
    }

    private class Data {

        String title;

        @SerializedName("time_start")
        Date crimeDateStart;
        @SerializedName("time_end")
        Date crimeDateEnd;
        String [] categories;

        @SerializedName("x_coordinate")
        double latitude;
        @SerializedName("y_coordinate")
        double longitude;

        public Data(String title, Date start, Date end,String[] categories, double lat, double lang){
            this.title = title;
            crimeDateStart = start;
            crimeDateEnd = end;
            this.categories = categories;
            latitude = lat;
            longitude = lang;

        }


        public String getTitle(){
            return title;
        }

        public Date getCrimeDateStart(){
            return crimeDateStart;
        }

        public Date getCrimeDateEnd(){
            return crimeDateEnd;
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
}