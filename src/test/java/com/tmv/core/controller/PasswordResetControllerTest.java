package com.tmv.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmv.core.dto.PasswordResetRequestDTO;
import com.tmv.core.dto.PasswordSaveRequestDTO;
import com.tmv.core.model.User;
import com.tmv.core.service.EmailService;
import com.tmv.core.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetController passwordResetController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(passwordResetController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testResetPassword_ValidEmail() throws Exception {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        PasswordResetRequestDTO requestDTO = new PasswordResetRequestDTO();
        requestDTO.setEmail(email);

        when(userService.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(userService.createPasswordResetTokenForUser(user)).thenReturn("token");

        // Act & Assert
        mockMvc.perform(post("/api/v1/user/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("If an account with that email exists, a password reset link has been sent."));

        verify(emailService, times(1)).sendSimpleMessage(eq(email), anyString(), anyString());
    }

    @Test
    void testResetPassword_UnknownEmail() throws Exception {
        // Arrange
        String email = "unknown@example.com";
        PasswordResetRequestDTO requestDTO = new PasswordResetRequestDTO();
        requestDTO.setEmail(email);

        when(userService.findUserByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/v1/user/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("If an account with that email exists, a password reset link has been sent."));

        verify(emailService, never()).sendSimpleMessage(anyString(), anyString(), anyString());
    }

    @Test
    void testSavePassword_Success() throws Exception {
        // Arrange
        String token = "valid-token";
        String newPassword = "newPassword";
        PasswordSaveRequestDTO requestDTO = new PasswordSaveRequestDTO();
        requestDTO.setToken(token);
        requestDTO.setNewPassword(newPassword);

        User user = new User();
        when(userService.validatePasswordResetToken(token)).thenReturn(null);
        when(userService.getUserByPasswordResetToken(token)).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(post("/api/v1/user/savePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successfully."));

        verify(userService, times(1)).changeUserPassword(user, newPassword);
    }

    @Test
    void testSavePassword_InvalidToken() throws Exception {
        // Arrange
        String token = "invalid-token";
        PasswordSaveRequestDTO requestDTO = new PasswordSaveRequestDTO();
        requestDTO.setToken(token);
        requestDTO.setNewPassword("newPassword");

        when(userService.validatePasswordResetToken(token)).thenReturn("invalidToken");

        // Act & Assert
        mockMvc.perform(post("/api/v1/user/savePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid or expired token: invalidToken"));

        verify(userService, never()).changeUserPassword(any(User.class), anyString());
    }
}
