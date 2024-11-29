package com.tmv.inbound.teltonika.model;

import lombok.Getter;

import java.util.Date;

public class AvlData {
    @Getter
    private final int priority;

    @Getter
    private final Date dateTime;

    @Getter
    private final GpsElement gpsElement;

    @Getter
    private final IoElement ioElement;

    private AvlData(int priority, Date dateTime, GpsElement gpsElement, IoElement ioElement) {
        this.priority = priority;
        this.dateTime = dateTime;
        this.gpsElement = gpsElement;
        this.ioElement = ioElement;
    }

    public static AvlData create(int priority, Date dateTime, GpsElement gpsElement, IoElement ioElement) {
        return new AvlData(priority, dateTime, gpsElement, ioElement);
    }
}