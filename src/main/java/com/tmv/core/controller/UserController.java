package com.tmv.core.controller;

import com.tmv.core.dto.UserFeaturesDTO;
import com.tmv.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class UserController extends BaseController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/v1/user/{username}/features")
    @PreAuthorize("hasRole('GOD') or #username == authentication.name")
    public ResponseEntity<UserFeaturesDTO> getUserFeatures(@PathVariable String username, Authentication authentication) {
        Optional<UserFeaturesDTO> features = userService.getUserFeatures(username);
        return features.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
