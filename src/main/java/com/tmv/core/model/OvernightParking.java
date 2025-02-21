package com.tmv.core.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "overnightparking")
public class OvernightParking {

    @EmbeddedId
    private OvernightParkingId id = new OvernightParkingId();

    @ManyToOne
    @MapsId("journeyId")
    @JsonBackReference
    private Journey journey;

    @ManyToOne
    @MapsId("parkSpotId")
    @JsonBackReference
    private ParkSpot parkSpot;

    @Column(insertable = false, updatable = false)
    private LocalDate overnightDate;
}
