package com.tmv.core.security;

import com.tmv.core.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    // Inside JwtAuthenticationFilter
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwtToken = jwtRequestResolver.resolveToken(request);

        if (jwtToken != null && jwtService.isValid(jwtToken)) {
            UserDetails userDetails = jwtService.getUserDetails(jwtToken);
            System.out.println(userDetails.getUsername());

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Authentication authentication = jwtService.getAuthentication(jwtToken);

                // Cast to UsernamePasswordAuthenticationToken to set details
                if (authentication instanceof UsernamePasswordAuthenticationToken) {
                    ((UsernamePasswordAuthenticationToken) authentication)
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                }
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
