package com.tmv.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmv.core.config.CoreConfiguration;
import com.tmv.core.dto.RegisterDeviceDTO;
import com.tmv.core.service.UserDeviceService;
import com.tmv.core.util.JwtTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.tmv")
@ActiveProfiles("test")
@Import(CoreConfiguration.class)
class UserDeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDeviceService userDeviceService;

    @Autowired
    private ObjectMapper objectMapper;

    private static String token;

    @BeforeAll
    static void setup() {
        token = JwtTestUtil.createMockToken("testuser", "ROLE_USER");
    }

    @Test
    void registerDevice_success() throws Exception {
        RegisterDeviceDTO dto = new RegisterDeviceDTO("token123", "ios");

        mockMvc.perform(post("/api/v1/user/devices")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userDeviceService).registerDevice("testuser", "token123", "ios");
    }

    @Test
    void unregisterDevice_success() throws Exception {
        mockMvc.perform(delete("/api/v1/user/devices/token123")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(userDeviceService).unregisterDevice("token123");
    }
}
