package com.ppl.crimezone.activities;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ppl.crimezone.R;
import com.ppl.crimezone.model.CrimeReport;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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

/**
 * This is controller for HomeMap UI
 */
public class MapController extends ActionBarActivity {

    //the map
    private GoogleMap map = null;

    private LatLng location;

    private Location locs;
    //provide the data
    private LocationManager locationManager;

    private Marker[] placeMarkers;

    private final int MAX_PLACES = 20;

    private MarkerOptions[] places;

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

        placeMarkers = new Marker[MAX_PLACES];
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
                    .zoom(15)                   // Sets the zoom level
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            LatLng far = map.getProjection().getVisibleRegion().farLeft;
            //double distanceKM = computeDistanceBetween(location, far);
            String latVal=String.valueOf(lat);
            String lngVal=String.valueOf(lng);
            String url;
            try {
                url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                        + URLEncoder.encode(latVal, "UTF-8")
                        +","
                        +URLEncoder.encode(lngVal, "UTF-8")
                        +"&radius="
                        +URLEncoder.encode("1000", "UTF-8")
                        +"&sensor="
                        +URLEncoder.encode("true", "UTF-8")
                        +"&types="
                        +URLEncoder.encode("food|bar|store|museum|art_gallery", "UTF-8")
                        +"&key="
                        +URLEncoder.encode("AIzaSyCP3fwzdW9BzrPrtAInLCgFUNSpIJrlgZo", "UTF-8");
                new GetPlaces().execute(url);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

    //fetch and parse place data
    private class GetPlaces extends AsyncTask<String, Void, String> {

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
}
