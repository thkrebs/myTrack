package com.tmv.core.security;

import com.tmv.core.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtUtil {

    private final String SECRET_KEY = "x3YaK87ghFSsgSDFDFGDSeHp6D4bwGGGQwertDDDLkj2o4fkas5egqLJFa7";

    @Value("${jwt.token_expiry}")
    public long token_expiry;

    @Value("${jwt.refresh_token_expiry}")
    public long refresh_token_expiry;

    @PostConstruct
    public void init() {
        log.info("Aktives Profil: " + System.getProperty("spring.profiles.active"));
        log.info("Token Expiry (Injected) (min): " + token_expiry);
        log.info("Refresh Token Expiry (Injected) (min): " + refresh_token_expiry);

    }

        // Extrahiere den Benutzernamen aus dem JWT
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrahiere das Ablaufdatum aus dem JWT
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrahiere beliebige Claims
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    // Prüfen, ob der Token abgelaufen ist
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Generiere ein Token für den Benutzer
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        // Benutzer-ID und Roles in die Claims einfügen
        claims.put("userId", user.getId()); // Benutzer-ID
        claims.put("roles", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // Authorities extrahieren
                .toList());

        return createToken(claims, user.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * token_expiry)) // Token läuft in 10 Stunden ab
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * refresh_token_expiry))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }


    // Validieren des Tokens
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // Token ist ungültig
        }
    }
}
