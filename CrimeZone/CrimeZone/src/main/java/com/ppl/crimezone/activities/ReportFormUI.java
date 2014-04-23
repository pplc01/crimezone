package com.ppl.crimezone.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
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
import com.ppl.crimezone.classes.DatePickerUI;
import com.ppl.crimezone.classes.GsonParser;
import com.ppl.crimezone.classes.ReportController;
import com.ppl.crimezone.classes.TimePickerUI;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class ReportFormUI extends FragmentActivity {

    //variable for point to the map
    private GoogleMap reportMap = null;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Marker placeMarker;
    private MarkerOptions place;

    //for filter
    private AutoCompleteTextView searchLocation;
    private DownloadTask placesDownloadTask;
    private DownloadTask placeDetailsDownloadTask;
    private ParserTask placesParserTask;
    private ParserTask placeDetailsParserTask;

    final int PLACES=0;
    final int PLACES_DETAILS=1;



    private EditText titleEditText, descriptionEditText;
    private Button crimeDate,crimeTime, submitButton;
    ImageButton type [] = new ImageButton[8];


    boolean newReportCrimeType [] = new boolean [8];
    Calendar time;
    LatLng location;
    //for progress bar
    //ProgressBar pb;


    private void printDate(){
        int timeInt = time.get(Calendar.DAY_OF_MONTH);
        String dayString = timeInt+"";
        if(timeInt<10)dayString = "0"+ dayString;
        timeInt = time.get(Calendar.MONTH)+1;
        String monthString = (timeInt)+"";
        if(timeInt<10)monthString= "0"+ monthString;
        crimeDate.setText(dayString+"/"+ monthString+"/"+time.get(Calendar.YEAR));
    }

    private void printTIme(){
        int timeInt = time.get(Calendar.HOUR_OF_DAY);
        String hourString = timeInt+"";
        if(timeInt<10)hourString = "0"+ hourString;
        timeInt = time.get(Calendar.MINUTE);
        String minuteString = timeInt + "";
        if(timeInt < 10)minuteString = "0"+ minuteString;
        crimeTime.setText(hourString + ":" + minuteString);
    }
    private void showDateDialog(){
        crimeDate = (Button)findViewById(R.id.crime_date);
        time = Calendar.getInstance();

        final Handler dateHandler= new Handler(){
            @Override
            public void handleMessage(Message m){
                Bundle b = m.getData();
                time.set(Calendar.YEAR, b.getInt("set_year"));
                time.set(Calendar.MONTH,b.getInt("set_month"));
                time.set(Calendar.DAY_OF_MONTH, b.getInt("set_day"));
                Calendar today = Calendar.getInstance();
                if(time.getTimeInMillis() > today.getTimeInMillis()){
                    crimeDate.setError("DATE NOT VALID");
                    showAlertDialog("DATE NOT VALID", "Please Select Date Today Backward");
                    crimeDate.setText("DD/MM/YYYY");
                }else {
                    printDate();
                }
            }
        };

        View.OnClickListener dateListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putInt("set_year", time.get(Calendar.YEAR));
                b.putInt("set_month", time.get(Calendar.MONTH));
                b.putInt("set_day", time.get(Calendar.DAY_OF_MONTH));
                DatePickerUI datePicker = new DatePickerUI(dateHandler);
                datePicker.setArguments(b);
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(datePicker, "date_picker");
                ft.commit();
            }
        };
        crimeDate.setOnClickListener(dateListener);
    }


    private void showTimeDialog(){
        crimeTime = (Button) findViewById(R.id.crime_time);

        final Handler timeHandler= new Handler(){
            @Override
            public void handleMessage(Message m){
                Bundle b = m.getData();
                time.set(Calendar.HOUR_OF_DAY, b.getInt("set_hour"));
                time.set(Calendar.MINUTE,  b.getInt("set_minute"));
                Calendar today = Calendar.getInstance();
                if(!crimeDate.getText().toString().equals("DD/MM/YYYY") && time.getTimeInMillis() > today.getTimeInMillis()){
                    crimeTime.setError("TIME NOT VALID");
                    showAlertDialog("TIME NOT VALID", "Please Select Tme Before Current Time");
                    crimeTime.setText("HH:MM");
                }else {
                    printTIme();
                }
            }
        };

        View.OnClickListener timeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Bundle b = new Bundle();
                b.putInt("set_hour", time.get(Calendar.HOUR_OF_DAY));
                b.putInt("set_minute", time.get(Calendar.MINUTE));
                TimePickerUI timePicker = new TimePickerUI(timeHandler);
                timePicker.setArguments(b);
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(timePicker, "time_picker");
                ft.commit();
            }
        };
        crimeTime.setOnClickListener(timeListener);
    }

    int onExpand = 1;
    private void autoCollapsExpandMap(){
        final RelativeLayout mapContainer;
        ImageView expandCollapseMap = (ImageView) findViewById(R.id.expand_collapse_button);
        titleEditText = (EditText) findViewById(R.id.headline);
        descriptionEditText = (EditText) findViewById(R.id.description);
        mapContainer = (RelativeLayout)findViewById(R.id.map_frame);
        expandCollapseMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onExpand==1) {
                    Display display = getWindowManager().getDefaultDisplay();
                    int screen_height = display.getHeight();
                    screen_height = (int) (0.72 * screen_height);

                    SupportMapFragment mMapFragment = (SupportMapFragment) (getSupportFragmentManager()
                            .findFragmentById(R.id.new_report_map));
                    ViewGroup.LayoutParams params2 = mMapFragment.getView().getLayoutParams();

                    params2.height = screen_height;
                    mMapFragment.getView().setLayoutParams(params2);

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    params.height = screen_height;
                    mapContainer.setLayoutParams(params);

                    onExpand = 2;

                }else if(onExpand == 2){
                    SupportMapFragment mMapFragment = (SupportMapFragment) (getSupportFragmentManager()
                            .findFragmentById(R.id.new_report_map));
                    ViewGroup.LayoutParams params2 = mMapFragment.getView().getLayoutParams();
                    params2.height = 0;
                    mMapFragment.getView().setLayoutParams(params2);

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    params.height = 0;
                    mapContainer.setLayoutParams(params);

                    onExpand = 0;

                }else {
                    Display display = getWindowManager().getDefaultDisplay();
                    int screen_height = display.getHeight();
                    screen_height = (int) (0.35 * screen_height);
                    SupportMapFragment mMapFragment = (SupportMapFragment) (getSupportFragmentManager()
                            .findFragmentById(R.id.new_report_map));
                    ViewGroup.LayoutParams params2 = mMapFragment.getView().getLayoutParams();
                    params2.height = screen_height;
                    mMapFragment.getView().setLayoutParams(params2);

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    params.height = screen_height;
                    mapContainer.setLayoutParams(params);


                    onExpand = 1;
                }
            }
        });
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("")){
                    titleEditText.setError(null);
                }else{
                    int ecolor = Color.RED; // whatever color you want
                    String estring = "Title Still Empty      ";  // your error message
                    ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
                    SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);
                    ssbuilder.setSpan(fgcspan, 0, estring.length(), 0);
                    titleEditText.setError(ssbuilder);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("")){
                    descriptionEditText.setError(null);
                }else{
                    descriptionEditText.setError(Html.fromHtml("<font color='green'>Description Empty</font>"));
                    int ecolor = Color.RED;
                    String estring = "Description Still Empty      ";
                    ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
                    SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);
                    ssbuilder.setSpan(fgcspan, 0, estring.length(), 0);
                    descriptionEditText.setError(ssbuilder);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        descriptionEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    Display display = getWindowManager().getDefaultDisplay();
                    SupportMapFragment mMapFragment = (SupportMapFragment) (getSupportFragmentManager()
                            .findFragmentById(R.id.new_report_map));
                    ViewGroup.LayoutParams params2 = mMapFragment.getView().getLayoutParams();
                    params2.height = 0;
                    mMapFragment.getView().setLayoutParams(params2);

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    params.height = 0;
                    mapContainer.setLayoutParams(params);

                    onExpand = 0;
                }else{
                }
            }
        });

        titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    Display display = getWindowManager().getDefaultDisplay();
                    int screen_height = display.getHeight();
                    SupportMapFragment mMapFragment = (SupportMapFragment) (getSupportFragmentManager()
                            .findFragmentById(R.id.new_report_map));
                    ViewGroup.LayoutParams params2 = mMapFragment.getView().getLayoutParams();
                    params2.height = 0;
                    mMapFragment.getView().setLayoutParams(params2);

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    params.height = 0;
                    mapContainer.setLayoutParams(params);

                    onExpand = 0;
                }
                else{
                    Display display = getWindowManager().getDefaultDisplay();
                    int screen_height = display.getHeight();
                    screen_height = (int) (0.35*screen_height);
                    LinearLayout.LayoutParams parms = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    parms.height = screen_height;
                    mapContainer.setLayoutParams(parms);
                }
            }
        });

        searchLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(onExpand == 0) {
                    Display display = getWindowManager().getDefaultDisplay();
                    int screen_height = display.getHeight();
                    screen_height = (int) (0.35 * screen_height);
                    SupportMapFragment mMapFragment = (SupportMapFragment) (getSupportFragmentManager()
                            .findFragmentById(R.id.new_report_map));
                    ViewGroup.LayoutParams params2 = mMapFragment.getView().getLayoutParams();
                    params2.height = screen_height;
                    mMapFragment.getView().setLayoutParams(params2);

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                    params.height = screen_height;
                    mapContainer.setLayoutParams(params);
                   onExpand = 1;
                }
            }
        });

    }

    private void setUpButtonListener(){
        submitButton = (Button) findViewById(R.id.submit);
        for(int ii=0; ii< 8; ++ii){
            newReportCrimeType[ii] = false;
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                submitButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                                submitForm();
                                            }
                                        }
        );

        type[0] = (ImageButton) findViewById(R.id.drugs);
        type[1] = (ImageButton) findViewById(R.id.burglary);
        type[2] = (ImageButton) findViewById(R.id.homicide);
        type[3] = (ImageButton) findViewById(R.id.kidnap);
        type[4] = (ImageButton) findViewById(R.id.sxassault);
        type[5] = (ImageButton) findViewById(R.id.theft);
        type[6] = (ImageButton) findViewById(R.id.vehicletheft);
        type[7] = (ImageButton) findViewById(R.id.violence);

        for(int ii=0; ii<8; ++ii){
            type[ii].setImageResource(R.drawable.ic_nocrime);
            final int finalIi = ii;
            type[ii].setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (newReportCrimeType[finalIi]) {
                                                    type[finalIi].setImageResource(R.drawable.ic_nocrime);
                                                    newReportCrimeType[finalIi] = false;
                                                }else{
                                                    type[finalIi].setImageResource(getResources().getColor(android.R.color.transparent));
                                                    newReportCrimeType[finalIi] = true;
                                                }
                                            }
                                        }
            );
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_form_ui);
        showMap();
        showDateDialog();
        showTimeDialog();
        autoCollapsExpandMap();
        setUpButtonListener();

        final ImageButton backHome = (ImageButton)findViewById(R.id.back_new_report);
        backHome.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                backHome.setImageResource(R.drawable.back_pressed);
                finish();
            }
        });
    }

    public void setUpMarkerListener(){
        reportMap.setOnMarkerDragListener(
                new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {
                    }

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

    private void updateLocationUser(){
        locationListener = new LocationListener() {
            public void onLocationChanged(Location userLocation) {
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
                place.snippet("Drag and Drop");
                place.draggable(true);

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
            searchLocation.setThreshold(0);
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

                    location = new LatLng(latitude, longitude);

                    CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(location);

                    ;               reportMap.animateCamera(cameraPosition);

                    if(placeMarker != null)placeMarker.remove();
                    place = new MarkerOptions();
                    place.position(location);
                    place.title("Crime Location");
                    place.draggable(true);
                    // Adding the marker in the Google Map
                    placeMarker = reportMap.addMarker(place);
                    break;
            }
        }
    }

    //pindahin ke Report Controller.validate
    private void submitForm(){
        Calendar submitTime = Calendar.getInstance();
        if (titleEditText.getText().toString().equals("")) {
            titleEditText.requestFocus();
            int ecolor = Color.RED; // whatever color you want
            String estring = "Title Still Empty      ";  // your error message
            ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
            SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);
            ssbuilder.setSpan(fgcspan, 0, estring.length(), 10);
            titleEditText.setError(ssbuilder);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(titleEditText, InputMethodManager.SHOW_IMPLICIT);
            submitButton.setBackgroundColor(getResources().getColor(R.color.color4));
        }else if(descriptionEditText.getText().toString().equalsIgnoreCase("")) {
            descriptionEditText.requestFocus();
            int ecolor = Color.RED; // whatever color you want
            String estring = "Description Still Empty";  // your error message
            ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
            SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);
            ssbuilder.setSpan(fgcspan, 0, estring.length(), 0);
            descriptionEditText.setError(ssbuilder);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(descriptionEditText, InputMethodManager.SHOW_IMPLICIT);
            submitButton.setBackgroundColor(getResources().getColor(R.color.color4));
        }
        else if(crimeDate.getText().toString().equals("DD/MM/YYYY")){
            crimeDate.setError("DATE NOT SET");
            showAlertDialog("DATE NOT SET", "Please Select Valid Date First");
            submitButton.setBackgroundColor(getResources().getColor(R.color.color4));
        }else if(time.get(Calendar.YEAR) > submitTime.get(Calendar.YEAR) || time.get(Calendar.MONTH) > submitTime.get(Calendar.MONTH) || time.get(Calendar.DAY_OF_MONTH) > submitTime.get(Calendar.DAY_OF_MONTH)) {
            crimeDate.setError("DATE NOT VALID");
            showAlertDialog("DATE NOT VALID", "Please Select Date Today Backward");
            submitButton.setBackgroundColor(getResources().getColor(R.color.color4));
        }else if(crimeTime.getText().toString().equals("HH:MM")) {
            crimeTime.setError("TIME NOT SET");
            showAlertDialog("TIME NOT SET", "Please Select Valid Time First");
            submitButton.setBackgroundColor(getResources().getColor(R.color.color4));
        }else if(time.getTimeInMillis() > submitTime.getTimeInMillis()) {
            crimeTime.setError("TIME NOT VALID");
            showAlertDialog("TIME NOT VALID", "Please Select Tme Before Current Time");
            submitButton.setBackgroundColor(getResources().getColor(R.color.color4));
        }else {
            boolean valid = false;
            for (int ii = 0; ii < 8; ++ii) {
                Log.d("loop " + ii, newReportCrimeType[ii] + "");
                if (newReportCrimeType[ii]) {
                    valid = true;
                    break;
                }
            }
            if (valid) {
                String MYPREFERENCES = "UserAccount";
                SharedPreferences sharedPreferences = getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);
                String username = sharedPreferences.getString("usernameKey", "");

                new SendReport().execute(ReportController.prepareNewReportData(time, submitTime, username, titleEditText.getText().toString(), descriptionEditText.getText().toString(), location, newReportCrimeType));

            } else {
                showAlertDialog("CRIME TYPE EMPTY", "Please Select At Least One Crime Type");
                submitButton.setBackgroundColor(getResources().getColor(R.color.color4));
            }
        }
    }

    private void showAlertDialog(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(ReportFormUI.this);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class SendReport extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            return ReportController.postData(params);

        }

        protected void onPostExecute(String result){
            Log.d("response ", result);
            //pb.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Succesful", Toast.LENGTH_LONG).show();
            /**
             * Tampilin progress bar
             * kalau berhasil tampilkan notif berhasil
             * havis itu write share preference mengenai location nya
             * pindah ke view map
             */
            String PREFS_NAME = "ReportLocation";
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("NotHome", true);
            editor.putString("latitude", location.latitude + "");
            editor.putString("longitude", location.longitude+"");

            // Commit the edits!
            editor.commit();

            finish();
        }
        protected void onProgressUpdate(Integer... progress){
            //pb.setProgress(progress[0]);
        }
    }
}