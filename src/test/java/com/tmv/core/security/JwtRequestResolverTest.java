package com.tmv.core.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ComponentScan(basePackages = "com.tmv")
class JwtRequestResolverTest {

    private final JwtRequestResolver jwtRequestResolver = new JwtRequestResolver();

    @Test
    void resolveToken_ShouldReturnToken_WhenHeaderContainsBearerToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer test_jwt_token");

        String token = jwtRequestResolver.resolveToken(request);
        assertEquals("test_jwt_token", token);
    }

    @Test
    void resolveToken_ShouldReturnNull_WhenHeaderIsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        String token = jwtRequestResolver.resolveToken(request);
        assertNull(token);
    }

    @Test
    void resolveToken_ShouldReturnNull_WhenHeaderDoesNotStartWithBearer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Token test_jwt_token");

        String token = jwtRequestResolver.resolveToken(request);
        assertNull(token);
    }
}