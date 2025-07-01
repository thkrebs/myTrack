package com.tmv.core.util;
import com.tmv.core.model.Authority;
import com.tmv.core.model.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class TestUserFactory {

    public static User createTestUserWithAuthorities(String username, String email, Long id, String... roles) {
        User testUser = new User();
        testUser.setId(id);
        testUser.setUsername(username);
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
        testUser.setEmail(email);
        testUser.setCreatedAt(LocalDateTime.now());

        // Add roles/authorities
        Set<Authority> authorities = new HashSet<>();
        for (String role : roles) {
            Authority authority = new Authority();
            authority.setName(role); // Assume Authority class has a `setName` method
            authorities.add(authority);
        }
        testUser.setAuthorities(authorities);

        return testUser;
    }
}