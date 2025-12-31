package com.tmv.core.service;

import com.tmv.core.model.PasswordResetToken;
import com.tmv.core.model.User;
import com.tmv.core.persistence.PasswordResetTokenRepository;
import com.tmv.core.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFeatureService userFeatureService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("oldPassword");
    }

    @Test
    void testCreatePasswordResetTokenForUser() {
        // Act
        String token = userService.createPasswordResetTokenForUser(testUser);

        // Assert
        assertNotNull(token);
        verify(passwordResetTokenRepository, times(1)).deleteByUser(testUser);
        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    void testValidatePasswordResetToken_Valid() {
        // Arrange
        String tokenStr = "valid-token";
        PasswordResetToken token = new PasswordResetToken(tokenStr, testUser);
        // Expiry is set to 24h in future by default constructor
        when(passwordResetTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

        // Act
        String result = userService.validatePasswordResetToken(tokenStr);

        // Assert
        assertNull(result);
    }

    @Test
    void testValidatePasswordResetToken_Invalid() {
        // Arrange
        String tokenStr = "invalid-token";
        when(passwordResetTokenRepository.findByToken(tokenStr)).thenReturn(Optional.empty());

        // Act
        String result = userService.validatePasswordResetToken(tokenStr);

        // Assert
        assertEquals("invalidToken", result);
    }

    @Test
    void testValidatePasswordResetToken_Expired() {
        // Arrange
        String tokenStr = "expired-token";
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenStr);
        token.setUser(testUser);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1)); // Expired

        when(passwordResetTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

        // Act
        String result = userService.validatePasswordResetToken(tokenStr);

        // Assert
        assertEquals("expired", result);
    }

    @Test
    void testChangeUserPassword() {
        // Arrange
        String newPassword = "newPassword";
        String encodedPassword = "encodedNewPassword";
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        // Act
        userService.changeUserPassword(testUser, newPassword);

        // Assert
        assertEquals(encodedPassword, testUser.getPassword());
        verify(userRepository, times(1)).save(testUser);
    }
}
