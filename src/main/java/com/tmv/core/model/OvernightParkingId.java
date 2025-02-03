package com.tmv.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class OvernightParkingId implements Serializable {
    //@Column(name = "journey_id")
    private Long journeyId;

    //@Column(name = "parkspot_id")
    private Long parkSpotId;
}
