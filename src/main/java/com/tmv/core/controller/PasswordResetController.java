package com.tmv.core.controller;

import com.tmv.core.dto.PasswordResetRequestDTO;
import com.tmv.core.dto.PasswordSaveRequestDTO;
import com.tmv.core.model.User;
import com.tmv.core.service.EmailService;
import com.tmv.core.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
public class PasswordResetController {

    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public PasswordResetController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(HttpServletRequest request, @RequestBody @Valid PasswordResetRequestDTO passwordResetRequest) {
        log.info("Received password reset request for email: {}", passwordResetRequest.getEmail());
        
        Optional<User> user = userService.findUserByEmail(passwordResetRequest.getEmail());
        if (user.isEmpty()) {
            log.warn("No user found with email: {}", passwordResetRequest.getEmail());
            // For security reasons, we shouldn't reveal if the email exists or not.
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
        }

        log.info("User found: {}. Creating reset token...", user.get().getUsername());
        String token = userService.createPasswordResetTokenForUser(user.get());
        log.info("Token created: {}", token);
        
        String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String message = "We received a password reset request. To reset your password, use the following link: " + appUrl + "/reset-password?token=" + token;
        
        log.info("Sending email to: {}", user.get().getEmail());
        try {
            emailService.sendSimpleMessage(user.get().getEmail(), "Reset Password", message);
            log.info("Email sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }

        return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
    }

    @PostMapping("/savePassword")
    public ResponseEntity<String> savePassword(@RequestBody @Valid PasswordSaveRequestDTO passwordSaveRequest) {
        log.info("Received password save request with token: {}", passwordSaveRequest.getToken());
        String result = userService.validatePasswordResetToken(passwordSaveRequest.getToken());

        if (result != null) {
            log.warn("Token validation failed: {}", result);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token: " + result);
        }

        Optional<User> user = userService.getUserByPasswordResetToken(passwordSaveRequest.getToken());
        if (user.isPresent()) {
            log.info("Changing password for user: {}", user.get().getUsername());
            userService.changeUserPassword(user.get(), passwordSaveRequest.getNewPassword());
            return ResponseEntity.ok("Password reset successfully.");
        } else {
            log.error("Token valid but user not found. This should not happen.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token.");
        }
    }
}
