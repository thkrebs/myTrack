package com.tmv.core.util;

import com.tmv.core.service.ImeiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ImeiUrlGuardianTest {

    private ImeiUrlGuardian imeiUrlGuardian;
    private ImeiService imeiService;

    @BeforeEach
    void setUp() {
        imeiService = mock(ImeiService.class);
        imeiUrlGuardian = new ImeiUrlGuardian(imeiService);
    }

    @Test
    void testPreHandle_ValidRequest_ShouldPass() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object handler = new Object();

        // Simulate a valid request (you can customize this based on your logic)
        request.setRequestURI("/api/v1/imei");

        // Act
        boolean result = imeiUrlGuardian.preHandle(request, response, handler);

        // Assert
        assertTrue(result, "preHandle should return true for valid requests");
    }

    @Test
    void testCheckUrlVariables_ValidVariables_ShouldPass() {
        // Arrange
        Map<String, String> pathVariables = new HashMap<>();
        final String imei = "123456789012345";
        pathVariables.put("imei",imei);  // Example valid IMEI

        when(imeiService.isActive(imei)).thenReturn(true);


        // Act & Assert (if the method throws exceptions for invalid input)
        imeiUrlGuardian.checkUrlVariables(pathVariables);
    }

    @Test
    void testShouldCheckImei_ShouldReturnTrueForValidConditions() {
        // Arrange
        Map<String, String> parameters = new HashMap<>();
        parameters.put("imei", "123456789012345");

        // Act
        boolean result = imeiUrlGuardian.shouldCheckImei(parameters);

        // Assert
        assertTrue(result, "shouldCheckImei should return true when IMEI exists");
    }

    @Test
    void testCheckAccess_ValidImei_ShouldPass() {
        // Arrange
        String imei = "123456789012345";

        // Mocking the imeiService behavior (you can adjust the behavior as necessary)
        when(imeiService.isActive(imei)).thenReturn(true);

        // Act
        imeiUrlGuardian.checkAccess(imei);

        // Assert
        verify(imeiService, times(1)).isActive(imei);
    }

    @Test
    void testCheckAccess_InvalidImei_ShouldThrowException() {
        // Arrange
        String imei = "invalid_imei";
        doThrow(new IllegalArgumentException("Invalid IMEI")).when(imeiService).isActive(imei);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> imeiUrlGuardian.checkAccess(imei),
                "Should throw exception for invalid IMEI"
        );
    }
}