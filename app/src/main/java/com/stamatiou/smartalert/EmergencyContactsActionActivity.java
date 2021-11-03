// -------------------------------------------------------------
//
// This Activity is used to add, modify and delete Emergency Contact records.
// Records are stored in Firebase.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.smartalert;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stamatiou.entities.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActionActivity extends AppCompatActivity {

    private DatabaseReference userEmergencyContactsReference;
    private List<EmergencyContact> emergencyContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts_action);
        Integer mode = (Integer) getIntent().getExtras().get("mode");
        emergencyContacts = (ArrayList<EmergencyContact>) getIntent().getSerializableExtra("emergencyContacts");
        userEmergencyContactsReference = FirebaseDatabase.getInstance().getReference("emergency_contacts/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        if (mode == 0) {
            addEmergencyContact();
        } else if (mode == 1) {
            editEmergencyContact();
        } else {
            deleteEmergencyContact();
        }
    }

    // Activity initialization method, for "Add Emergency Contact" mode.
    private void addEmergencyContact() {
        Log.i("message","AddEmergencyContact method started.");
        try {
            this.setTitle(getString(R.string.emergency_contacts_action_add));
            findViewById(R.id.deleteMessageTextView).setVisibility(View.GONE);
            findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addEmergencyContactSubmitAction();
                }
            });
            Log.i("message","AddEmergencyContact method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during AddEmergencyContact method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Application validates submitted fields.
    // On successful fields validation, a new Emergency Contact record is created.
    private void addEmergencyContactSubmitAction() {
        Log.i("message","AddEmergencyContactSubmitAction method started.");
        try {
            findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
            Boolean formValidation = validateFormFields();
            if (formValidation) {
                Log.i("message","Form Fields validation succeeded. Generating record...");
                EmergencyContact emergencyContact = new EmergencyContact.Builder()
                                                                        .withName(((EditText) findViewById(R.id.nameEditText)).getText().toString())
                                                                        .withSurname(((EditText) findViewById(R.id.surnameEditText)).getText().toString())
                                                                        .withPhone(((EditText) findViewById(R.id.phoneEditText)).getText().toString())
                                                                        .build();
                userEmergencyContactsReference.push().setValue(emergencyContact);
            } else {
                Log.i("message","Form Fields validation failed.");
            }
            Log.i("message","AddEmergencyContactSubmitAction method completed successfully.");
            findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
            if (formValidation) finish();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during AddEmergencyContactSubmitAction method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
            findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
        }
    }

    // Activity initialization method, for "Edit Emergency Contact" mode.
    private void editEmergencyContact() {
        Log.i("message","EditEmergencyContact method started.");
        try {
            this.setTitle(getString(R.string.emergency_contacts_action_edit));
            ((EditText) findViewById(R.id.nameEditText)).setText((String) getIntent().getExtras().get("name"));
            ((EditText) findViewById(R.id.surnameEditText)).setText((String) getIntent().getExtras().get("surname"));
            ((EditText) findViewById(R.id.phoneEditText)).setText((String) getIntent().getExtras().get("phone"));
            findViewById(R.id.deleteMessageTextView).setVisibility(View.GONE);
            findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editEmergencyContactSubmitAction();
                }
            });
            Log.i("message","EditEmergencyContact method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during EditEmergencyContact method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Application validates submitted fields.
    // On successful fields validation, requested Emergency Contact record is modified.
    private void editEmergencyContactSubmitAction() {
        Log.i("message","EditEmergencyContactSubmitAction method started.");
        try {
            findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
            Boolean formValidation = validateFormFields();
            if (formValidation) {
                Log.i("message","Form Fields validation succeeded. Modifying record...");
                EmergencyContact originalEmergencyContact = emergencyContacts.stream()
                                                                             .filter(emergencyContact ->
                                                                                        emergencyContact.getName().equals((String) getIntent().getExtras().get("name"))
                                                                                        && emergencyContact.getSurname().equals((String) getIntent().getExtras().get("surname"))
                                                                                        && emergencyContact.getPhone().equals((String) getIntent().getExtras().get("phone")))
                                                                             .findAny()
                                                                             .orElse(null);
                if (originalEmergencyContact != null) {
                    emergencyContacts.remove(originalEmergencyContact);
                }
                EmergencyContact newEmergencyContact = new EmergencyContact.Builder()
                                                                           .withName(((EditText) findViewById(R.id.nameEditText)).getText().toString())
                                                                           .withSurname(((EditText) findViewById(R.id.surnameEditText)).getText().toString())
                                                                           .withPhone(((EditText) findViewById(R.id.phoneEditText)).getText().toString())
                                                                           .build();
                emergencyContacts.add(newEmergencyContact);
                userEmergencyContactsReference.removeValue();
                for (EmergencyContact emergencyContact : emergencyContacts) {
                    userEmergencyContactsReference.push().setValue(emergencyContact);
                }
            } else {
                Log.i("message","Form Fields validation failed.");
            }
            Log.i("message","EditEmergencyContactSubmitAction method completed successfully.");
            findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
            if (formValidation) finish();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during EditEmergencyContactSubmitAction method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
            findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
        }
    }

    // Activity initialization method, for "Delete Emergency Contact" mode.
    private void deleteEmergencyContact() {
        Log.i("message","DeleteEmergencyContact method started.");
        try {
            this.setTitle(getString(R.string.emergency_contacts_action_delete));
            StringBuilder messageBuilder = new StringBuilder().append("Are you sure that you want to delete Emergency Contact: ")
                                                              .append((String) getIntent().getExtras().get("name").toString())
                                                              .append(" ").append((String) getIntent().getExtras().get("surname"))
                                                              .append(" (").append((String) getIntent().getExtras().get("phone")).append(")?");
            ((TextView) findViewById(R.id.deleteMessageTextView)).setText(messageBuilder.toString());
            findViewById(R.id.nameTextView).setVisibility(View.GONE);
            findViewById(R.id.nameEditText).setVisibility(View.GONE);
            findViewById(R.id.surnameTextView).setVisibility(View.GONE);
            findViewById(R.id.surnameEditText).setVisibility(View.GONE);
            findViewById(R.id.phoneTextView).setVisibility(View.GONE);
            findViewById(R.id.phoneEditText).setVisibility(View.GONE);
            findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteEmergencyContactSubmitAction();
                }
            });
            Log.i("message","DeleteEmergencyContact method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during DeleteEmergencyContact method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Application validates submitted fields.
    // On successful fields validation, requested Emergency Contact record is removed.
    private void deleteEmergencyContactSubmitAction() {
        Log.i("message","DeleteEmergencyContactSubmitAction method started.");
        try {
            findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
            EmergencyContact originalEmergencyContact = emergencyContacts.stream()
                                                                         .filter(emergencyContact ->
                                                                                     emergencyContact.getName().equals((String) getIntent().getExtras().get("name"))
                                                                                     && emergencyContact.getSurname().equals((String) getIntent().getExtras().get("surname"))
                                                                                     && emergencyContact.getPhone().equals((String) getIntent().getExtras().get("phone")))
                                                                         .findAny()
                                                                         .orElse(null);
            if (originalEmergencyContact != null) {
                emergencyContacts.remove(originalEmergencyContact);
            }
            userEmergencyContactsReference.removeValue();
            for (EmergencyContact emergencyContact : emergencyContacts) {
                userEmergencyContactsReference.push().setValue(emergencyContact);
            }
            Log.i("message","DeleteEmergencyContactSubmitAction method completed successfully.");
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during DeleteEmergencyContactSubmitAction method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
            findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
        }
    }

    // Validates User submitted Movie fields.
    // Fields cannot be empty and must have a specific length.
    // 'Phone' field must also follow a specific pattern.
    private Boolean validateFormFields() {
        Boolean valid = true;
        EditText nameEditText = findViewById(R.id.nameEditText);
        if (nameEditText.getText().toString().trim().length() == 0) {
            nameEditText.setError(getString(R.string.emergency_contacts_action_validation_error_name));
            valid = false;
        } else if (nameEditText.getText().toString().trim().length() > 50) {
            nameEditText.setError(getString(R.string.emergency_contacts_action_validation_error_name_length));
            valid = false;
        }
        EditText surnameEditText = findViewById(R.id.surnameEditText);
        if (surnameEditText.getText().toString().trim().length() == 0) {
            surnameEditText.setError(getString(R.string.emergency_contacts_action_validation_error_surname));
            valid = false;
        } else if (surnameEditText.getText().toString().trim().length() > 50) {
            surnameEditText.setError(getString(R.string.emergency_contacts_action_validation_error_surname_length));
            valid = false;
        }
        EditText phoneEditText = findViewById(R.id.phoneEditText);
        if (phoneEditText.getText().toString().trim().length() == 0) {
            phoneEditText.setError(getString(R.string.emergency_contacts_action_validation_error_phone));
            valid = false;
        } else if (!phoneEditText.getText().toString().matches("69[0-9]{8}")) {
            phoneEditText.setError(getString(R.string.emergency_contacts_action_validation_error_phone_pattern));
            valid = false;
        }
        return valid;
    }

}
