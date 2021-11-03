// -------------------------------------------------------------
//
// This Activity is used to monitor user's emergency alerts history.
// When a new emergency alert is created, list is refreshed.
// Network permissions are required.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stamatiou.entities.EmergencyAlert;
import com.stamatiou.entities.EmergencyAlertAdapter;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import java.util.ArrayList;
import java.util.List;

public class UserEmergencyAlertsHistoryActivity extends AppCompatActivity implements InternetConnectivityListener {

    private DatabaseReference userEmergencyAlertsReference;
    private List<EmergencyAlert> emergencyAlerts;
    private EmergencyAlertAdapter emergencyAlertsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_emergency_alerts_history);
        InternetAvailabilityChecker.init(this);
        InternetAvailabilityChecker.getInstance().addInternetConnectivityListener(this);
    }

    // User Emergency Alerts List initialization method.
    // Firebase value event listener for the user's emergency alerts is created.
    // Emergency Alerts list is refreshed in a live manner.
    private void userEmergencyAlertsHistoryInit() {
        Log.i("message","UserEmergencyAlertsHistoryInit method started.");
        try {
            this.setTitle(getString(R.string.user_emergency_alerts_history));
            findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            emergencyAlerts = new ArrayList<>();
            emergencyAlertsAdapter = new EmergencyAlertAdapter(emergencyAlerts);
            recyclerView.setAdapter(emergencyAlertsAdapter);

            userEmergencyAlertsReference = FirebaseDatabase.getInstance().getReference("emergency_alerts/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            userEmergencyAlertsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    emergencyAlerts.clear();
                    for (DataSnapshot emergencyAlert : dataSnapshot.getChildren()) {
                        emergencyAlerts.add(0, emergencyAlert.getValue(EmergencyAlert.class));
                    }
                    refreshEmergencyAlerts();
                    findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("message", "Failed to retrieve user emergency alerts. Error: " + databaseError.toException());
                    emergencyAlerts.clear();
                    refreshEmergencyAlerts();
                    Toast.makeText(getApplicationContext(), getString(R.string.emergency_alerts_activity_retrieval_failed), Toast.LENGTH_SHORT).show();
                    findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
                }
            });
            Log.i("message","UserEmergencyAlertsHistoryInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during UserEmergencyAlertsHistoryInit method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
            findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
        }
    }

    // Application listens to internet connectivity status.
    // When internet provider is disabled, user is informed via a message box.
    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        Log.i("message","OnInternetConnectivityChanged method started.");
        try {
            if (isConnected) {
                findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
                userEmergencyAlertsHistoryInit();
            } else {
                findViewById(R.id.recyclerView).setVisibility(View.INVISIBLE);
                ((TextView) findViewById(R.id.titleView)).setText(getString(R.string.internet_provider_disabled));
            }
            Log.i("message","OnInternetConnectivityChanged method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnInternetConnectivityChanged method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Refreshes Activity displayed list.
    private void refreshEmergencyAlerts() {
        if (emergencyAlerts.isEmpty()) {
            ((TextView) findViewById(R.id.titleView)).setText(getString(R.string.emergency_alerts_activity_no_alerts));
        }
        emergencyAlertsAdapter.notifyDataSetChanged();
    }

}
