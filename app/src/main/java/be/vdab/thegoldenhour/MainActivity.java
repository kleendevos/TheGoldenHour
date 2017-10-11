package be.vdab.thegoldenhour;

import android.*;
import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.R.attr.data;
import static android.R.attr.singleLineTitle;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private TextView mTextMessage;

    Button dateButton;
    int year_x, month_x, day_x;
    static final int DIALOG_ID = 0;
    TextView CurrentLocationAbove;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 777;
    private double longitude;
    private double latitude;

    private TextView textview;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }

    };
    private SimpleDateFormat simpleDateFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        final Calendar calender = Calendar.getInstance();
        year_x = calender.get(Calendar.YEAR);
        month_x = calender.get(Calendar.MONTH);
        day_x = calender.get(Calendar.DAY_OF_MONTH);

        CurrentLocationAbove = (TextView) findViewById(R.id.CurrentLocationAbove);

        textview = (TextView) findViewById(R.id.date);
        textview.setText(day_x + " / " + (month_x + 1) + " / " + year_x);

        showDialogOnClick();
        captureLocation(CurrentLocationAbove);

        getvalues();







    }

    public void captureLocation(View view) {
        if (checkPermission()) {
        } else {
            requestPermission();
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationlistener);
    }


    LocationListener locationlistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String locationFound = "";

            longitude = location.getLongitude();
            latitude = location.getLatitude();

            try {
                locationFound += getCityFromLocation(location.getLongitude(), location.getLatitude()) + ", " + getCountryFromLocation(location.getLongitude(), location.getLatitude());
            } catch (IOException e) {
                e.printStackTrace();
            }
            CurrentLocationAbove.setText(locationFound);

            getURL();


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider Enabled", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider Disabled", Toast.LENGTH_LONG).show();
        }
    };

    private String getCountryFromLocation(double longitude, double latitude) throws IOException {
        String country = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            country = addresses.get(0).getCountryName();

        }


        return country;
    }

    private String getCityFromLocation(double longitude, double latitude) throws IOException {

        String city = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Log.d("address",addresses.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            city = addresses.get(0).getLocality();

        }


        return city;
    }


    public DatePickerDialog.OnDateSetListener dpickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthofyear, int dayofMonth) {
            year_x = year;
            month_x = monthofyear;
            day_x = dayofMonth;


            textview.setText(day_x + " / " + (month_x + 1) + " / " + year_x);
            // Toast.makeText(MainActivity.this,year_x + "/" + (month_x+1) + "/" + day_x, Toast.LENGTH_LONG).show();

            getURL();
            Log.d("url",getURL().toString());
            getvalues();

        }
    };

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "GPS permission needed to capture your location", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void showDialogOnClick() {
        dateButton = (Button) findViewById(R.id.changeDate);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(DIALOG_ID);

            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID)
            return new DatePickerDialog(this, dpickerListener, year_x, month_x, day_x);
        return null;
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void getvalues() {
        final TextView sunriseTxt = (TextView) findViewById(R.id.Sunrise);
        final TextView sunsetTxt = (TextView) findViewById(R.id.Sunset);
        final TextView goldenhoursunriseTsxt = (TextView) findViewById(R.id.GoldenHourSunrise);
        final TextView goldenhoursunsetTxt = (TextView) findViewById(R.id.GoldenHour2);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getURL(), (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", response.toString());
                String sunrise = " ";
                String sunset = " ";
                String goldenhoursunrise = " ";
                String goldenhoursunset = " ";

                try {
                    JSONObject results = response.getJSONObject("results");
                    sunrise = results.getString("sunrise");
                    sunset = results.getString("sunset");
                    goldenhoursunrise = results.getString("civil_twilight_begin");
                    goldenhoursunset = results.getString("civil_twilight_end");

//                    simpleDateFormat = new SimpleDateFormat("HH:mm");
//                    Date sunriseTransformed = (simpleDateFormat.parse(sunrise));
//                    Log.d("transformed", simpleDateFormat.format(sunriseTransformed));


                    DateTimeFormatter formatter= DateTimeFormatter.ofPattern("HH:mm");

                    LocalDateTime sunrisetransform = LocalDateTime.parse(sunrise.split("\\+")[0]);
                    LocalDateTime sunsettransform = LocalDateTime.parse(sunset.split("\\+")[0]);
                    LocalDateTime ghSunriseTransform = LocalDateTime.parse(goldenhoursunrise.split("\\+")[0]);
                    LocalDateTime ghSunsetTransform = LocalDateTime.parse(goldenhoursunset.split("\\+")[0]);

                    sunriseTxt.setText(sunrisetransform.format(formatter));
                    sunsetTxt.setText(sunsettransform.format(formatter));
                    sunrisetransform=sunrisetransform.plusMinutes(30);
                    goldenhoursunriseTsxt.setText(ghSunriseTransform.format(formatter) + "-" + (sunrisetransform.format(formatter)));
                    sunsettransform = sunsettransform.plusMinutes(30);
                    goldenhoursunsetTxt.setText(ghSunsetTransform.format(formatter)+ "-" + (sunsettransform.format(formatter)));


                    sunrisetransform=sunrisetransform.plusMinutes(30);
                    Log.d("transformed", sunrisetransform.format(formatter));




                } catch (JSONException e) {
                    e.printStackTrace();

                }

//                catch (ParseException e) {
//                    e.printStackTrace();
//                }


//                String sunriseTransformed = (sunrise.split(" ")[0]).substring(0,sunrise.lastIndexOf(":"));
//                String sunsetTransformed = (sunset.split(" ")[0]).substring(0,sunset.lastIndexOf(":"));
//                String ghSunriseTransformed = (goldenhoursunrise.split(" ")[0]).substring(0,goldenhoursunrise.lastIndexOf(":"));
//                String ghSunsetTransformed = (goldenhoursunset.split(" ")[0]).substring(0,goldenhoursunset.lastIndexOf(":"));
//
//
//
//                sunriseTxt.setText(sunriseTransformed);
//                sunsetTxt.setText(sunsetTransformed);
//                goldenhoursunriseTsxt.setText(ghSunriseTransformed + " - " + sunriseTransformed);
//                goldenhoursunsetTxt.setText(sunsetTransformed + " - " + ghSunsetTransformed);

                Log.d("sunrise", sunrise);
            }



        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Response", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private String getURL() {
        StringBuilder placeURL = new StringBuilder("https://api.sunrise-sunset.org/json?");
        placeURL.append("lat=" + latitude + "&lng=" + longitude);
        placeURL.append("&date=" + year_x+"-"+(month_x+1)+"-"+day_x);
        placeURL.append("&formatted=0");
        Log.d("url", placeURL.toString());
        return (placeURL.toString());
    }


}
