// -------------------------------------------------------------
//
// This is the Detection Structure used by the application.
// Detection data: Uid, Latitude, Longitude and Timestamp.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.entities;

public class Detection {

    private String uid;
    private Double latitude;
    private Double longitude;
    private String timestamp;

    public static class Builder {

        private String uid;
        private Double latitude;
        private Double longitude;
        private String timestamp;

        public Builder() {}

        public Detection.Builder withUid(String uid) {
            this.uid = uid;
            return this;
        }

        public Detection.Builder withLatitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Detection.Builder withLongitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Detection.Builder withTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Detection build() {
            Detection detection = new Detection();
            detection.uid = this.uid;
            detection.latitude = this.latitude;
            detection.longitude = this.longitude;
            detection.timestamp = this.timestamp;
            return detection;
        }
    }

    private Detection() {}

    public String getUid() {
        return uid;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Detection{uid=" + uid + ", latitude=" + latitude + ", longitude=" + longitude + ", timestamp=" + timestamp + "}";
    }

}
