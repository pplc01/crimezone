package com.ppl.crimezone.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.ppl.crimezone.R;
import com.ppl.crimezone.fragments.DatePickerDialogFragment;
import com.ppl.crimezone.fragments.TimePickerDialogFragment;
import com.ppl.crimezone.model.CrimeReport;
import org.json.JSONObject;

import java.sql.Time;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ReportController extends FragmentActivity {

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



    /*
        Variable for view detail report mode
     */

    CrimeReport detail;
    CrimeReport newCrimeReport;

    boolean newReportCrimeType [] = new boolean [8];

    private void initReportMode(){
        final String PREFS_NAME = "ReporControllerMode";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        newReportMode =  settings.getBoolean("NewReportMode", false);
    }


    private void showDateDialog(){
        final Button crimeDate = (Button)findViewById(R.id.crime_date);
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
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
                crimeDate.setText(month+"/"+day+"/"+year);
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
                DatePickerDialogFragment datePicker = new DatePickerDialogFragment(dateHandler);

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
                TimePickerDialogFragment timePicker = new TimePickerDialogFragment(timeStartHandler);

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
                TimePickerDialogFragment timePicker = new TimePickerDialogFragment(timeEndHandler);

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

        searchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand(mapContainer);
            }
        });

    }

    private void setUpButtonListener(){
        ImageButton  type [] = new ImageButton[8];
        type[0] = (ImageButton) findViewById(R.id.drugs);
        type[1] = (ImageButton) findViewById(R.id.burglary);
        type[2] = (ImageButton) findViewById(R.id.homicide);
        type[3] = (ImageButton) findViewById(R.id.kidnap);
        type[4] = (ImageButton) findViewById(R.id.sxassault);
        type[5] = (ImageButton) findViewById(R.id.theft);
        type[6] = (ImageButton) findViewById(R.id.vehicletheft);
        type[7] = (ImageButton) findViewById(R.id.violence);
        /*
        for(int ii=0; ii<9; ++ii) {
            final int finalIi = ii;
            type[ii].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(newReportCrimeType[finalIi]){
                        newReportCrimeType[finalIi
                        newReportCrimeType[finalIi] = newReportCrimeType[finalIi] ?false:true;
                    }
            });
        }*/

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initReportMode();

        newReportMode = false;

        if(newReportMode){
            setContentView(R.layout.report_form_ui);
            showMap();
            showDateDialog();
            showTimeDialog();
            autoCollapsExpandMap();
            //seFtUpBottonListener();
            submitForm();
        }else{
            setContentView(R.layout.report_detail_ui);
            final ImageButton giveRatingButton = (ImageButton) findViewById(R.id.b_rate);


            // add button listener
            giveRatingButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // custom dialog
                    final Dialog dialog;
                    dialog = new Dialog(ReportController.this);
                    dialog.setContentView(R.layout.fragment_give_rating);
                    dialog.setTitle("Give Rating");

                    final ImageButton star1 = (ImageButton) dialog.findViewById(R.id.star1);
                    final ImageButton star2 = (ImageButton) dialog.findViewById(R.id.star2);
                    final ImageButton star3 = (ImageButton) dialog.findViewById(R.id.star3);
                    final ImageButton star4 = (ImageButton) dialog.findViewById(R.id.star4);
                    final ImageButton star5 = (ImageButton) dialog.findViewById(R.id.star5);

                    // set the custom dialog components - text, image and button
                    Button submitRating = (Button) dialog.findViewById(R.id.submitrate);
                    Button cancelRating = (Button) dialog.findViewById(R.id.cancelrate);

                    star1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            star1.setImageResource(R.drawable.r_yesstar);
                            star2.setImageResource(R.drawable.r_nostar);
                            star3.setImageResource(R.drawable.r_nostar);
                            star4.setImageResource(R.drawable.r_nostar);
                            star5.setImageResource(R.drawable.r_nostar);
                        }
                    });

                    star2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            star1.setImageResource(R.drawable.r_yesstar);
                            star2.setImageResource(R.drawable.r_yesstar);
                            star3.setImageResource(R.drawable.r_nostar);
                            star4.setImageResource(R.drawable.r_nostar);
                            star5.setImageResource(R.drawable.r_nostar);                        }
                    });
                    star3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            star1.setImageResource(R.drawable.r_yesstar);
                            star2.setImageResource(R.drawable.r_yesstar);
                            star3.setImageResource(R.drawable.r_yesstar);
                            star4.setImageResource(R.drawable.r_nostar);
                            star5.setImageResource(R.drawable.r_nostar);                        }
                    });
                    star4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            star1.setImageResource(R.drawable.r_yesstar);
                            star2.setImageResource(R.drawable.r_yesstar);
                            star3.setImageResource(R.drawable.r_yesstar);
                            star4.setImageResource(R.drawable.r_yesstar);
                            star5.setImageResource(R.drawable.r_nostar);
                        }
                    });
                    star5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            star1.setImageResource(R.drawable.r_yesstar);
                            star2.setImageResource(R.drawable.r_yesstar);
                            star3.setImageResource(R.drawable.r_yesstar);
                            star4.setImageResource(R.drawable.r_yesstar);
                            star5.setImageResource(R.drawable.r_yesstar);
                        }
                    });

                    submitRating.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            //send to server
                        }
                    });

                    cancelRating.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });
        }
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
                place.snippet("Latitude:"+location.latitude+",Longitude:"+location.longitude);
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
                    place.draggable(true);
                    // Adding the marker in the Google Map
                    placeMarker = reportMap.addMarker(place);
                    break;
            }
        }
    }

    private void submitForm(){

        EditText inputTitle = (EditText)findViewById(R.id.title);
        EditText inputDescription = (EditText)findViewById(R.id.description);
        Button date = (Button) findViewById(R.id.crime_date);
        Button timeStart = (Button)findViewById(R.id.crime_time_start);
        Button timeEnd = (Button)findViewById(R.id.crime_time_end);
        ImageButton type1 = (ImageButton) findViewById(R.id.drugs);
        ImageButton type2 = (ImageButton) findViewById(R.id.burglary);
        ImageButton type3 = (ImageButton) findViewById(R.id.homicide);
        ImageButton type4 = (ImageButton) findViewById(R.id.kidnap);
        ImageButton type5 = (ImageButton) findViewById(R.id.sxassault);
        ImageButton type6 = (ImageButton) findViewById(R.id.theft);
        ImageButton type7= (ImageButton) findViewById(R.id.vehicletheft);
        ImageButton type8 = (ImageButton) findViewById(R.id.violence);

        if (inputTitle.getText().toString().equals("")) {
            inputTitle.requestFocus();
            Toast.makeText(getApplicationContext(), "Title field empty", Toast.LENGTH_SHORT).show();
        }else if(inputDescription.toString().equals("")) {
            inputDescription.requestFocus();
            Toast.makeText(getApplicationContext(), "Description field empty", Toast.LENGTH_SHORT).show();
        }
        else if(date.getText().toString().equals("Date")){
            date.performClick();
            Toast.makeText(getApplicationContext(), "Date field empty", Toast.LENGTH_SHORT).show();
        }else if(timeStart.getText().toString().equals("Start")) {
            timeStart.performClick();
            Toast.makeText(getApplicationContext(), "Start Time field empty", Toast.LENGTH_SHORT).show();
        }else if(timeEnd.getText().toString().equals("End")){
            timeEnd.performClick();
            Toast.makeText(getApplicationContext(), "End Time field empty", Toast.LENGTH_SHORT).show();
        }else {

        }
        //validate

    }






    /*
    for collapsing and expanding the map
     */
    public static void expand(final ViewGroup v) {

        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targtetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targtetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targtetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final ViewGroup v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

}



