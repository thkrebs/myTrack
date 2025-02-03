package com.tmv.core.dto;

import lombok.*;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ParkSpotDTO {
    private Long id;
    private String name;
    private String description;
    private Float lat;
    private Float lng;
}