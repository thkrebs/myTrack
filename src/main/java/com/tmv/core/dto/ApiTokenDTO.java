package com.tmv.core.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ApiTokenDTO {
    private Long id;
    private String token;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long userId; // Optional, can hold user information if needed
}