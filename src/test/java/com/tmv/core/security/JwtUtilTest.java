package com.tmv.core.security;

import com.tmv.core.model.User;
import com.tmv.core.util.TestUserFactory;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


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
        String token = jwtUtil.generateToken(createUser("user1", 1L, "t@gmx.de", "xyz", "ROLE_USER"));

        String username = jwtUtil.extractUsername(token);
        assertEquals("user1", username);
    }

    @Test
    void extractExpiration_ShouldReturnCorrectExpirationDate() {
        String token = jwtUtil.generateToken(createUser("user1", 1L, "t@gmx.de", "xyz", "ROLE_USER"));

        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        User user = createUser("user1", 1L, "t@gmx.de", "xyz", "ROLE_USER");

        String token = jwtUtil.generateToken(user);

        assertNotNull(token);
        assertTrue(token.startsWith("ey")); // JWT-Token beginnt meist mit "ey"
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        User user = createUser("user1", 1L, "t@gmx.de", "xyz", "ROLE_USER");
        String token = jwtUtil.generateToken(user);

        boolean isValid = jwtUtil.validateToken(token, user);
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        User user = createUser("user1", 1L, "t@gmx.de", "xyz", "ROLE_USER");
        String token = jwtUtil.generateToken(user);

        User anotherUser = createUser("anotheruser", 2L, "y@gmx.de", "xxxx", "ROLE_USER");
        boolean isValid = jwtUtil.validateToken(token, anotherUser);
        assertFalse(isValid);
    }

    private User createUser(String username, Long id, String email, String password, String... authorities ) {
        return TestUserFactory.createTestUserWithAuthorities(username, email, id, authorities);
    }
}