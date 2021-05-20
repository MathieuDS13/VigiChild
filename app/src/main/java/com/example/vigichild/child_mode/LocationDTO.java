package com.example.vigichild.child_mode;

import android.location.Location;

public class LocationDTO {

    public double longitude;
    public double latitude;
    public long date;

    public LocationDTO(Location location) {
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        this.date = location.getTime();
    }

    public LocationDTO() {
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
