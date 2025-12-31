package com.tmv.core.controller;

import com.tmv.core.dto.PasswordResetRequestDTO;
import com.tmv.core.dto.PasswordSaveRequestDTO;
import com.tmv.core.model.User;
import com.tmv.core.service.EmailService;
import com.tmv.core.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

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
        Optional<User> user = userService.findUserByEmail(passwordResetRequest.getEmail());
        if (user.isEmpty()) {
            // For security reasons, we shouldn't reveal if the email exists or not.
            // We can return OK or a generic message.
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
        }

        String token = userService.createPasswordResetTokenForUser(user.get());
        String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        // In a real app, this URL would point to your frontend app, e.g., https://myapp.com/reset-password?token=...
        // For now, I'll just send the token.
        String message = "We received a password reset request. To reset your password, use the following link: " + appUrl + "/reset-password?token: " + token;
        emailService.sendSimpleMessage(user.get().getEmail(), "Reset Password", message);

        return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
    }

    @PostMapping("/savePassword")
    public ResponseEntity<String> savePassword(@RequestBody @Valid PasswordSaveRequestDTO passwordSaveRequest) {
        String result = userService.validatePasswordResetToken(passwordSaveRequest.getToken());

        if (result != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token: " + result);
        }

        Optional<User> user = userService.getUserByPasswordResetToken(passwordSaveRequest.getToken());
        if (user.isPresent()) {
            userService.changeUserPassword(user.get(), passwordSaveRequest.getNewPassword());
            return ResponseEntity.ok("Password reset successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token.");
        }
    }
}
