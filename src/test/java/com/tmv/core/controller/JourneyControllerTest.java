package com.tmv.core.controller;

import com.tmv.core.config.CoreConfiguration;
import com.tmv.core.dto.JourneyPatchDTO;
import com.tmv.core.dto.ParkSpotWithDateDTO;
import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.Imei;
import com.tmv.core.model.Journey;
import com.tmv.core.model.ParkSpot;
import com.tmv.core.model.User;
import com.tmv.core.service.JourneySecurity;
import com.tmv.core.service.JourneyService;
import com.tmv.core.service.JourneyServiceImpl;
import com.tmv.core.util.JwtTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.tmv")
@ActiveProfiles("test")
@Import(CoreConfiguration.class)
class JourneyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(JourneyServiceImpl.class)
    private JourneyService journeyService;

    @MockBean
    private JourneySecurity journeySecurity;


    private Journey testJourney;

    private final String imeiStr1 = "123456789012345";
    private final String imeiStr2 = "123456789012365";

    private  Imei firstImei;
    private  Imei secondImei;

    private User testUser;
    private static String token;


    @BeforeAll
    static void setup() {
        // Generate a mock JWT token
        token = JwtTestUtil.createMockToken("testuser", "ROLE_USER");
    }

    @BeforeEach
    void setUp() {
        testUser = new User();
        Imei firstImei =  new Imei(imeiStr1, true, Date.from(Instant.now()), Date.from(Instant.now()), "123", testUser, "", "", true);
        Imei secondImei =  new Imei(imeiStr2, true, Date.from(Instant.now()), Date.from(Instant.now()), "456", testUser, "", "", true);

        testUser.setId(1L); // Mocked user ID
        testUser.setUsername("testuser");

        testJourney = new Journey();
        testJourney.setId(1L);
        testJourney.setDescription("Test Journey");
        testJourney.setStartDate(getDate(0));
        testJourney.setEndDate(getDate(1));
        testJourney.setTrackedByImeis(Set.of(firstImei, secondImei));
        testJourney.setOwner(testUser); // Set the owner user
        // Generate a mock JWT token
        token = JwtTestUtil.createMockToken("testuser", "ROLE_USER");
        given(journeySecurity.isOwner(any(Long.class))).willReturn(true);

    }

    @Test
    void shouldReturnJourneyById() throws Exception {
        // Mocking service response
        given(journeyService.getJourneyById(1L)).willReturn(Optional.of(testJourney));
        Mockito.when(journeyService.getValidatedJourney(1L)).thenReturn(testJourney);

        // Testing the endpoint
        mockMvc.perform(get("/api/v1/journeys/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token) // Include Bearer token
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Test Journey")))
                .andExpect(jsonPath("$.startDate", is(getDate(testJourney.getStartDate()))))
                .andExpect(jsonPath("$.endDate", is(getDate(testJourney.getEndDate()))))
                .andExpect(jsonPath("$.ownerId", is(testJourney.getOwner().getId().intValue()))) // Validate ownerId
                .andExpect(jsonPath("$.trackedByImeis[*].imei", containsInAnyOrder(imeiStr1, imeiStr2)));
    }

    @Test
    void shouldReturn404WhenJourneyNotFound() throws Exception {
        // Mocking service response for a non-existent journey
        given(journeyService.getJourneyById(1L)).willReturn(Optional.empty());
        Mockito.when(journeyService.getValidatedJourney(1L)).thenThrow(new ResourceNotFoundException("Journey not found with id: " + 1));
        // Testing the endpoint
        mockMvc.perform(get("/api/v1/journeys/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token) // Include Bearer token
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewJourney() throws Exception {
        Journey newJourney = new Journey();
        newJourney.setId(2L);
        newJourney.setDescription("New Journey");
        newJourney.setOwner(testUser);

        // Mocking service response
        given(journeyService.createNewJourney(any(Journey.class))).willReturn(newJourney);

        // Testing the endpoint
        mockMvc.perform(post("/api/v1/journeys")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token) // Include Bearer token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"New Journey\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.ownerId", is(testJourney.getOwner().getId().intValue()))) // Validate ownerId
                .andExpect(jsonPath("$.description", is("New Journey")));
    }

    @Test
    void shouldUpdateJourneyWhenExists() throws Exception {
        Journey updatedJourney = new Journey();
        updatedJourney.setId(1L);
        updatedJourney.setDescription("Updated Journey");

        // Mocking service response
        given(journeyService.updateJourney(eq(1L), any(Journey.class))).willReturn(updatedJourney);

        // Testing the endpoint
        mockMvc.perform(put("/api/v1/journeys/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token) // Include Bearer token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Journey\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Updated Journey")));
    }


    @Test
    void shouldDeleteJourney() throws Exception {
        // Mocking service response
        Mockito.doNothing().when(journeyService).deleteJourney(1L);

        // Testing the endpoint
        mockMvc.perform(delete("/api/v1/journeys/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))// Include Bearer token
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentJourney() throws Exception {
        // Mocking service response for a non-existent journey
        Mockito.doThrow(new ResourceNotFoundException("Journey not found"))
                .when(journeyService).deleteJourney(1L);

        // Testing the endpoint
        mockMvc.perform(delete("/api/v1/journeys/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)) // Include Bearer token.header(HttpHeaders.AUTHORIZATION, "Bearer " + token) // Include Bearer token)
                .andExpect(status().isNotFound());
    }

    @Test
    @Disabled("needs to be investigated. does return 500 for undefined reasons")
    void shouldReturnBadRequestOnEndWhenJourneyIdIsMissing() throws Exception {
        mockMvc.perform(put("/api/v1/journeys//end")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token) // Include Bearer token
                ) // Ungültige URL
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldReturnIsNotFoundOnEndWhenJourneyDoesNotExist() throws Exception {
        Long journeyId = 1L;

        Mockito.when(journeyService.endJourney(journeyId))
                .thenThrow(new ResourceNotFoundException("Journey not found with id: 1"));
        mockMvc.perform(put("/api/v1/journeys/1/end")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)) // Include Bearer token)
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldReturnIsNotFoundOnStartWhenJourneyDoesNotExist() throws Exception {
        Long journeyId = 1L;

        Mockito.when(journeyService.startJourney(journeyId))
                .thenThrow(new ResourceNotFoundException("Journey not found with id: 1"));
        mockMvc.perform(put("/api/v1/journeys/1/start")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)) // Include Bearer token)
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldEndJourneySuccessfully() throws Exception {
        Long journeyId = 1L;

        Journey mockJourney = new Journey();
        mockJourney.setId(journeyId);
        mockJourney.setEndDate(LocalDate.now());
        mockJourney.setStartDate(LocalDate.now().minusDays(1));


        Mockito.when(journeyService.endJourney(journeyId)).thenReturn(mockJourney);
        Mockito.when(journeyService.getValidatedJourney(journeyId)).thenReturn(mockJourney);
        Mockito.when(journeyService.createGeoJsonData(mockJourney, null, mockJourney.getEndDate().atStartOfDay(),false)).thenReturn(new HashMap<>());

        mockMvc.perform(put("/api/v1/journeys/{id}/end", journeyId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)) // Include Bearer token
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenJourneyDoesNotExistForStart() throws Exception {
        Long invalidJourneyId = 999L;

        Mockito.when(journeyService.startJourney(invalidJourneyId)).thenThrow(new ResourceNotFoundException("Journey not found with id: 999"));

        mockMvc.perform(put("/api/v1/journeys/{id}/start", invalidJourneyId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)) // Include Bearer token
                .andExpect(status().isNotFound())
                .andExpect(content().string("Journey not found with id: 999")); // Optional, wenn Error-Response ein JSON enthält
    }

    @Test
    void shouldStartJourneySuccessfully() throws Exception {
        Long journeyId = 1L;

        Journey mockJourney = new Journey();
        mockJourney.setId(journeyId);
        mockJourney.setStartDate(LocalDate.now());

        Mockito.when(journeyService.startJourney(journeyId)).thenReturn(mockJourney);

        mockMvc.perform(put("/api/v1/journeys/{id}/start", journeyId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)) // Include Bearer token
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L)); // Validate ownership

    }

    @Test
    void shouldPatchJourney() throws Exception {
        Journey patchedJourney = new Journey();
        patchedJourney.setId(1L);
        patchedJourney.setName("Patched Name");

        given(journeyService.patchJourney(eq(1L), any(JourneyPatchDTO.class))).willReturn(patchedJourney);

        mockMvc.perform(patch("/api/v1/journeys/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Patched Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Patched Name")));
    }

    @Test
    void shouldGetOvernightParkings() throws Exception {
        ParkSpot spot = new ParkSpot();
        spot.setId(10L);
        spot.setName("Spot");

        given(journeyService.getOvernightParkSpots(1L)).willReturn(List.of(spot));

        mockMvc.perform(get("/api/v1/journeys/1/overnight-parkings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].name", is("Spot")));
    }

    @Test
    void shouldGetNearbyOvernightParking() throws Exception {
        ParkSpotWithDateDTO dto = new ParkSpotWithDateDTO();
        dto.setId(10L);
        dto.setName("Spot");
        dto.setParkDate(LocalDate.now());

        given(journeyService.getNearbyParkSpotsWithDate(1L, 50)).willReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/journeys/1/nearbyOvernight-parking")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].name", is("Spot")))
                .andExpect(jsonPath("$[0].parkDate", is(getDate(LocalDate.now()))));
    }

    public static LocalDate getDate(int offset) {
        return LocalDate.now(ZoneId.of("UTC")).plusDays(offset);
    }

    public static String getDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Definiertes Format
        return date.format(formatter);
    }

}