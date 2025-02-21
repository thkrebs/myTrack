package com.tmv.core.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OvernightParkingDTO {
    Long parkSpotId;
    Long journeyId;
    LocalDate overnightDate;
}
