// -------------------------------------------------------------
//
// This Activity is used to create Emergency Alert records
// and send SMS messages to user's emergency contacts.
// SMS permissions are required.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.smartalert;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stamatiou.entities.EmergencyAlert;
import com.stamatiou.entities.EmergencyAlertStatus;
import com.stamatiou.entities.EmergencyAlertType;
import com.stamatiou.entities.EmergencyContact;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmergencyAlertHandler extends AppCompatActivity {

    private final static int REQ_CODE = 123;
    private EmergencyAlert emergencyAlert;
    private List<EmergencyContact> emergencyContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_alert);
        handle();
    }

    // Emergency Alert creation handle.
    // Alert is created and depending on its status, SMS sending process is executed.
    private void handle() {
        Log.i("message","Handle method started.");
        try {
            findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
            Location location = (Location) getIntent().getExtras().get("location");
            if (location != null) {
                EmergencyAlertType type = (EmergencyAlertType) getIntent().getExtras().get("type");
                EmergencyAlertStatus status = (EmergencyAlertStatus) getIntent().getExtras().get("status");
                Log.i("message","Generating EmergencyAlert record...");
                DatabaseReference userEmergencyAlertsReference = FirebaseDatabase.getInstance().getReference("emergency_alerts/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
                emergencyAlert = new EmergencyAlert.Builder()
                                                   .withEmergencyAlertType(type)
                                                   .withEmergencyAlertStatus(status)
                                                   .withLatitude(location.getLatitude())
                                                   .withLongitude(location.getLongitude())
                                                   .withTimestamp(new Date())
                                                   .build();
                userEmergencyAlertsReference.push().setValue(emergencyAlert);
                Log.i("message","EmergencyAlert record generated successfully.");
                if (status.equals(EmergencyAlertStatus.EXECUTED)) {
                    initSMSMessageSend();
                } else {
                    SmartAlertActivity.disableProgressBar();
                    finish();
                }
                Toast.makeText(this, getString(R.string.emergency_alert_submitted), Toast.LENGTH_SHORT).show();
            } else {
                Log.i("message","Location missing. EmergencyAlert record generation failed.");
                Toast.makeText(this, getString(R.string.location_emergency_alert_event), Toast.LENGTH_SHORT).show();
                SmartAlertActivity.disableProgressBar();
                finish();
            }
            Log.i("message","Handle method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during Handle method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
            SmartAlertActivity.disableProgressBar();
            finish();
        }
    }

    // Initialization of SMS send process.
    // User's emergency contacts are retrieved from Firebase.
    // Process works in an asynchronous manner.
    private void initSMSMessageSend() {
        Log.i("message","Setting SMS async send.");
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            DatabaseReference userEmergencyContactsReference = FirebaseDatabase.getInstance().getReference("emergency_contacts/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            userEmergencyContactsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("message","OnDataChange method started.");
                    try {
                        emergencyContacts = new ArrayList<>();
                        for (DataSnapshot emergencyContact : dataSnapshot.getChildren()) {
                            emergencyContacts.add(0, emergencyContact.getValue(EmergencyContact.class));
                        }
                        if (emergencyContacts.isEmpty()) {
                            Log.i("message","No emergency contacts exists.");
                            Toast.makeText(getApplicationContext(), getString(R.string.no_emergency_contacts), Toast.LENGTH_SHORT).show();
                            SmartAlertActivity.disableProgressBar();
                            finish();
                        } else {
                            checkSMSPermissions();
                        }
                    } catch (Exception e) {
                        Log.i("message","Exception during OnDataChange method:" + e.getMessage());
                        Toast.makeText(getApplicationContext(), getString(R.string.exception), Toast.LENGTH_SHORT).show();
                        SmartAlertActivity.disableProgressBar();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("message", "Failed to retrieve user emergency contacts. Error: " + databaseError.toException());
                    Toast.makeText(getApplicationContext(), getString(R.string.exception_emergency_contacts), Toast.LENGTH_SHORT).show();
                    SmartAlertActivity.disableProgressBar();
                    finish();
                }
            });
        } else {
            Log.i("message","Internet provider is disabled...");
            Toast.makeText(this, getString(R.string.sms_internet_disabled), Toast.LENGTH_SHORT).show();
            SmartAlertActivity.disableProgressBar();
            finish();
        }
    }

    // Sends SMS messages to user's emergency contacts.
    // SMS content is based on the emergency alert type.
    private void sendSMStoEmergencyContacts() {
        Log.i("message","SendSMStoEmergencyContacts method started.");
        try {
            Log.i("message","Sending SMS messages to emergency contacts...");
            for (EmergencyContact emergencyContact : emergencyContacts) {
                SmsManager smsManager = SmsManager.getDefault();
                StringBuilder sb = new StringBuilder().append(getString(R.string.sos));
                if (emergencyAlert.getType().equals(EmergencyAlertType.FALL)) {
                    sb.append(getString(R.string.sos_fallen));
                } else if (emergencyAlert.getType().equals(EmergencyAlertType.FIRE)) {
                    sb.append(getString(R.string.sos_fire));
                } else {
                    sb.append(getString(R.string.sos_earthquake));
                }
                sb.append(String.format("%.6f", emergencyAlert.getLatitude())).append(" - ").append(String.format("%.6f", emergencyAlert.getLongitude()));
                smsManager.sendTextMessage(emergencyContact.getPhone(), null, sb.toString(), null, null);
            }
            Toast.makeText(getApplicationContext(), getString(R.string.sms_success), Toast.LENGTH_SHORT).show();
            Log.i("message","SendSMStoEmergencyContacts method completed successfully.");
        } catch (Exception e) {
            Log.i("message","Exception during SendSMStoEmergencyContacts method:" + e.getMessage());
            Toast.makeText(getApplicationContext(), getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
        SmartAlertActivity.disableProgressBar();
        finish();
    }

    // Check SMS permissions and sends SMS messages to user's emergency contacts.
    // If permissions are not granted, application requests them.
    private void checkSMSPermissions() {
        Log.i("message","checkSMSPermissions method started.");
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQ_CODE);
            } else {
                sendSMStoEmergencyContacts();
            }
            Log.i("message","checkSMSPermissions method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during checkSMSPermissions method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
            SmartAlertActivity.disableProgressBar();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("message","OnRequestPermissionsResult method started.");
        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                sendSMStoEmergencyContacts();
            } else {
                Log.i("message", "ok");
            }
            Log.i("message","OnRequestPermissionsResult method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnRequestPermissionsResult method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
            SmartAlertActivity.disableProgressBar();
            finish();
        }
    }

}
