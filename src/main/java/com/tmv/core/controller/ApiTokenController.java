package com.tmv.core.controller;

import com.tmv.core.dto.ApiTokenDTO;
import com.tmv.core.model.User;
import com.tmv.core.service.ApiTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tokens")
public class ApiTokenController {

    private final ApiTokenService apiTokenService;

    public ApiTokenController(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    // 1. Create a new API token
    @PostMapping
    public ResponseEntity<ApiTokenDTO> createApiToken(@RequestBody ApiTokenDTO request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        ApiTokenDTO apiTokenDTO = apiTokenService.createTokenForUser(currentUser, request.getDescription());
        return ResponseEntity.ok(apiTokenDTO);
    }

    // 2. Revoke an API token
    @DeleteMapping("/{token}")
    public ResponseEntity<Void> revokeToken(@PathVariable String token, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        apiTokenService.revokeTokenForUser(token, currentUser);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // 3. Get all tokens for the authenticated user
    @GetMapping
    public ResponseEntity<List<ApiTokenDTO>> getTokensForUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<ApiTokenDTO> tokens = apiTokenService.findTokensForUser(currentUser);
        return ResponseEntity.ok(tokens);
    }
}