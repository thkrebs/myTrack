package com.tmv.core.service;

import com.tmv.core.dto.ApiTokenDTO;
import com.tmv.core.dto.MapStructMapper;
import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.exception.UnauthorizedOperationException;
import com.tmv.core.model.ApiToken;
import com.tmv.core.model.User;
import com.tmv.core.persistence.ApiTokenRepository;
import com.tmv.core.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiTokenServiceTest {

    private ApiTokenService apiTokenService;
    private ApiTokenRepository apiTokenRepository;
    private MapStructMapper mapper;

    @BeforeEach
    void setUp() {
        apiTokenRepository = mock(ApiTokenRepository.class);
        mapper = mock(MapStructMapper.class);
        apiTokenService = new ApiTokenService(apiTokenRepository, mapper);
    }

    @Test
    void testCreateTokenForUser() {
        // Arrange
        User user = new User();
        user.setId(1L);
        ApiToken apiToken = new ApiToken();
        apiToken.setUser(user);
        apiToken.setToken(UUID.randomUUID().toString());
        apiToken.setDescription("Test Token");
        apiToken.setCreatedAt(LocalDateTime.now());
        apiToken.setExpiresAt(apiToken.getCreatedAt().plusDays(30));

        ApiTokenDTO apiTokenDTO = new ApiTokenDTO();
        apiTokenDTO.setId(1L);

        when(apiTokenRepository.save(any(ApiToken.class))).thenReturn(apiToken);
        when(mapper.toApiTokenDTO(apiToken)).thenReturn(apiTokenDTO);

        // Act
        ApiTokenDTO result = apiTokenService.createTokenForUser(user, "Test Token");

        // Assert
        assertNotNull(result);
        verify(apiTokenRepository, times(1)).save(any(ApiToken.class));
        verify(mapper, times(1)).toApiTokenDTO(apiToken);
    }

    @Test
    void testValidateToken_Success() {
        // Arrange
        ApiToken apiToken = new ApiToken();
        apiToken.setToken("valid-token");
        apiToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(apiTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(apiToken));

        // Act
        boolean result = apiTokenService.validateToken("valid-token");

        // Assert
        assertTrue(result);
        verify(apiTokenRepository, times(1)).findByToken("valid-token");
    }

    @Test
    void testValidateToken_Failure_ExpiredToken() {
        // Arrange
        ApiToken apiToken = new ApiToken();
        apiToken.setToken("expired-token");
        apiToken.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(apiTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(apiToken));

        // Act
        boolean result = apiTokenService.validateToken("expired-token");

        // Assert
        assertFalse(result);
        verify(apiTokenRepository, times(1)).findByToken("expired-token");
    }

    @Test
    void testValidateToken_Failure_InvalidToken() {
        // Arrange
        when(apiTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> apiTokenService.validateToken("invalid-token"));
        verify(apiTokenRepository, times(1)).findByToken("invalid-token");
    }

    @Test
    void testRevokeTokenForUser_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);

        ApiToken apiToken = new ApiToken();
        apiToken.setId(1L);
        apiToken.setToken("token-to-revoke");
        apiToken.setUser(user);

        when(apiTokenRepository.findById(1L)).thenReturn(Optional.of(apiToken));

        // Act
        apiTokenService.revokeTokenForUser(1L, user);

        // Assert
        verify(apiTokenRepository, times(1)).delete(apiToken);
    }

    @Test
    void testRevokeTokenForUser_Failure_Unauthorized() {
        // Arrange
        User user = new User();
        user.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);

        ApiToken apiToken = new ApiToken();
        apiToken.setId(1L);
        apiToken.setToken("token-to-revoke");
        apiToken.setUser(otherUser);

        when(apiTokenRepository.findByToken("token-to-revoke")).thenReturn(Optional.of(apiToken));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> apiTokenService.revokeTokenForUser(1L, user));
        verify(apiTokenRepository, never()).delete(apiToken);
    }

    @Test
    void testFindUserByToken_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);

        ApiToken apiToken = new ApiToken();
        apiToken.setToken("valid-token");
        apiToken.setUser(user);

        when(apiTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(apiToken));

        // Act
        User result = apiTokenService.findUserByToken("valid-token");

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        verify(apiTokenRepository, times(1)).findByToken("valid-token");
    }

    @Test
    void testFindUserByToken_Failure() {
        // Arrange
        when(apiTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> apiTokenService.findUserByToken("invalid-token"));
        verify(apiTokenRepository, times(1)).findByToken("invalid-token");
    }

    @Test
    void testFindTokensForUser() {
        // Arrange
        User user = new User();
        user.setId(1L);

        ApiToken token1 = new ApiToken();
        token1.setId(1L);
        token1.setDescription("Token 1");

        ApiToken token2 = new ApiToken();
        token2.setId(2L);
        token2.setDescription("Token 2");

        List<ApiToken> tokenList = List.of(token1, token2);
        ApiTokenDTO dto1 = new ApiTokenDTO();
        dto1.setId(1L);
        ApiTokenDTO dto2 = new ApiTokenDTO();
        dto2.setId(2L);

        when(apiTokenRepository.findByUser(user)).thenReturn(tokenList);
        when(mapper.toApiTokenDTO(token1)).thenReturn(dto1);
        when(mapper.toApiTokenDTO(token2)).thenReturn(dto2);

        // Act
        List<ApiTokenDTO> result = apiTokenService.findTokensForUser(user);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(apiTokenRepository, times(1)).findByUser(user);
        verify(mapper, times(1)).toApiTokenDTO(token1);
        verify(mapper, times(1)).toApiTokenDTO(token2);
    }
}