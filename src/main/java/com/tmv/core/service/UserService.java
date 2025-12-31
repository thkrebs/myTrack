package com.tmv.core.service;

import com.tmv.core.dto.UserFeaturesDTO;
import com.tmv.core.model.PasswordResetToken;
import com.tmv.core.model.User;
import com.tmv.core.persistence.PasswordResetTokenRepository;
import com.tmv.core.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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
        return userRepository.findByEmail(email);
    }

    @Transactional
    public String createPasswordResetTokenForUser(User user) {
        // Check if a token already exists for this user and delete it
        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(myToken);
        return token;
    }

    public String validatePasswordResetToken(String token) {
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
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}
