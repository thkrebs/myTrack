package com.tmv.core.service;

import com.tmv.core.model.User;
import com.tmv.core.model.UserDevice;
import com.tmv.core.persistence.UserDeviceRepository;
import com.tmv.core.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserDeviceService(UserDeviceRepository userDeviceRepository, UserRepository userRepository) {
        this.userDeviceRepository = userDeviceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDevice registerDevice(String username, String token, String platform) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Optional<UserDevice> existingDevice = userDeviceRepository.findByToken(token);

        if (existingDevice.isPresent()) {
            UserDevice device = existingDevice.get();
            // If the token is already registered but to a different user, update the user
            if (!device.getUser().getId().equals(user.getId())) {
                device.setUser(user);
            }
            device.setPlatform(platform);
            device.setLastUsed(LocalDateTime.now());
            return userDeviceRepository.save(device);
        } else {
            UserDevice newDevice = new UserDevice();
            newDevice.setUser(user);
            newDevice.setToken(token);
            newDevice.setPlatform(platform);
            return userDeviceRepository.save(newDevice);
        }
    }

    @Transactional
    public void unregisterDevice(String token) {
        userDeviceRepository.deleteByToken(token);
    }
}
