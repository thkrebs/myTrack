package com.tmv.core.dto;
import com.tmv.core.config.CoreConfiguration;
import lombok.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PositionDTO {
    private Long id;
    private short altitude;
    private short angle;
    private byte  satellites;
    private short speed;
    private String imei;
    private LocalDateTime dateTime;
    private Float lat;
    private Float lng;
}
