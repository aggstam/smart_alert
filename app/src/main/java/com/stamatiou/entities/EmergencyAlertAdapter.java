// -------------------------------------------------------------
//
// This is the EmergencyAlert Adapter used by the application, to
// populate the corresponding Recycler View in UserEmergencyAlertsHistory Activity.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.entities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.stamatiou.smartalert.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class EmergencyAlertAdapter extends RecyclerView.Adapter<EmergencyAlertAdapter.EmergencyAlertViewHolder> {

    private List<EmergencyAlert> emergencyAlerts;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static class EmergencyAlertViewHolder extends RecyclerView.ViewHolder {

        TextView typeView, statusView, latitudeView, longitudeView, timestampView;

        public EmergencyAlertViewHolder(View itemView) {
            super(itemView);
            this.typeView = itemView.findViewById(R.id.typeView);
            this.statusView = itemView.findViewById(R.id.statusView);
            this.latitudeView = itemView.findViewById(R.id.latitudeView);
            this.longitudeView = itemView.findViewById(R.id.longitudeView);
            this.timestampView = itemView.findViewById(R.id.timestampView);
        }
    }

    public EmergencyAlertAdapter(List<EmergencyAlert> emergencyAlerts) {
        this.emergencyAlerts = emergencyAlerts;
    }

    @Override
    public EmergencyAlertViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emergency_alert_card_layout, parent, false);
        EmergencyAlertViewHolder emergencyAlertViewHolder = new EmergencyAlertViewHolder(view);
        return emergencyAlertViewHolder;
    }

    @Override
    public void onBindViewHolder(EmergencyAlertViewHolder holder, int position) {

        TextView typeView = holder.typeView;
        TextView statusView = holder.statusView;
        TextView latitudeView = holder.latitudeView;
        TextView longitudeView = holder.longitudeView;
        TextView timestampView = holder.timestampView;

        typeView.setText(typeView.getResources().getString(R.string.type) + emergencyAlerts.get(position).getType().toString().toLowerCase());
        statusView.setText(statusView.getResources().getString(R.string.status) + emergencyAlerts.get(position).getStatus().toString().toLowerCase());
        latitudeView.setText(latitudeView.getResources().getString(R.string.latitude) + String.format("%.6f", emergencyAlerts.get(position).getLatitude()));
        longitudeView.setText(longitudeView.getResources().getString(R.string.longitude) + String.format("%.6f", emergencyAlerts.get(position).getLongitude()));
        timestampView.setText(timestampView.getResources().getString(R.string.timestamp) + dateFormatter.format(emergencyAlerts.get(position).getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return emergencyAlerts.size();
    }

}
