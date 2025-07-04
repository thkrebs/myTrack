package com.tmv.core.controller;

import com.tmv.core.dto.ApiTokenDTO;
import com.tmv.core.model.User;
import com.tmv.core.service.ApiTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class ApiTokenController {

    private final ApiTokenService apiTokenService;

    public ApiTokenController(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    // 1. Create a new API token
    @PostMapping(value = "/api/v1/api-tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiTokenDTO> createApiToken(@RequestBody ApiTokenDTO request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        log.debug("Create api token for user {}", currentUser);
        ApiTokenDTO apiTokenDTO = apiTokenService.createTokenForUser(currentUser, request.getDescription());
        return ResponseEntity.ok(apiTokenDTO);
    }

    // 2. Revoke an API token
    @DeleteMapping(value = "/api/v1/api-tokens/{tokenId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> revokeToken(@PathVariable Long tokenId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        apiTokenService.revokeTokenForUser(tokenId, currentUser);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // 3. Get all tokens for the authenticated user
    @GetMapping(value = "/api/v1/api-tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ApiTokenDTO>> getTokensForUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<ApiTokenDTO> tokens = apiTokenService.findTokensForUser(currentUser);
        return ResponseEntity.ok(tokens);
    }
}