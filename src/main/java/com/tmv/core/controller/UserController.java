package com.tmv.core.controller;

import com.tmv.core.dto.UserFeaturesDTO;
import com.tmv.core.dto.UserProfileAdminUpdateDTO;
import com.tmv.core.dto.UserProfileDTO;
import com.tmv.core.dto.UserProfileUpdateDTO;
import com.tmv.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController extends BaseController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}/features")
    @PreAuthorize("hasRole('GOD') or #username == authentication.name")
    public ResponseEntity<UserFeaturesDTO> getUserFeatures(@PathVariable String username) {
        return userService.getUserFeatures(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}/profile")
    @PreAuthorize("hasRole('GOD') or #username == authentication.name")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String username) {
        return userService.getUserProfile(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{username}/profile")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<UserProfileDTO> updateUserProfile(@PathVariable String username, @RequestBody @Valid UserProfileUpdateDTO profileUpdate) {
        UserProfileDTO updatedProfile = userService.updateUserProfile(username, profileUpdate);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/{username}/admin/profile")
    @PreAuthorize("hasRole('GOD')")
    public ResponseEntity<UserProfileDTO> adminUpdateUserProfile(@PathVariable String username, @RequestBody @Valid UserProfileAdminUpdateDTO profileUpdate) {
        UserProfileDTO updatedProfile = userService.adminUpdateUserProfile(username, profileUpdate);
        return ResponseEntity.ok(updatedProfile);
    }
}
