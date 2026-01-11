package com.tmv.core.controller;

import com.tmv.core.config.CoreConfiguration;
import com.tmv.core.dto.MapStructMapper;
import com.tmv.core.dto.ParkSpotDTO;
import com.tmv.core.model.ParkSpot;
import com.tmv.core.service.ParkSpotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.tmv")
@ActiveProfiles("test")
@Import(CoreConfiguration.class)
class ParkSpotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkSpotService parkSpotService;

    @MockBean
    private  @Qualifier("mapStructMapper") MapStructMapper mapper;

    @Test
    void getNearbyParkSpots_success() throws Exception {
        ParkSpot spot = new ParkSpot();
        spot.setId(1L);
        spot.setName("Test Spot");

        ParkSpotDTO dto = new ParkSpotDTO();
        dto.setId(1L);
        dto.setName("Test Spot");

        given(parkSpotService.findParkSpotsWithinDistance(anyDouble(), anyDouble(), anyDouble())).willReturn(List.of(spot));
        given(mapper.toParkSpotDTO(spot)).willReturn(dto);

        mockMvc.perform(get("/api/v1/parkspots/nearby")
                        .param("longitude", "10.0")
                        .param("latitude", "50.0")
                        .param("distance", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Spot"));
    }
}
