package com.tmv.core.service;

import com.tmv.core.dto.UserFeaturesDTO;
import com.tmv.core.dto.UserProfileAdminUpdateDTO;
import com.tmv.core.dto.UserProfileDTO;
import com.tmv.core.dto.UserProfileUpdateDTO;
import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.PasswordResetToken;
import com.tmv.core.model.User;
import com.tmv.core.persistence.PasswordResetTokenRepository;
import com.tmv.core.persistence.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserFeatureService userFeatureService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, UserFeatureService userFeatureService,
                       PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userFeatureService = userFeatureService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<UserFeaturesDTO> getUserFeatures(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(u -> userFeatureService.decodeFeatures(u.getFeatures()));
    }

    public Optional<User> findUserByEmail(String email) {
        log.debug("Searching for user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Transactional
    public String createPasswordResetTokenForUser(User user) {
        log.debug("Creating password reset token for user: {}", user.getUsername());
        passwordResetTokenRepository.deleteByUser(user);
        log.debug("Deleted any existing tokens for user.");

        String token = generateOTP();
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(myToken);
        log.debug("Saved new token: {}", token);
        return token;
    }

    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(1000000);
        return String.format("%06d", num);
    }

    public String validatePasswordResetToken(String token) {
        log.debug("Validating token: {}", token);
        final Optional<PasswordResetToken> passToken = passwordResetTokenRepository.findByToken(token);

        return !isTokenFound(passToken) ? "invalidToken"
                : isTokenExpired(passToken) ? "expired"
                : null;
    }

    private boolean isTokenFound(Optional<PasswordResetToken> passToken) {
        return passToken.isPresent();
    }

    private boolean isTokenExpired(Optional<PasswordResetToken> passToken) {
        final LocalDateTime expiryDate = passToken.get().getExpiryDate();
        return expiryDate.isBefore(LocalDateTime.now());
    }

    public Optional<User> getUserByPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token).map(PasswordResetToken::getUser);
    }

    public void changeUserPassword(User user, String password) {
        log.debug("Changing password for user: {}", user.getUsername());
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        log.debug("Password changed successfully.");
    }

    public Optional<UserProfileDTO> getUserProfile(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserProfileDTO(
                        user.getUsername(),
                        user.getEmail(),
                        user.getCreatedAt(),
                        user.getFeatures(),
                        user.isEnabled()
                ));
    }

    @Transactional
    public UserProfileDTO updateUserProfile(String username, UserProfileUpdateDTO profileUpdate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        user.setUsername(profileUpdate.getUsername());
        user.setEmail(profileUpdate.getEmail());
        User updatedUser = userRepository.save(user);

        return new UserProfileDTO(
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getCreatedAt(),
                updatedUser.getFeatures(),
                updatedUser.isEnabled()
        );
    }

    @Transactional
    public UserProfileDTO adminUpdateUserProfile(String username, UserProfileAdminUpdateDTO profileUpdate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        user.setUsername(profileUpdate.getUsername());
        user.setEmail(profileUpdate.getEmail());
        user.setFeatures(profileUpdate.getFeatures());
        user.setEnabled(profileUpdate.isEnabled());
        User updatedUser = userRepository.save(user);

        return new UserProfileDTO(
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getCreatedAt(),
                updatedUser.getFeatures(),
                updatedUser.isEnabled()
        );
    }
}
