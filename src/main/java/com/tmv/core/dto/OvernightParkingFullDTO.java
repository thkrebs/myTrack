package com.tmv.core.dto;


import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OvernightParkingFullDTO {
    private Long parkspotId;
    private String name;
    private String description;
    private Integer wpPostId;
    private float lat;
    private float lng;
    private LocalDate overnightDate;
}
