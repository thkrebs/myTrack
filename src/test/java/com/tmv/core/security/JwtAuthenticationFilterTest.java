package com.tmv.core.security;

import com.tmv.core.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.Mockito.*;


@SpringBootTest
@ComponentScan(basePackages = "com.tmv")
class JwtAuthenticationFilterTest {

    private final JwtRequestResolver jwtRequestResolver = mock(JwtRequestResolver.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtRequestResolver, jwtService);

    @Test
    void doFilterInternal_ShouldAuthenticate_WhenTokenIsValid() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtRequestResolver.resolveToken(request)).thenReturn("validToken");
        when(jwtService.isValid("validToken")).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);
        when(jwtService.getUserDetails("validToken")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService, times(1)).getUserDetails("validToken");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenTokenIsInvalid() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtRequestResolver.resolveToken(request)).thenReturn("invalidToken");
        when(jwtService.isValid("invalidToken")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService, never()).getUserDetails(Mockito.anyString());
        verify(filterChain, times(1)).doFilter(request, response); // Filter-Kette wird fortgesetzt
    }
}