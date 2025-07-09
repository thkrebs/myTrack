package com.tmv.core.controller;

import com.tmv.core.dto.AuthenticationRequestDTO;
import com.tmv.core.dto.AuthenticationResponseDTO;
import com.tmv.core.model.User;
import com.tmv.core.security.JwtUtil;
import com.tmv.core.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AuthenticationController extends BaseController {


    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public AuthenticationController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
            this.authenticationManager = authenticationManager;
            this.jwtUtil = jwtUtil;
            this.userDetailsService = userDetailsService;
    }

    @PostMapping("/api/v1/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequestDTO request)  throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Falscher Benutzername oder Passwort", e);
        }

        final User user = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwt = jwtUtil.generateToken(user);
        final String refreshToken = jwtUtil.generateRefreshToken(user);


        return ResponseEntity.ok(new AuthenticationResponseDTO(jwt, refreshToken));
    }

    @PostMapping("/api/v1/refresh-token")
    public ResponseEntity<?> refreshAuthenticationToken(@RequestBody String refreshToken) {
        try {
            // Refresh Token validieren
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(401).body("Invalid refresh token");
            }

            // Benutzer aus dem Token extrahieren
            String username = jwtUtil.extractUsername(refreshToken);
            final User user = userDetailsService.loadUserByUsername(username);

            // Neues Access Token generieren
            final String newAccessToken = jwtUtil.generateToken(user);

            // Rückgabe des neuen Tokens
            return ResponseEntity.ok(new AuthenticationResponseDTO(newAccessToken, refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Could not refresh token");
        }
    }
}
