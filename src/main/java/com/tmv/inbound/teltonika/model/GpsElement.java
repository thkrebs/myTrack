package com.tmv.inbound.teltonika.model;

public class GpsElement {
    public static final short INVALID_GPS_SPEED = 255;

    private float x;
    private float y;
    private short altitude;
    private short angle;
    private byte satellites;
    private short speed;

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

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public short getAltitude() {
        return altitude;
    }

    public short getAngle() {
        return angle;
    }

    public byte getSatellites() {
        return satellites;
    }

    public short getSpeed() {
        return speed;
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