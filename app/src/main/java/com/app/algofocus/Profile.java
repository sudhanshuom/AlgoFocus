package com.app.algofocus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Profile extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };
    FirebaseAuth firebaseAuth;
    LocationManager manager;
    LocationRequest mLocationRequest;
    FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback;
    double latitude = 0, longitude = 0;
    long minTime = 3000;
    boolean wasGpsOff = false;
    TextView address;
    ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageButton logout = findViewById(R.id.logout);
        TextView name = findViewById(R.id.pr_name);
        TextView email = findViewById(R.id.pr_email);
        address = findViewById(R.id.address);
        loading = findViewById(R.id.pbar);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(Profile.this, UserLogIn.class));
            finish();
        } else {
            name.setText(user.getDisplayName());
            email.setText(user.getEmail());
        }

        /*
         * To Check if gps is enabled and permission is granted and generate Alert box accordingly.
         * */
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert manager != null;
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            wasGpsOff = true;
            buildAlertMessageNoGps();
        } else {
            if (!hasPermissions(this, PERMISSIONS)) {
                new AlertDialog
                        .Builder(this)
                        .setCancelable(false)
                        .setTitle("Permissions not provided")
                        .setMessage("This app uses location permission. click okay to grant the permission.")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (ActivityCompat.checkSelfPermission(Profile.this,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {

                                    ActivityCompat.requestPermissions(Profile.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                                }
                            }
                        });
            }
        }

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasPermissions(this, PERMISSIONS)) {
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location mLastLocation = locationResult.getLastLocation();
                    Log.e("lastlocf", mLastLocation.getLatitude() + "");
                    Log.e("lastlocf", mLastLocation.getLongitude() + "");

                }
            };
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            getLastLocation();
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Profile.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else if (wasGpsOff) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(Profile.this, MainActivity.class));
                    finish();
                }
            }, 700);
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, enable it?")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static boolean hasPermissions(Context context, String... permissions) {

        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }

    private void getLastLocation() {
        mFusedLocationClient.getLastLocation().addOnCompleteListener(
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            Log.e("lastlocf", location.getLatitude() + "");
                            Log.e("lastlocf", location.getLongitude() + "");

                            getAddress(location.getLatitude(), location.getLongitude());
                        }
                    }
                }
        );
    }

    /*
    * this function run when last known location is null
    * */
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    /*
    * For Getting address from latitude and longitude.
    * */
    private void getAddress(double latitude, double longitude) {

        Geocoder geoCoder = new Geocoder(Profile.this, Locale.getDefault());
        List<Address> addresses;
        int x = 0;
        try {
            addresses = geoCoder.getFromLocation(latitude, longitude, 1);
            if (addresses == null)
                return;

            Log.e("Address", addresses + "");

            address.setText(addresses.get(0).getAddressLine(0));
            loading.setVisibility(View.GONE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
