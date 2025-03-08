package com.tmv.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "parkspot")
public class ParkSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String description;
    private int wpPostId;

    @Column(name = "point", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point point;

    @OneToMany(mappedBy = "parkSpot", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<OvernightParking> overnightParkings = new HashSet<>();
}
