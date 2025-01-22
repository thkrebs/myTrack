package com.tmv.core.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @Id
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id")
    @JsonBackReference
    Journey journey;

    @ManyToOne
    @JoinColumn(name = "parkspot_id")
    ParkSpot parkSpot;

    LocalDate stayedAt;
}
