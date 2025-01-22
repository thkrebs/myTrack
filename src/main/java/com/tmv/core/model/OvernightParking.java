package com.tmv.core.model;

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

    @ManyToOne
    @JoinColumn(name = "journey_id")
    Journey journey;

    @ManyToOne
    @JoinColumn(name = "parkspot_id")
    ParkSpot parkSpot;

    LocalDate stayedAt;
}
