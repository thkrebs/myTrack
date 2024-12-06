package com.tmv.core.model.position;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Entity
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Position() {}

    public Position(float lng, float lat, short altitude, short angle, byte satellites, short speed, String imei, Date dateTime) {
        this.lng = lng;
        this.lat = lat;
        this.altitude = altitude;
        this.angle = angle;
        this.satellites = satellites;
        this.speed = speed;
        this.imei = imei;
        this.dateTime = dateTime;
    }
    private float lng;
    private float lat;
    private short altitude;
    private short angle;
    private byte  satellites;
    private short speed;
    private String imei;
    private Date dateTime;

}