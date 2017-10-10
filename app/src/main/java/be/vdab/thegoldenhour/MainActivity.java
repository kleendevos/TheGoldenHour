package be.vdab.thegoldenhour;

import android.*;
import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private TextView mTextMessage;

    Button dateButton;
    int year_x,month_x, day_x;
    static final int DIALOG_ID = 0;
    TextView CurrentLocationAbove;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 777 ;
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
        textview.setText(day_x +" / " + (month_x+1) + " / " + year_x);

        showDialogOnClick();
        captureLocation(CurrentLocationAbove);



    }

    public void captureLocation (View view) {
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
            locationFound += getCityFromLocation (location.getLongitude(),location.getLatitude()) + ", " + getCountryFromLocation (location.getLongitude(),location.getLatitude());
        } catch (IOException e){
            e.printStackTrace();
        }
        CurrentLocationAbove.setText(locationFound);

        getURL(location.getLongitude(),location.getLatitude());


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
        Toast.makeText(MainActivity.this,"Provider Disabled", Toast.LENGTH_LONG).show();
    }
};

    private String getCountryFromLocation(double longitude, double latitude) throws IOException {
        String country = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

        }catch (IOException e){
            e.printStackTrace();
        }
        if(addresses != null && addresses.size()> 0) {
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

        }catch (IOException e){
            e.printStackTrace();
        }
        if(addresses != null && addresses.size()> 0) {
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


            textview.setText(day_x +" / " + (month_x+1) + " / " + year_x);
           // Toast.makeText(MainActivity.this,year_x + "/" + (month_x+1) + "/" + day_x, Toast.LENGTH_LONG).show();

        }
    };

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this,"GPS permission needed to capture your location", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public void showDialogOnClick () {
        dateButton = (Button) findViewById(R.id.changeDate);
        dateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showDialog(DIALOG_ID);

            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id){
        if (id == DIALOG_ID)
            return new DatePickerDialog(this,dpickerListener,year_x,month_x,day_x);
        return null;
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void getSunrise (double lat, double longi) {
        TextView sunrise = (TextView) findViewById(R.id.Sunrise);


    }

    private String getURL (double latitude, double longitude) {
        StringBuilder placeURL = new StringBuilder("https://api.sunrise-sunset.org/json?");
        placeURL.append("lat="+ longitude + "&lng=" + latitude);
        Log.d("url", placeURL.toString());
        return (placeURL.toString());
    }


}
