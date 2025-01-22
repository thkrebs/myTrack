package com.tmv.core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "parkspot")
public class ParkSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(name = "point", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point point;

    @OneToMany(mappedBy = "parkSpot", orphanRemoval = true )
    Set<OvernightParking> overnightParkings;
}
