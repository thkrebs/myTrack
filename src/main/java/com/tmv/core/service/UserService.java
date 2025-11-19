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
    private final UserFeatureService userFeatureService;

    @Autowired
    public UserService(UserRepository userRepository, UserFeatureService userFeatureService) {
        this.userRepository = userRepository;
        this.userFeatureService = userFeatureService;
    }

    public Optional<UserFeaturesDTO> getUserFeatures(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(u -> userFeatureService.decodeFeatures(u.getFeatures()));
    }
}
