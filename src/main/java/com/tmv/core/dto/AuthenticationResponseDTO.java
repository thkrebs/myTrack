package com.tmv.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthenticationResponseDTO {
    private String jwt;
    private String refreshToken; // Refresh Token
}
