package com.tmv.core.security;

import com.tmv.core.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtRequestResolver jwtRequestResolver;
    private final JwtService jwtService; // Ein Service zur JWT-Validierung oder Benutzerextraktion

    public JwtAuthenticationFilter(JwtRequestResolver jwtRequestResolver, JwtService jwtService) {
        this.jwtRequestResolver = jwtRequestResolver;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 1. Token aus der Anfrage extrahieren
        String jwtToken = jwtRequestResolver.resolveToken(request);

        if (jwtToken != null && jwtService.isValid(jwtToken)) {
            // 2. Benutzer oder Claims aus Token extrahieren (via JwtService)
            UserDetails userDetails = jwtService.getUserDetails(jwtToken);

            // 3. Manuelle Authentifizierung, wenn noch keine vorhanden ist
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var authentication = jwtService.getAuthentication(jwtToken); // Hole Authentication-Objekt
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 4. Weiter in der Filterkette
        filterChain.doFilter(request, response);
    }
}