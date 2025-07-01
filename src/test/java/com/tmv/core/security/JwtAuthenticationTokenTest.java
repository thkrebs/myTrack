package com.tmv.core.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ComponentScan(basePackages = "com.tmv")
class JwtAuthenticationTokenTest {

    @Test
    void getCredentials_ShouldReturnToken() {
        JwtAuthenticationToken token = new JwtAuthenticationToken("test_token");
        assertEquals("test_token", token.getCredentials());
    }

    @Test
    void getPrincipal_ShouldReturnPrincipal() {
        JwtAuthenticationToken token = new JwtAuthenticationToken("principal", "test_token", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals("principal", token.getPrincipal());
    }

    @Test
    void isAuthenticated_ShouldBeFalse_WhenNotAuthenticated() {
        JwtAuthenticationToken token = new JwtAuthenticationToken("test_token");
        assertFalse(token.isAuthenticated());
    }

    @Test
    void isAuthenticated_ShouldBeTrue_WhenAuthenticated() {
        JwtAuthenticationToken token = new JwtAuthenticationToken("principal", "test_token", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(token.isAuthenticated());
    }
}