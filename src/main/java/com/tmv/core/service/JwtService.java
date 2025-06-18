package com.tmv.core.service;

import com.tmv.core.security.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtUtil jwtUtil; // Dependency Injection von JwtUtil
    private final CustomUserDetailsService customUserDetailsService;


    public JwtService(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService)
    {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    // Prüft, ob ein gegebener JWT gültig ist
    public boolean isValid(String jwtToken) {
        try {
            // Extrahiere den Benutzernamen aus dem Token
            String username = jwtUtil.extractUsername(jwtToken);

            if (username != null && !jwtUtil.isTokenExpired(jwtToken)) {
                // Benutzerabruf aus dem CustomUserDetailsService zur Validierung
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                return jwtUtil.validateToken(jwtToken, userDetails);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Extrahiere Benutzerinformationen (UserDetails) aus dem JWT
    public UserDetails getUserDetails(String jwtToken) {
        String username = jwtUtil.extractUsername(jwtToken);

        if (username != null) {
            return customUserDetailsService.loadUserByUsername(username);
        }
        return null;
    }

    // Generiert ein Authentication-Objekt basierend auf dem JWT
    public Authentication getAuthentication(String jwtToken) {
        UserDetails userDetails = getUserDetails(jwtToken);

        if (userDetails != null) {
            // Erstelle ein UsernamePasswordAuthenticationToken
            return new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null, // Keine Anmeldeinformationen erforderlich
                    userDetails.getAuthorities()
            );
        }
        return null;
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtil.generateToken(userDetails);
    }

    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !jwtUtil.isTokenExpired(token);
    }
}