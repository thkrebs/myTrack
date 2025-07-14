package com.tmv.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ImeiSlimDTO {
    private Long id;
    @NotNull
    private String imei;
    private String description;
    private String deviceType;
    private boolean shownOnMap;

}
