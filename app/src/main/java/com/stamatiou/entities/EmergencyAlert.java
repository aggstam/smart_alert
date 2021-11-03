// -------------------------------------------------------------
//
// This is the EmergencyAlert Structure used by the application.
// EmergencyAlert data: Type, Status, Latitude, Longitude and Timestamp.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.entities;

import java.util.Date;

public class EmergencyAlert {

    private EmergencyAlertType type;
    private EmergencyAlertStatus status;
    private Double latitude;
    private Double longitude;
    private Date timestamp;

    public static class Builder {

        private EmergencyAlertType type;
        private EmergencyAlertStatus status;
        private Double latitude;
        private Double longitude;
        private Date timestamp;

        public Builder() {}

        public Builder withEmergencyAlertType(EmergencyAlertType type) {
            this.type = type;
            return this;
        }

        public Builder withEmergencyAlertStatus(EmergencyAlertStatus status) {
            this.status = status;
            return this;
        }

        public Builder withLatitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder withLongitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder withTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public EmergencyAlert build() {
            EmergencyAlert emergencyAlert = new EmergencyAlert();
            emergencyAlert.type = this.type;
            emergencyAlert.status = this.status;
            emergencyAlert.latitude = this.latitude;
            emergencyAlert.longitude = this.longitude;
            emergencyAlert.timestamp = this.timestamp;
            return emergencyAlert;
        }
    }

    private EmergencyAlert() {}

    public EmergencyAlertType getType() {
        return type;
    }

    public EmergencyAlertStatus getStatus() {
        return status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "EmergencyAlert{type=" + type + ", status=" + status + ", latitude=" + latitude + ", longitude=" + longitude + ", timestamp=" + timestamp + "}";
    }

}
