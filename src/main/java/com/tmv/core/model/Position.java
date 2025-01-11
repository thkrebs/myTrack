package com.tmv.core.model;

import com.tmv.core.config.CoreConfiguration;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

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
        final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), CoreConfiguration.SRID);
        this.altitude = altitude;
        this.angle = angle;
        this.satellites = satellites;
        this.speed = speed;
        this.imei = imei;
        this.dateTime = dateTime;
        this.point = geometryFactory.createPoint( new Coordinate(lng, lat) );
    }
    private short altitude;
    private short angle;
    private byte  satellites;
    private short speed;
    private String imei;
    private Date dateTime;

    @Column(name = "point", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point point;

}