// -------------------------------------------------------------
//
// This Activity is used to create Emergency Alert records of
// Fire type, by submitting a photograph.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.listener;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stamatiou.entities.EmergencyAlertStatus;
import com.stamatiou.entities.EmergencyAlertType;
import com.stamatiou.smartalert.EmergencyAlertHandler;
import com.stamatiou.smartalert.R;
import com.stamatiou.smartalert.SmartAlertActivity;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FireListener extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private StorageReference userImagesReference;
    private Location location;
    private SimpleDateFormat fileNameFormatter = new SimpleDateFormat("yyMMdd_hh_mm_ss'.jpg'");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fireListenerInit();
    }

    // Activity initialization method.
    // Application executes an image capture intent, which open users camera.
    public void fireListenerInit() {
        Log.i("message","FireListenerInit method started.");
        try {
            userImagesReference = FirebaseStorage.getInstance().getReference("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            location = (Location) getIntent().getExtras().get("location");
            if (location != null) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } else {
                Toast.makeText(this, getString(R.string.location_fire_event), Toast.LENGTH_SHORT).show();
                SmartAlertActivity.disableProgressBar();
                finish();
            }
            Log.i("message","FireListenerInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during FireListenerInit method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Image capture intent result processing.
    // If user actually took a picture, file is uploaded to Firebase.
    // Fire emergency alert is created.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("message","OnActivityResult method started.");
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Log.i("message","Fire report operation started.");
                StorageReference imageRef = userImagesReference.child(fileNameFormatter.format(new Date()));
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();
                UploadTask uploadTask = imageRef.putBytes(imageData);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.i("message","Exception during file upload:" + exception.getMessage());
                        Toast.makeText(getApplicationContext(), getString(R.string.exception_file_upload), Toast.LENGTH_SHORT).show();
                        SmartAlertActivity.disableProgressBar();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i("message","File upload was successful!");
                        startEmergencyAlertHandlerActivity(EmergencyAlertStatus.EXECUTED);
                    }
                });
            } else {
                Log.i("message","Fire report operation cancelled.");
                startEmergencyAlertHandlerActivity(EmergencyAlertStatus.ABORTED);
            }
            Log.i("message","OnActivityResult method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnActivityResult method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
            SmartAlertActivity.disableProgressBar();
        }
        finish();
    }

    // Emergency alert is reported by calling the EmergencyAlertHandler class.
    private void startEmergencyAlertHandlerActivity(EmergencyAlertStatus status) {
        Intent intent = new Intent(getApplicationContext(), EmergencyAlertHandler.class);
        intent.putExtra("location", location);
        intent.putExtra("type", EmergencyAlertType.FIRE);
        intent.putExtra("status", status);
        startActivity(intent);
    }

}
