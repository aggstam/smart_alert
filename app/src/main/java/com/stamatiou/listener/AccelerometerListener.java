// -------------------------------------------------------------
//
// This auxiliary class is used by the application to detect fall
// and earthquake events, using devices accelerometer sensor.
// Class extends BroadcastReceiver to enable earthquake mode, when
// phone is charging and connected to internet.
// To verify an earthquake is happening, application checks records
// from close users submitted at the same time.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.listener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stamatiou.entities.Detection;
import com.stamatiou.entities.EmergencyAlertType;
import com.stamatiou.smartalert.R;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccelerometerListener extends BroadcastReceiver implements SensorEventListener, InternetConnectivityListener {

    private final Activity activity;
    private final SensorManager sensorManager;
    private final CountDown countDown;
    private Boolean earthquakeMode;
    private Location location;
    private int finishMessage;
    private EmergencyAlertType type;
    private Boolean isPowerConnected;
    private Boolean isNetworkConnected;
    private long lastUpdateTime;
    private float lastUpdatePositionsSum;
    private Boolean earthquakeDetecting;
    private final DatabaseReference otherUsersDetectionsReference;
    private List<Detection> otherUsersDetections;
    private ValueEventListener valueEventListener;

    public AccelerometerListener(Activity activity) {
        this.activity = activity;
        this.sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        this.countDown = new CountDown(activity);
        this.earthquakeMode = false;
        this.finishMessage = R.string.user_fallen;
        this.type = EmergencyAlertType.FALL;
        this.isPowerConnected = false;
        this.isNetworkConnected = false;
        this.earthquakeDetecting = false;
        this.otherUsersDetections = new ArrayList<>();
        this.otherUsersDetectionsReference = FirebaseDatabase.getInstance().getReference("detections");
        accelerometerListenerInit();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    // Class initialization method.
    // Application register the class as an accelerometer listener and a broadcast receiver.
    private void accelerometerListenerInit() {
        Log.i("message","accelerometerListenerInit method started.");
        try {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            activity.registerReceiver(this, filter);
            InternetAvailabilityChecker.init(activity);
            InternetAvailabilityChecker.getInstance().addInternetConnectivityListener(this);
            Log.i("message","accelerometerListenerInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during accelerometerListenerInit method:" + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // In case a movement is detected, processing starts based on enabled mode.
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Log.i("message","OnSensorChanged method started.");
        try {
            if (earthquakeMode) {
                earthquakeDetection(sensorEvent);
            } else {
                fallDetection(sensorEvent);
            }
            //Log.i("message","OnSensorChanged method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnSensorChanged method:" + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Fall detection mechanism.
    private void fallDetection(SensorEvent sensorEvent) {
        double rootSquare = Math.sqrt(Math.pow(sensorEvent.values[0], 2) + Math.pow(sensorEvent.values[1], 2) + Math.pow(sensorEvent.values[2], 2));
        if (rootSquare < 2.0) {
            Log.i("message","Fall detected!");
            countDown.setTimer(location, finishMessage, type);
        }
    }

    // Earthquake detection mechanism.
    // In case an earthquake is detected, application checks if other users submitted similar records in close distance,
    // to positively verify an earthquake is happening.
    private void earthquakeDetection(SensorEvent sensorEvent) {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastUpdateTime) > 50) {
            long difference = (currentTime - lastUpdateTime);
            lastUpdateTime = currentTime;
            float positionsSum = sensorEvent.values[0] + sensorEvent.values[1] + sensorEvent.values[2];
            float speed = Math.abs(positionsSum - lastUpdatePositionsSum) / difference * 10000;
            if (speed > 200 && !earthquakeDetecting) {
                if (location != null) {
                    earthquakeDetecting = true;
                    checkCloseUsers();
                } else {
                    Log.i("message","Location missing. Earthquake detection failed.");
                    earthquakeDetecting = false;
                }
            }
            lastUpdatePositionsSum = positionsSum;
        }
    }

    // Application creates an event listened which reads Firebase Detection records for 10 seconds.
    // Users with distance less than 5km are considered close.
    private void checkCloseUsers() {
        Log.i("message", "Starting earthquake detection...");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                otherUsersDetections.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Detection detection = child.getValue(Detection.class);
                    if (!detection.getUid().equals(uid) && distance(location.getLatitude(), location.getLongitude(), detection.getLatitude(), detection.getLongitude()) < 5) {
                        otherUsersDetections.add(0, detection);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("message", "Failed to retrieve other users detections. Error: " + databaseError.toException());
                Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
            }
        };
        otherUsersDetectionsReference.orderByChild("timestamp").startAt(String.valueOf(new Date().getTime())).addValueEventListener(valueEventListener);
        Detection detection = new Detection.Builder()
                                           .withUid(uid)
                                           .withLongitude(location.getLongitude())
                                           .withLatitude(location.getLatitude())
                                           .withTimestamp(String.valueOf(new Date().getTime()))
                                           .build();
        otherUsersDetectionsReference.push().setValue(detection);
        (new Handler()).postDelayed(this::checkCloseUsersResults, 10000);
    }

    // Application checks close users records, after the 10 second delay.
    // If close users report an earthquake detection, an earthquake emergency event is created.
    private void checkCloseUsersResults() {
        otherUsersDetectionsReference.removeEventListener(valueEventListener);
        if (otherUsersDetections.size() > 0) {
            countDown.setTimer(location, finishMessage, type);
        }
        earthquakeDetecting = false;
        Log.i("message", "Earthquake detection finished.");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Log.i("message","OnAccuracyChanged method started.");
        try {
            //Log.i("message","CountDownTimerAccelerometerListener accuracy changed.");
            //Log.i("message","OnAccuracyChanged method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnAccuracyChanged method:" + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Stops listener execution.
    public void stopListener() {
        Log.i("message","StopListener method started.");
        try {
            if (countDown.isRunning()) {
                countDown.cancelTimer();
            }
            earthquakeDetecting = false;
            sensorManager.unregisterListener(this);
            Log.i("message","StopListener method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during StopListener method:" + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Cancel count down mechanism.
    public void cancelTimer() {
        earthquakeDetecting = false;
        countDown.cancelTimer();
    }

    // Earthquake mode check is executed on broadcast receiver events.
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("message","OnReceive method started.");
        try {
            isPowerConnected = intent.getAction().equals(Intent.ACTION_POWER_CONNECTED);
            enableEarthquakeMode();
            Log.i("message","OnReceive method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnReceive method:" + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Earthquake mode check is executed on connectivity change events.
    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        Log.i("message","OnInternetConnectivityChanged method started.");
        try {
            isNetworkConnected = isConnected;
            enableEarthquakeMode();
            Log.i("message","OnInternetConnectivityChanged method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnInternetConnectivityChanged method:" + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Earthquake mode check.
    // Phone must be connected to the internet and a power source.
    private void enableEarthquakeMode() {
        if (isNetworkConnected && (isPowerConnected || ((BatteryManager) activity.getSystemService(Context.BATTERY_SERVICE)).isCharging())) {
            earthquakeMode = true;
            finishMessage = R.string.earthquake_detected;
            type = EmergencyAlertType.EARTHQUAKE;
            Log.i("message","Earthquake mode enabled.");
            Toast.makeText(activity, activity.getString(R.string.earthquake_mode_enabled), Toast.LENGTH_SHORT).show();
        } else {
            earthquakeMode = false;
            finishMessage = R.string.user_fallen;
            type = EmergencyAlertType.FALL;
            Log.i("message","Earthquake mode is disabled.");
        }
    }

    // Calculate distance between two Locations using their latitudes and longitudes.
    private double distance(Double lat1, Double lon1, Double lat2, Double lon2) {
        double lat1radian = Math.toRadians(lat1);
        double lon1radian = Math.toRadians(lon1);
        double lat2radian = Math.toRadians(lat2);
        double lon2radian = Math.toRadians(lon2);
        double earthRadius = 6371.01;
        return earthRadius * Math.acos((Math.sin(lat1radian) * Math.sin(lat2radian)) + (Math.cos(lat1radian) * Math.cos(lat2radian) * Math.cos(lon1radian - lon2radian)));
    }

}
