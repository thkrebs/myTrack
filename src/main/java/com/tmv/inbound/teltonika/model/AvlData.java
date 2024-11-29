package com.tmv.inbound.teltonika.model;

import java.util.Date;

public class AvlData {
    private final int priority;
    private final Date dateTime;
    private final GpsElement gpsElement;
    private final IoElement ioElement;

    private AvlData(int priority, Date dateTime, GpsElement gpsElement, IoElement ioElement) {
        this.priority = priority;
        this.dateTime = dateTime;
        this.gpsElement = gpsElement;
        this.ioElement = ioElement;
    }

    public int getPriority() {
        return priority;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public GpsElement getGpsElement() {
        return gpsElement;
    }

    public IoElement getIoElement() {
        return ioElement;
    }

    public static AvlData create(int priority, Date dateTime, GpsElement gpsElement, IoElement ioElement) {
        return new AvlData(priority, dateTime, gpsElement, ioElement);
    }
}