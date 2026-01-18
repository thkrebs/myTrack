package com.tmv.core.controller;

import com.tmv.core.dto.RegisterDeviceDTO;
import com.tmv.core.service.UserDeviceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/devices")
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    @Autowired
    public UserDeviceController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @PostMapping
    public ResponseEntity<Void> registerDevice(@RequestBody @Valid RegisterDeviceDTO registerDeviceDTO, Authentication authentication) {
        userDeviceService.registerDevice(authentication.getName(), registerDeviceDTO.getToken(), registerDeviceDTO.getPlatform());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> unregisterDevice(@PathVariable String token) {
        userDeviceService.unregisterDevice(token);
        return ResponseEntity.noContent().build();
    }
}
