package com.tmv.core.security;

import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@ComponentScan(basePackages = "com.tmv")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @PostConstruct
    public void logConfig() {
        System.out.println("Profil geladen: " + System.getProperty("spring.profiles.active"));
    }


    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(createUser("user1"));

        String username = jwtUtil.extractUsername(token);
        assertEquals("user1", username);
    }

    @Test
    void extractExpiration_ShouldReturnCorrectExpirationDate() {
        String token = jwtUtil.generateToken(createUser("user1"));

        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        UserDetails userDetails = createUser("testUser");

        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(token.startsWith("ey")); // JWT-Token beginnt meist mit "ey"
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        UserDetails userDetails = createUser("user1");
        String token = jwtUtil.generateToken(userDetails);

        boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        UserDetails userDetails = createUser("user1");
        String token = jwtUtil.generateToken(userDetails);

        UserDetails anotherUser = createUser("user2");
        boolean isValid = jwtUtil.validateToken(token, anotherUser);
        assertFalse(isValid);
    }

    private UserDetails createUser(String username) {
        return User.builder()
                .username(username)
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }
}