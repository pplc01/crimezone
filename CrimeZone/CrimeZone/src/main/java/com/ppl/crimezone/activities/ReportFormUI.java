package com.ppl.crimezone.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.CrimeReport;
import com.ppl.crimezone.classes.DatePickerUI;
import com.ppl.crimezone.classes.GsonParser;
import com.ppl.crimezone.classes.TimePickerUI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class ReportFormUI extends ActionBarActivity {

    //variable for point to the map
    private GoogleMap reportMap = null;
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

    public boolean newReptMode = false;
    /*
        for new Report
     */
    DatePickerDialog.OnDateSetListener ondate;


    int hour_start = 15;
    int minute_start = 15;

    int hour_end = 15;
    int minute_end = 15;

    int year;
    int month;
    int day;

    private LatLng location;

    EditText titleEditText;
    String title;

    EditText descriptionEditText;
    String description;

    Boolean newReportMode;
    /*
    Nama Shared Prefereces: UserAccount
    Nama string usermae: usernameKey;
     */
    ProgressBar pb;

    boolean newReportCrimeType [] = new boolean [8];

    private void showDateDialog(){
        final Button crimeDate = (Button)findViewById(R.id.crime_date);
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        Log.d("month", month + "");
        day = cal.get(Calendar.DATE);
        final Handler dateHandler= new Handler(){
            @Override
            public void handleMessage(Message m){
                /** Creating a bundle object to pass currently set Time to the fragment */
                Bundle b = m.getData();

                /** Getting the year from bundle */
                year = b.getInt("set_year");

                /** Getting the month from bundle */
                month = b.getInt("set_month");

                /** Getting the day from bundle */
                day = b.getInt("set_day");

                /** Displaying a short time message containing time set by Time picker dialog fragment */
                crimeDate.setText(day+"/"+(month+1)+"/"+year);
            }
        };

        /** Click Event Handler for button */
        View.OnClickListener dateListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /** Creating a bundle object to pass currently set time to the fragment */
                Bundle b = new Bundle();

                /** Adding currently set hour to bundle object */
                b.putInt("set_year", year);

                /** Adding currently set minute to bundle object */
                b.putInt("set_month", month);

                b.putInt("set_day", day);
                /** Instantiating TimePickerDialogFragment */
                DatePickerUI datePicker = new DatePickerUI(dateHandler);

                /** Setting the bundle object on timepicker fragment */
                datePicker.setArguments(b);

                /** Getting fragment manger for this activity */
                FragmentManager fm = getSupportFragmentManager();

                /** Starting a fragment transaction */
                FragmentTransaction ft = fm.beginTransaction();

                /** Adding the fragment object to the fragment transaction */
                ft.add(datePicker, "date_picker");

                /** Opening the TimePicker fragment */
                ft.commit();
            }
        };
        crimeDate.setOnClickListener(dateListener);
    }


    private void showTimeDialog(){
        final Button crimeStartTime = (Button) findViewById(R.id.crime_time_start);
        final Button crimeEndTime = (Button) findViewById(R.id.crime_time_end);
        Calendar cal = Calendar.getInstance();

        hour_end = cal.get(Calendar.HOUR);
        minute_end = cal.get(Calendar.MINUTE);

        hour_start = cal.get(Calendar.HOUR);
        minute_start = cal.get(Calendar.MINUTE);


        final Handler timeStartHandler= new Handler(){
            @Override
            public void handleMessage(Message m){
                /** Creating a bundle object to pass currently set Time to the fragment */
                Bundle b = m.getData();

                /** Getting the Hour of day from bundle */
                hour_start = b.getInt("set_hour");

                /** Getting the Minute of the hour from bundle */
                minute_start = b.getInt("set_minute");

                /** Displaying a short time message containing time set by Time picker dialog fragment */
                crimeStartTime.setText(hour_start + ":" + minute_start);

            }
        };



        /** Click Event Handler for button */
        View.OnClickListener timeStartListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /** Creating a bundle object to pass currently set time to the fragment */
                Bundle b = new Bundle();

                /** Adding currently set hour to bundle object */
                b.putInt("set_hour", hour_start);

                /** Adding currently set minute to bundle object */
                b.putInt("set_minute", minute_start);

                /** Instantiating TimePickerDialogFragment */
                TimePickerUI timePicker = new TimePickerUI(timeStartHandler);

                /** Setting the bundle object on timepicker fragment */
                timePicker.setArguments(b);

                /** Getting fragment manger for this activity */
                FragmentManager fm = getSupportFragmentManager();

                /** Starting a fragment transaction */
                FragmentTransaction ft = fm.beginTransaction();

                /** Adding the fragment object to the fragment transaction */
                ft.add(timePicker, "time_picker");

                /** Opening the TimePicker fragment */
                ft.commit();
            }
        };

        final Handler timeEndHandler= new Handler(){
            @Override
            public void handleMessage(Message m){
                /** Creating a bundle object to pass currently set Time to the fragment */
                Bundle b = m.getData();

                /** Getting the Hour of day from bundle */
                hour_end = b.getInt("set_hour");

                /** Getting the Minute of the hour from bundle */
                minute_end = b.getInt("set_minute");

                /** Displaying a short time message containing time set by Time picker dialog fragment */
                crimeEndTime.setText(hour_end+":"+minute_end);

            }
        };



        /** Click Event Handler for button */
        View.OnClickListener timeEndListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /** Creating a bundle object to pass currently set time to the fragment */
                Bundle b = new Bundle();

                /** Adding currently set hour to bundle object */
                b.putInt("set_hour", hour_start);

                /** Adding currently set minute to bundle object */
                b.putInt("set_minute", minute_start);

                /** Instantiating TimePickerDialogFragment */
                TimePickerUI timePicker = new TimePickerUI(timeEndHandler);

                /** Setting the bundle object on timepicker fragment */
                timePicker.setArguments(b);

                /** Getting fragment manger for this activity */
                FragmentManager fm = getSupportFragmentManager();

                /** Starting a fragment transaction */
                FragmentTransaction ft = fm.beginTransaction();

                /** Adding the fragment object to the fragment transaction */
                ft.add(timePicker, "time_picker");

                /** Opening the TimePicker fragment */
                ft.commit();
            }
        };

        crimeStartTime.setOnClickListener(timeStartListener);
        crimeEndTime.setOnClickListener(timeEndListener);

    }


    private void autoCollapsExpandMap(){
        final LinearLayout mapContainer;
        titleEditText = (EditText) findViewById(R.id.headline);
        descriptionEditText = (EditText) findViewById(R.id.description);
        mapContainer = (LinearLayout)findViewById(R.id.map_frame);

        descriptionEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    LinearLayout.LayoutParams parms = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    parms.height = 0;
                    // Set it back.
                    mapContainer.setLayoutParams(parms);
                }else{
                    Display display = getWindowManager().getDefaultDisplay();
                    int screen_height = display.getHeight();
                    screen_height = (int) (0.3*screen_height);
                    LinearLayout.LayoutParams parms = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    parms.height = screen_height;
                    // Set it back.
                    mapContainer.setLayoutParams(parms);
                }
            }
        });

        titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    LinearLayout.LayoutParams parms = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    parms.height = 0;
                    // Set it back.
                    mapContainer.setLayoutParams(parms);
                }else{
                    Display display = getWindowManager().getDefaultDisplay();
                    int screen_height = display.getHeight();
                    screen_height = (int) (0.3*screen_height);
                    LinearLayout.LayoutParams parms = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    parms.height = screen_height;
                    // Set it back.
                    mapContainer.setLayoutParams(parms);
                }
            }
        });

        searchLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    Display display = getWindowManager().getDefaultDisplay();
                    int screen_height = display.getHeight();
                    screen_height = (int) (0.3*screen_height);
                    LinearLayout.LayoutParams parms = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    parms.height = screen_height;
                    // Set it back.
                    mapContainer.setLayoutParams(parms);
                }
            }
        });


    }

    private void setUpButtonListener(){
        Button submitButton = (Button) findViewById(R.id.submit);
        for(int ii=0; ii< 8; ++ii){
            newReportCrimeType[ii] = false;
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                submitForm();
                                            }
                                        }
        );

        final ImageButton type [] = new ImageButton[8];
        type[0] = (ImageButton) findViewById(R.id.drugs);
        type[1] = (ImageButton) findViewById(R.id.burglary);
        type[2] = (ImageButton) findViewById(R.id.homicide);
        type[3] = (ImageButton) findViewById(R.id.kidnap);
        type[4] = (ImageButton) findViewById(R.id.sxassault);
        type[5] = (ImageButton) findViewById(R.id.theft);
        type[6] = (ImageButton) findViewById(R.id.vehicletheft);
        type[7] = (ImageButton) findViewById(R.id.violence);

        type[0].setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           if (newReportCrimeType[0]) {
                                               type[0].setImageResource(R.drawable.ic_drugs);
                                               newReportCrimeType[0] = false;
                                           }else{
                                               type[0].setImageResource(R.drawable.nc_drugs);
                                               newReportCrimeType[0] = true;
                                           }
                                       }
                                   }
        );
        type[1].setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           if (newReportCrimeType[1]) {
                                               type[1].setImageResource(R.drawable.ic_burglary);
                                               newReportCrimeType[1] = false;
                                           }else{
                                               type[1].setImageResource(R.drawable.nc_burglary);
                                               newReportCrimeType[1] = true;
                                           }
                                       }
                                   }
        );

        type[2].setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           if (newReportCrimeType[2]) {
                                               type[2].setImageResource(R.drawable.ic_homicide);
                                               newReportCrimeType[2] = false;
                                           }else{
                                               type[2].setImageResource(R.drawable.nc_homicide);
                                               newReportCrimeType[2] = true;
                                           }
                                       }
                                   }
        );
        type[3].setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           if (newReportCrimeType[3]) {
                                               type[3].setImageResource(R.drawable.ic_kidnap);
                                               newReportCrimeType[3] = false;
                                           }else{
                                               type[3].setImageResource(R.drawable.nc_kidnap);
                                               newReportCrimeType[3] = true;
                                           }
                                       }
                                   }
        );
        type[4].setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           if (newReportCrimeType[4]) {
                                               type[4].setImageResource(R.drawable.ic_sxassault);
                                               newReportCrimeType[4] = false;
                                           }else{
                                               type[4].setImageResource(R.drawable.nc_sxassault);
                                               newReportCrimeType[4] = true;
                                           }
                                       }
                                   }
        );
        type[5].setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           if (newReportCrimeType[5]) {
                                               type[5].setImageResource(R.drawable.ic_theft);
                                               newReportCrimeType[5] = false;
                                           } else {
                                               type[5].setImageResource(R.drawable.nc_theft);
                                               newReportCrimeType[5] = true;
                                           }
                                       }
                                   }
        );
        type[6].setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           if (newReportCrimeType[6]) {
                                               type[6].setImageResource(R.drawable.ic_vehicletheft);
                                               newReportCrimeType[6] = false;
                                           } else {
                                               type[6].setImageResource(R.drawable.nc_vehicletheft);
                                               newReportCrimeType[6] = true;
                                           }
                                       }
                                   }
        );
        type[7].setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           if (newReportCrimeType[7]) {
                                               type[7].setImageResource(R.drawable.ic_violence);
                                               newReportCrimeType[7] = false;
                                           }else{
                                               type[7].setImageResource(R.drawable.nc_violence);
                                               newReportCrimeType[7] = true;
                                           }
                                       }
                                   }
        );
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_form_ui);
        showMap();

        pb=(ProgressBar)findViewById(R.id.progressBar1);
        pb=(ProgressBar)findViewById(R.id.progressBar1);
        pb.setVisibility(View.GONE);
        showDateDialog();
        showTimeDialog();
        autoCollapsExpandMap();
        setUpButtonListener();
        ImageButton backHome = (ImageButton)findViewById(R.id.back_new_report);
        backHome.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    public void setUpMarkerListener(){
        reportMap.setOnMarkerDragListener(
                new GoogleMap.OnMarkerDragListener()
                {
                    @Override
                    public void onMarkerDragStart(Marker marker) {}

                    @Override
                    public void onMarkerDrag(Marker marker) {}

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        location = marker.getPosition();
                    }
                }

        );
    }


    public void showMap(){
        if(reportMap == null) {
            reportMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.new_report_map)).getMap();
            reportMap.setMyLocationEnabled(true);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            updateLocationUser();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            setUpSearchLocation();
            setUpMarkerListener();
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
                //place.snippet("Latitude:"+location.latitude+",Longitude:"+location.longitude);
                place.draggable(true);
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
    private class DownloadTask extends AsyncTask<String, Void, String> {

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
                    place.draggable(true);
                    // Adding the marker in the Google Map
                    placeMarker = reportMap.addMarker(place);
                    break;
            }
        }
    }

    private void submitForm(){
        EditText inputTitle = (EditText)findViewById(R.id.headline);
        EditText inputDescription = (EditText)findViewById(R.id.description);
        Button date = (Button) findViewById(R.id.crime_date);
        Button timeStart = (Button)findViewById(R.id.crime_time_start);
        Button timeEnd = (Button)findViewById(R.id.crime_time_end);
        boolean validDate= true;
        final Button crimeDate = (Button)findViewById(R.id.crime_date);
        Calendar cal = Calendar.getInstance();
        if(year > cal.get(Calendar.YEAR)) {
            validDate = false;
        }else if(year == cal.get(Calendar.YEAR) && month > cal.get(Calendar.MONTH)){
            validDate = false;
        }else if(year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH) && day > cal.get(Calendar.DATE)){
            validDate = false;
        }

        boolean validTimeStart = true;
        boolean validTimeEnd = true;
        if(year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH) && day == cal.get(Calendar.DATE) && hour_start > cal.get(Calendar.HOUR)) {
            validTimeStart = false;
        }else if(year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH) && day == cal.get(Calendar.DATE) && hour_start == cal.get(Calendar.HOUR) && minute_start > cal.get(Calendar.MINUTE)){
            validTimeStart = false;
        }else if(year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH) && day == cal.get(Calendar.DATE) && hour_end > cal.get(Calendar.HOUR)) {
            validTimeEnd = false;
        }else if(year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH) && day == cal.get(Calendar.DATE) && hour_start == cal.get(Calendar.HOUR) && minute_end > cal.get(Calendar.MINUTE)){
            validTimeEnd = false;
        }else if(hour_start > hour_end) {
            validTimeStart = false;
        }else if(hour_start == hour_end && minute_start > minute_end){
            validTimeStart = false;
        }else if( hour_start == hour_end && minute_start == minute_end){
            validTimeStart = false;
        }


        if (inputTitle.getText().toString().equals("")) {
            inputTitle.requestFocus();
            Toast.makeText(getApplicationContext(), "Title field empty", Toast.LENGTH_SHORT).show();
        }else if(inputDescription.getText().toString().equalsIgnoreCase("")) {
            inputDescription.requestFocus();

            Toast.makeText(getApplicationContext(), "Description field empty", Toast.LENGTH_SHORT).show();
        }
        else if(date.getText().toString().equals("DD/MM/YYYY")){
            date.performClick();
            Toast.makeText(getApplicationContext(), "Date field empty", Toast.LENGTH_SHORT).show();
        }else if(!validDate) {
            date.performClick();
            Toast.makeText(getApplicationContext(), "Date field invalid", Toast.LENGTH_SHORT).show();
        }else if(timeStart.getText().toString().equals("Start")) {
            timeStart.performClick();
            Toast.makeText(getApplicationContext(), "Start Time field empty", Toast.LENGTH_SHORT).show();
        }else if(!validTimeStart) {
            timeStart.performClick();
            Toast.makeText(getApplicationContext(), "Start Time field invalid", Toast.LENGTH_SHORT).show();
        }else if(timeEnd.getText().toString().equals("End") ) {
            timeEnd.performClick();
            Toast.makeText(getApplicationContext(), "End Time field empty", Toast.LENGTH_SHORT).show();
        }else if(!validTimeEnd){
            timeEnd.performClick();
            Toast.makeText(getApplicationContext(), "End Time field invalid", Toast.LENGTH_SHORT).show();
        }else {
            boolean valid = false;
            for(int ii=0; ii<8; ++ii)
            {
                Log.d("loop "+ii, newReportCrimeType[ii]+"");
                if(newReportCrimeType[ii]){
                    valid = true;
                    break;
                }
            }
            if(valid){
                //json

                /*
                username
                title
                dreported
                start
                end
                description
                lat
                long
                crimetype1
                crimetype2
                crimetype3
                crimetype4
                crimetype5
                crimetype6
                crimetype7
                crimetype8
                 */

                String username = "adesudiman";
                String [] data = new String[16];



                data[0] = username;
                data[1] = titleEditText.getText().toString();
                data[2] = cal.get(Calendar.DATE)+ "/"+(cal.get(Calendar.MONTH)+1)+"/" + cal.get(Calendar.YEAR) + " " + cal.get(Calendar.HOUR)+ ":"+cal.get(Calendar.MINUTE);
                data[3] =  day+"/"+(month+1)+"/" + year + " " + hour_start+ ":"+ minute_start;
                data[4] = day+"/"+(month+1)+"/" + year + " " + hour_end+ ":"+ minute_end;
                data[5] = descriptionEditText.getText().toString();
                data[6] = location.latitude+"";
                data[7] = location.longitude+"";
                for(int jj=0; jj<8;  ++jj){
                    data[jj+8] = newReportCrimeType[jj]+"";
                }
                new MyAsyncTask().execute(data);

            }else{
                Toast.makeText(getApplicationContext(), "Crime Type field empty", Toast.LENGTH_SHORT).show();
            }
        }
        //validate
    }


    private class MyAsyncTask extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            return postData(params);

        }

        protected void onPostExecute(String result){
            Log.d("response ", result);
            pb.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "command sent", Toast.LENGTH_LONG).show();
            /**
             * Tampilin progress bar
             * kalau berhasil tampilkan notif berhasil
             * havis itu write share preference mengenai location nya
             * pindah ke view map
             */
            String PREFS_NAME = "NewReportLocation";
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("latitude", location.latitude + "");
            editor.putString("longitude", location.longitude+"");

            // Commit the edits!
            editor.commit();

            finish();
        }
        protected void onProgressUpdate(Integer... progress){
            pb.setProgress(progress[0]);
        }

        public String postData(String valueIWantToSend[]) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://crimezone.besaba.com/webservice/submitReport.php");
            String message = "";
            try
            {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("username", valueIWantToSend[0]));
                nameValuePairs.add(new BasicNameValuePair("title", valueIWantToSend[1]));
                nameValuePairs.add(new BasicNameValuePair("dreported", valueIWantToSend[2]));
                nameValuePairs.add(new BasicNameValuePair("start", valueIWantToSend[3]));
                nameValuePairs.add(new BasicNameValuePair("end", valueIWantToSend[4]));
                nameValuePairs.add(new BasicNameValuePair("description", valueIWantToSend[5]));
                nameValuePairs.add(new BasicNameValuePair("lat", valueIWantToSend[6]));
                nameValuePairs.add(new BasicNameValuePair("long", valueIWantToSend[7]));
                if(valueIWantToSend[8].equals("true")){
                    nameValuePairs.add(new BasicNameValuePair("crimetype1", 0+""));
                }
                if(valueIWantToSend[9].equals("true")) {
                    nameValuePairs.add(new BasicNameValuePair("crimetype2", 1 + ""));
                }
                if(valueIWantToSend[10].equals("true")){
                    nameValuePairs.add(new BasicNameValuePair("crimetype3", 2+""));
                }
                if(valueIWantToSend[11].equals("true")){
                    nameValuePairs.add(new BasicNameValuePair("crimetype4", 3+""));
                }
                if(valueIWantToSend[12].equals("true")){
                    nameValuePairs.add(new BasicNameValuePair("crimetype5", 4+""));
                }
                if(valueIWantToSend[13].equals("true")){
                    nameValuePairs.add(new BasicNameValuePair("crimetype6", 5+""));
                }
                if(valueIWantToSend[14].equals("true")){
                    nameValuePairs.add(new BasicNameValuePair("crimetype7", 6+""));
                }
                if(valueIWantToSend[15].equals("true")){
                    nameValuePairs.add(new BasicNameValuePair("crimetype8", 7+""));
                }
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                message =  response.getStatusLine().getStatusCode()+"";
            }
            catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            return message;
        }
    }
}