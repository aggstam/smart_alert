// -------------------------------------------------------------
//
// This auxiliary class implements an alarming countdown mechanism,
// enabling users to abort false emergencies.
// When a fall or earthquake emergency is detected, an alarm sound
// starts playing and a countdown starts.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.listener;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.stamatiou.entities.EmergencyAlertStatus;
import com.stamatiou.entities.EmergencyAlertType;
import com.stamatiou.smartalert.EmergencyAlertHandler;
import com.stamatiou.smartalert.R;

public class CountDown extends CountDownTimer {

    private Boolean running;
    private final Activity activity;
    private final TextView countDownTimerView;
    private final Button abortButton;
    private final MediaPlayer mediaPlayer;
    private Location location;
    private int finishMessage;
    private EmergencyAlertType type;

    public CountDown(Activity activity) {
        super(30000, 1000);
        this.running = false;
        this.activity = activity;
        this.mediaPlayer = MediaPlayer.create(activity.getApplicationContext(), R.raw.alert_sound);
        this.mediaPlayer.setLooping(true);
        this.countDownTimerView = (TextView) activity.findViewById(R.id.countDownTimerView);
        this.abortButton = (Button) activity.findViewById(R.id.abortButton);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        //Log.i("message","OnTick method started.");
        try {
            countDownTimerView.setText(activity.getString(R.string.seconds_remaining) + millisUntilFinished / 1000);
            //Log.i("message","OnTick method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnTick method:" + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // When countdown finishes, emergency alert is reported by calling the EmergencyAlertHandler class.
    @Override
    public void onFinish() {
        Log.i("message","OnFinish method started.");
        try {
            activity.findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
            running = false;
            countDownTimerView.setText(finishMessage);
            disableAlert();
            startEmergencyAlertHandlerActivity(EmergencyAlertStatus.EXECUTED);
            activity.findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
            Log.i("message","OnFinish method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnFinish method:" + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
            activity.findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
        }
    }

    // Start the alarm mechanism.
    public void setTimer(Location location, int finishMessage, EmergencyAlertType type) {
        Log.i("message","SetTimer method started.");
        try {
            if (!running) {
                running = true;
                this.location = location;
                this.finishMessage = finishMessage;
                this.type = type;
                abortButton.setEnabled(true);
                mediaPlayer.start();
                this.start();
            }
            Log.i("message","SetTimer method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during SetTimer method:" + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Cancel the alarm mechanism. Aborted emergency alert is reported by calling the EmergencyAlertHandler class.
    public void cancelTimer() {
        Log.i("message","CancelTimer method started.");
        try {
            activity.findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
            this.cancel();
            running = false;
            countDownTimerView.setText(activity.getString(R.string.crisis_aborted));
            disableAlert();
            startEmergencyAlertHandlerActivity(EmergencyAlertStatus.ABORTED);
            activity.findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
            Log.i("message","CancelTimer method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during CancelTimer method:" + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
            activity.findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
        }
    }

    // Disables the alarm sound.
    private void disableAlert() {
        Log.i("message","DisableAlert method started.");
        try {
            abortButton.setEnabled(false);
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            }
            Log.i("message","DisableAlert method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during DisableAlert method:" + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    public Boolean isRunning() {
        return running;
    }

    // Emergency alert is reported by calling the EmergencyAlertHandler class.
    private void startEmergencyAlertHandlerActivity(EmergencyAlertStatus status) {
        Intent intent = new Intent(activity.getApplicationContext(), EmergencyAlertHandler.class);
        intent.putExtra("location", location);
        intent.putExtra("type", type);
        intent.putExtra("status", status);
        activity.startActivity(intent);
    }

}
