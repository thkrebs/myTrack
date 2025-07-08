package com.tmv.core.controller;

import com.tmv.core.dto.AuthenticationRequestDTO;
import com.tmv.core.dto.AuthenticationResponseDTO;
import com.tmv.core.model.User;
import com.tmv.core.security.JwtUtil;
import com.tmv.core.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class AuthenticationController extends BaseController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

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

        return ResponseEntity.ok(new AuthenticationResponseDTO(jwt));
    }


    @GetMapping("/api/v1/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            // Entfernen der möglichen "Bearer " Prefix aus dem Token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Überprüfung der Gültigkeit des Tokens
            boolean notExpired = jwtUtil.isTokenExpired(token);

            if (notExpired) {
                return ResponseEntity.ok("Token is valid.");
            } else {
                return ResponseEntity.status(401).body("Token expired.");
            }
        } catch (Exception e) {
            log.error("Token validation failed: ", e);
            return ResponseEntity.status(400).body("Bad Request: Invalid token format.");
        }
    }
}
