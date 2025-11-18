package com.tmv.core.service;

import com.tmv.core.dto.UserFeaturesDTO;
import com.tmv.core.model.User;
import com.tmv.core.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserFeaturesDTO> getUserFeatures(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(u -> new UserFeaturesDTO(u.getFeatures()));
    }
}
