// -------------------------------------------------------------
//
// This Activity is used to display user's emergency contact records.
// User can add/modify/delete records.
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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stamatiou.entities.EmergencyContact;
import com.stamatiou.entities.EmergencyContactAdapter;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity implements InternetConnectivityListener {

    private DatabaseReference userEmergencyContactsReference;
    private List<EmergencyContact> emergencyContacts;
    private EmergencyContactAdapter emergencyContactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);
        InternetAvailabilityChecker.init(this);
        InternetAvailabilityChecker.getInstance().addInternetConnectivityListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.emergency_contacts_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, EmergencyContactsActionActivity.class);
        intent.putExtra("mode", 0);
        startActivity(intent);
        return true;
    }

    // User Emergency Contacts List initialization method.
    // Firebase value event listener for the user's emergency contacts is created.
    // Emergency Contacts list is refreshed in a live manner.
    private void userEmergencyContactsListInit() {
        Log.i("message","UserEmergencyContactsListInit method started.");
        try {
            this.setTitle(getString(R.string.emergency_contacts));
            findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            emergencyContacts = new ArrayList<>();
            emergencyContactsAdapter = new EmergencyContactAdapter(emergencyContacts);
            recyclerView.setAdapter(emergencyContactsAdapter);

            userEmergencyContactsReference = FirebaseDatabase.getInstance().getReference("emergency_contacts/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            userEmergencyContactsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    emergencyContacts.clear();
                    for (DataSnapshot emergencyContact : dataSnapshot.getChildren()) {
                        emergencyContacts.add(0, emergencyContact.getValue(EmergencyContact.class));
                    }
                    refreshEmergencyContacts();
                    findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("message", "Failed to retrieve user emergency contacts. Error: " + databaseError.toException());
                    emergencyContacts.clear();
                    refreshEmergencyContacts();
                    Toast.makeText(getApplicationContext(), getString(R.string.emergency_contacts_activity_retrieval_failed), Toast.LENGTH_SHORT).show();
                    findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
                }
            });
            Log.i("message","UserEmergencyContactsListInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during UserEmergencyContactsListInit method:" + e.getMessage());
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
                userEmergencyContactsListInit();
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
    private void refreshEmergencyContacts() {
        if (emergencyContacts.isEmpty()) {
            ((TextView) findViewById(R.id.titleView)).setText(getString(R.string.emergency_contacts_activity_no_contacts));
        }
        emergencyContactsAdapter.notifyDataSetChanged();
    }

}
