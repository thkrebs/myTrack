package com.tmv.inbound.teltonika.model;

import lombok.Getter;

public class GpsElement {
    public static final short INVALID_GPS_SPEED = 255;

    @Getter private float x;
    @Getter private float y;
    @Getter private short altitude;
    @Getter private short angle;
    @Getter private byte satellites;
    @Getter private short speed;

    private GpsElement() {
        // Default constructor for GpsElement.Default
    }

    private GpsElement(float x, float y, short altitude, short speed, short angle, byte satellites) {
        this.x = x;
        this.y = y;
        this.altitude = altitude;
        this.angle = angle;
        this.satellites = satellites;
        this.speed = speed;
    }

    public static final GpsElement DEFAULT = new GpsElement();

    public static GpsElement create(float x, float y, short altitude, short speed, short angle, byte satellites) {
        return new GpsElement(x, y, altitude, speed, angle, satellites);
    }

    public static boolean isLatValid(double latitude) {
        return -90 <= latitude && latitude <= 90;
    }
    public static boolean isLngValid(double longitude) {
        return -180 <= longitude && longitude <= 180;
    }
}