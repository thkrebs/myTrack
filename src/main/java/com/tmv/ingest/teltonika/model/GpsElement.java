package com.tmv.ingest.teltonika.model;

import lombok.Getter;

public class GpsElement {
    public static final short INVALID_GPS_SPEED = 255;

    @Getter private float lng;
    @Getter private float lat;
    @Getter private short altitude;
    @Getter private short angle;
    @Getter private byte satellites;
    @Getter private short speed;

    private GpsElement() {
        // Default constructor for GpsElement.Default
    }

    private GpsElement(float lng, float lat, short altitude, short speed, short angle, byte satellites) {
        this.lng = lng;
        this.lat = lat;
        this.altitude = altitude;
        this.angle = angle;
        this.satellites = satellites;
        this.speed = speed;
    }

    public static final GpsElement DEFAULT = new GpsElement();

    public static GpsElement create(float lng, float lat, short altitude, short speed, short angle, byte satellites) {
        return new GpsElement(lng, lat, altitude, speed, angle, satellites);
    }

    public static boolean isLatValid(double latitude) {
        return -90 <= latitude && latitude <= 90;
    }
    public static boolean isLngValid(double longitude) {
        return -180 <= longitude && longitude <= 180;
    }
}