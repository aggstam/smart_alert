// -------------------------------------------------------------
//
// This Activity is used to monitor user's activity and/or report emergencies.
// Application uses two listeners to manage emergencies:
//      1. AccelerometerListener: SensorEventListener used to monitor falls and earthquakes.
//      2. FireListener: Activity used to access user's camera for submitting a fire photograph.
// User can navigate to rest application activities using the top right menu.
// Location permissions are required.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.smartalert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.stamatiou.listener.AccelerometerListener;
import com.stamatiou.listener.FireListener;

public class SmartAlertActivity extends AppCompatActivity implements LocationListener {

    private final static int REQ_CODE = 765;
    private AccelerometerListener accelerometerListener;
    private static ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_alert);
        smartAlertInit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.smart_alert_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        if (id == R.id.emergencyContacts) {
            intent = new Intent(this, EmergencyContactsActivity.class);
        } else if (id == R.id.userEmergencyAlertsHistory) {
            intent = new Intent(this, UserEmergencyAlertsHistoryActivity.class);
        } else {
            finish();
        }
        if (intent != null) {
            startActivity(intent);
        }
        return true;
    }

    // Activity initialization method.
    // AccelerometerListener and FireListener are initialized.
    // Application checks appropriate location permissions.
    private void smartAlertInit() {
        Log.i("message","SmartAlertInit method started.");
        try {
            progressBar = findViewById(R.id.progressBar_cyclic);
            accelerometerListenerInit();
            checkLocationPermission();
            fireListenerInit();
            findViewById(R.id.abortButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accelerometerListener.cancelTimer();
                }
            });
            Log.i("message","SmartAlertInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during SmartAlertInit method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // AccelerometerListener initialization method.
    private void accelerometerListenerInit() {
        Log.i("message","AccelerometerListenerInit method started.");
        try {
            accelerometerListener = new AccelerometerListener(this);
            Log.i("message","AccelerometerListenerInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during AccelerometerListenerInit method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // FireListener initialization method.
    // Fire listener is bound to the fire button.
    private void fireListenerInit() {
        Log.i("message","FireListenerInit method started.");
        try {
            findViewById(R.id.fireButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(getApplicationContext(), FireListener.class);
                    intent.putExtra("location", accelerometerListener.getLocation());
                    startActivity(intent);
                }
            });
            Log.i("message","FireListenerInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during FireListenerInit method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    public static void disableProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    // Check location permissions.
    // If permissions are not granted, application requests them.
    // User is informed on the permissions status via a message box.
    private void checkLocationPermission() {
        Log.i("message","CheckLocationPermission method started.");
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_CODE);
            } else {
                ((LocationManager) getSystemService(LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            accelerometerListener.setLocation(location);
                        }
                    }
                });
            }
            Log.i("message","CheckLocationPermission method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during CheckLocationPermission method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("message","OnRequestPermissionsResult method started.");
        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ((TextView) findViewById(R.id.messageView)).setText(getString(R.string.location_permission_not_granted));
                checkLocationPermission();
            } else {
                ((LocationManager) getSystemService(LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            accelerometerListener.setLocation(location);
                        }
                    }
                });
            }
            Log.i("message","OnRequestPermissionsResult method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnRequestPermissionsResult method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // On location changed, AccelerometerListener location is updated.
    @Override
    public void onLocationChanged(Location location) {
        //Log.i("message","OnLocationChanged method started.");
        try {
            accelerometerListener.setLocation(location);
            //Log.i("message","OnLocationChanged method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnLocationChanged method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // On Location Provider status changed, user is informed via a message box.
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("message","OnStatusChanged method started.");
        try {
            ((TextView) findViewById(R.id.messageView)).setText(getString(R.string.location_provider_status_changed));
            Log.i("message","OnStatusChanged method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnStatusChanged method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // On Location provider enable, application checks for appropriate permissions.
    @Override
    public void onProviderEnabled(String provider) {
        Log.i("message","OnProviderEnabled method started.");
        try {
            ((TextView) findViewById(R.id.messageView)).setText(null);
            checkLocationPermission();
            Log.i("message","OnProviderEnabled method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnProviderEnabled method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // On Location Provider disable, user is informed via a message box.
    @Override
    public void onProviderDisabled(String provider) {
        Log.i("message","OnProviderDisabled method started.");
        try {
            ((TextView) findViewById(R.id.messageView)).setText(getString(R.string.location_provider_disabled));
            Log.i("message","OnProviderDisabled method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnProviderDisabled method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // On Back press, AccelerometerListener is disabled.
    @Override
    public void onBackPressed() {
        accelerometerListener.stopListener();
        super.onBackPressed();
    }

}
