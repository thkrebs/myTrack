package com.tmv.core.service;


import com.tmv.core.dto.ApiTokenDTO;
import com.tmv.core.dto.MapStructMapper;
import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.exception.UnauthorizedOperationException;
import com.tmv.core.model.ApiToken;
import com.tmv.core.model.User;
import com.tmv.core.persistence.ApiTokenRepository;
import com.tmv.core.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ApiTokenService {

    private final ApiTokenRepository apiTokenRepository; // Your token repository
    private final MapStructMapper mapper;


    // Dependency Injection
    public ApiTokenService(ApiTokenRepository apiTokenRepository, @Qualifier("mapStructMapper") MapStructMapper mapper) {
        this.apiTokenRepository = apiTokenRepository;
        this.mapper = mapper;

    }

    public ApiTokenDTO createTokenForUser(User user, String description) {
        ApiToken apiToken = new ApiToken();
        apiToken.setUser(user);
        apiToken.setToken(UUID.randomUUID().toString()); // Generate a secure random token
        apiToken.setDescription(description);
        apiToken.setCreatedAt(LocalDateTime.now());
        apiToken.setExpiresAt(LocalDateTime.now().plusDays(30)); // Example: 30-day expiration

        ApiToken savedToken = apiTokenRepository.save(apiToken);

        return mapper.toApiTokenDTO(savedToken);

    }

    public boolean validateToken(String token) {
        ApiToken apiToken = apiTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        // Optional: Verify expiry
        if (apiToken.getExpiresAt() != null && apiToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }


    // Revoke a token
    public void revokeTokenForUser(String token, User user) {
        ApiToken apiToken = apiTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));

        if (!apiToken.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("You are not authorized to revoke this token.");
        }

        apiTokenRepository.delete(apiToken);
    }


    public User findUserByToken(String token) {
        return apiTokenRepository.findByToken(token)
                .map(ApiToken::getUser) // Retrieve the User associated with the token
                .orElseThrow(() -> new ResourceNotFoundException("No user found for token: " + token));
    }

    // Fetch all tokens for the user
    public List<ApiTokenDTO> findTokensForUser(User user) {
        List<ApiToken> tokens = apiTokenRepository.findByUser(user);
        return tokens.stream()
                .map(mapper::toApiTokenDTO)
                .collect(Collectors.toList());
    }


}