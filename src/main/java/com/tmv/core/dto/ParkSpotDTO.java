package com.tmv.core.dto;

import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ParkSpotDTO {
    private Long id;
    private String name;
    private String description;
    private Integer wpPostId;
    private Float lat;
    private Float lng;
}