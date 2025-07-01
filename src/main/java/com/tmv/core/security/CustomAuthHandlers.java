package com.tmv.core.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthHandlers {

    // Handle 401 Unauthorized
    @Component
    public static class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(
                HttpServletRequest request, 
                HttpServletResponse response,
                org.springframework.security.core.AuthenticationException authException
        ) throws IOException {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }

    // Handle 403 Forbidden
    @Component
    public static class CustomAccessDeniedHandler implements AccessDeniedHandler {
        @Override
        public void handle(
                HttpServletRequest request, 
                HttpServletResponse response, 
                AccessDeniedException accessDeniedException
        ) throws IOException {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
        }
    }
}