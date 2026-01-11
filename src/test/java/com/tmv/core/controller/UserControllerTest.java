package com.tmv.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmv.core.config.CoreConfiguration;
import com.tmv.core.dto.UserFeaturesDTO;
import com.tmv.core.dto.UserProfileAdminUpdateDTO;
import com.tmv.core.dto.UserProfileDTO;
import com.tmv.core.dto.UserProfileUpdateDTO;
import com.tmv.core.model.Authority;
import com.tmv.core.model.User;
import com.tmv.core.service.CustomUserDetailsService;
import com.tmv.core.service.UserService;
import com.tmv.core.util.JwtTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.tmv")
@ActiveProfiles("test")
@Import(CoreConfiguration.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private static String token;
    private static String adminToken;

    @BeforeAll
    static void setup() {
        token = JwtTestUtil.createMockToken("testuser", "ROLE_USER");
        adminToken = JwtTestUtil.createMockToken("admin", "ROLE_GOD");
    }

    @BeforeEach
    void init() {
        // Mock the UserDetailsService to return valid users for the tokens
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        Authority userAuth = new Authority();
        userAuth.setName("ROLE_USER");
        testUser.setAuthorities(Collections.singleton(userAuth));

        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        Authority adminAuth = new Authority();
        adminAuth.setName("ROLE_GOD");
        adminUser.setAuthorities(Collections.singleton(adminAuth));

        given(customUserDetailsService.loadUserByUsername("testuser")).willReturn(testUser);
        given(customUserDetailsService.loadUserByUsername("admin")).willReturn(adminUser);
    }

    @Test
    void getUserFeatures_success() throws Exception {
        UserFeaturesDTO features = new UserFeaturesDTO(1, 2);
        given(userService.getUserFeatures("testuser")).willReturn(Optional.of(features));

        mockMvc.perform(get("/api/v1/user/testuser/features")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.domain").value(1))
                .andExpect(jsonPath("$.packages").value(2));
    }

    @Test
    void getUserProfile_success() throws Exception {
        UserProfileDTO profile = new UserProfileDTO("testuser", "test@example.com", LocalDateTime.now(), 0L, true);
        given(userService.getUserProfile("testuser")).willReturn(Optional.of(profile));

        mockMvc.perform(get("/api/v1/user/testuser/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void updateUserProfile_success() throws Exception {
        UserProfileUpdateDTO updateDTO = new UserProfileUpdateDTO("newuser", "new@example.com");
        UserProfileDTO updatedProfile = new UserProfileDTO("newuser", "new@example.com", LocalDateTime.now(), 0L, true);

        given(userService.updateUserProfile(eq("testuser"), any(UserProfileUpdateDTO.class))).willReturn(updatedProfile);

        mockMvc.perform(put("/api/v1/user/testuser/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void adminUpdateUserProfile_success() throws Exception {
        UserProfileAdminUpdateDTO updateDTO = new UserProfileAdminUpdateDTO("newuser", "new@example.com", 10L, false);
        UserProfileDTO updatedProfile = new UserProfileDTO("newuser", "new@example.com", LocalDateTime.now(), 10L, false);

        given(userService.adminUpdateUserProfile(eq("testuser"), any(UserProfileAdminUpdateDTO.class))).willReturn(updatedProfile);

        mockMvc.perform(put("/api/v1/user/testuser/admin/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.features").value(10))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void adminUpdateUserProfile_forbiddenForUser() throws Exception {
        UserProfileAdminUpdateDTO updateDTO = new UserProfileAdminUpdateDTO("newuser", "new@example.com", 10L, false);

        mockMvc.perform(put("/api/v1/user/testuser/admin/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }
}
