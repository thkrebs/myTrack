package com.tmv.core.service;

import com.tmv.core.config.CoreConfiguration;
import com.tmv.core.exception.ConstraintViolationException;
import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.*;
import com.tmv.core.persistence.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.tmv")
@PropertySource("classpath:application.properties")
@Import(CoreConfiguration.class)
@ExtendWith(MockitoExtension.class)
class JourneyServiceImplTest {

    @Mock
    private JourneyRepository journeyRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private ParkSpotRepository parkSpotRepository;

    @Mock
    OvernightParkingRepository overnightParkingRepository;

    @Mock
    ImeiRepository imeiRepository;

    @Mock
    WordPressPostServiceImpl wordPressPostService;

    @Mock
    private EntityManager entityManagerMock;

    @InjectMocks
    private JourneyServiceImpl journeyService;

    @InjectMocks
    private PositionServiceImpl positionService;

    @Captor
    private ArgumentCaptor<List<String>> imeiCaptor;

    @Captor
    private ArgumentCaptor<Journey> journeyCaptor;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L); // Mocked user ID
        testUser.setUsername("testuser");

        journeyService = new JourneyServiceImpl(positionRepository, journeyRepository, parkSpotRepository, overnightParkingRepository, imeiRepository, wordPressPostService, positionService);
        // tried several different approaches to get the mocked entityManager into the journeyService which didn't work
        ReflectionTestUtils.setField(journeyService, "entityManager", entityManagerMock);
    }


    @Test
    void updateJourney_existingJourney_updatedSuccessfully() {
        final LocalDate dtStart = LocalDate.now();
        final LocalDate dtEnd = LocalDate.now().plusDays(1);

        // Arrange
        Long journeyId = 1L;
        Journey existingJourney = new Journey();
        existingJourney.setId(journeyId);
        existingJourney.setDescription("Old description");

        Journey newJourneyData = new Journey();
        newJourneyData.setDescription("New description");
        newJourneyData.setStartDate(dtStart);
        newJourneyData.setEndDate(dtEnd);
        newJourneyData.setTrackedByImeis(Set.of(new Imei("12345", true, null, null, null, testUser)));

        Journey updatedJourney = new Journey();
        updatedJourney.setId(journeyId);
        updatedJourney.setDescription("New description");

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(existingJourney));
        Mockito.when(journeyRepository.save(existingJourney)).thenReturn(updatedJourney);

        // Act
        Journey result = journeyService.updateJourney(journeyId, newJourneyData);

        // Assert
        assertNotNull(result);
        assertEquals("New description", result.getDescription());
        verify(journeyRepository, times(1)).findById(journeyId);
        verify(journeyRepository, times(1)).save(existingJourney);

        verify(journeyRepository, times(1)).save(journeyCaptor.capture());
        Journey jCapt = journeyCaptor.getValue();
        assertEquals(jCapt.getDescription(),newJourneyData.getDescription());
        assertEquals(jCapt.getStartDate(),newJourneyData.getStartDate());
        assertEquals(jCapt.getEndDate(),newJourneyData.getEndDate());
        assertEquals(jCapt.getOwner(), newJourneyData.getOwner());
        List<String> capturedImeis = jCapt.getTrackedByImeis().stream().map(Imei::getImei).toList();
        assertEquals(1, capturedImeis.size());
        assertTrue(capturedImeis.contains("12345"));
    }

    @Test
    void updateJourney_journeyNotFound_createsNewJourney() {
        final LocalDate dtStart = LocalDate.now();
        final LocalDate dtEnd = LocalDate.now().plusDays(1);

        // Arrange
        Long journeyId = 1L;
        Journey newJourneyData = new Journey();
        newJourneyData.setDescription("New Journey");
        newJourneyData.setStartDate(dtStart);
        newJourneyData.setEndDate(dtEnd);
        newJourneyData.setTrackedByImeis(Set.of(new Imei("67890", true, null, null, null, testUser)));

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.empty());
        Mockito.when(journeyRepository.save(newJourneyData)).thenReturn(newJourneyData);

        // Act
        Journey result = journeyService.updateJourney(journeyId, newJourneyData);

        // Assert
        assertNotNull(result);
        assertEquals("New Journey", result.getDescription());
        assertEquals(journeyId, result.getId());
        verify(journeyRepository, times(1)).findById(journeyId);
        verify(journeyRepository, times(1)).save(newJourneyData);

        verify(journeyRepository, times(1)).save(journeyCaptor.capture());
        Journey jCapt = journeyCaptor.getValue();
        assertEquals(jCapt.getDescription(),newJourneyData.getDescription());
        assertEquals(jCapt.getStartDate(),newJourneyData.getStartDate());
        assertEquals(jCapt.getEndDate(),newJourneyData.getEndDate());
        assertEquals(jCapt.getOwner(), newJourneyData.getOwner());
        List<String> capturedImeis = jCapt.getTrackedByImeis().stream().map(Imei::getImei).toList();
        assertEquals(1, capturedImeis.size());
        assertTrue(capturedImeis.contains("67890"));
    }

    @Test
    void startJourney_success() {
        // Arrange
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setTrackedByImeis(Set.of(new Imei("12345", true, null, null, null, testUser)));

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        Mockito.when(journeyRepository.save(journey)).thenReturn(journey);

        // Act
        Journey result = journeyService.startJourney(journeyId);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getStartDate());
        assertEquals(result.getOwner(),journey.getOwner());
        verify(journeyRepository, times(1)).findById(journeyId);
        verify(journeyRepository, times(1)).save(journey);
    }

    @Test
    void startJourney_journeyNotFound_throwsException() {
        // Arrange
        Long journeyId = 1L;
        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> journeyService.startJourney(journeyId));

        assertEquals("Journey not found with id: " + journeyId, exception.getMessage());
        verify(journeyRepository, times(1)).findById(journeyId);
        verify(journeyRepository, never()).save(any());
    }

    @Test
    void startJourney_noActiveImeis_throwsConstraintViolationException() {
        // Arrange
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setTrackedByImeis(Set.of(new Imei("12345", false, null, null, null, testUser)));
        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        // Act & Assert
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> journeyService.startJourney(journeyId));

        assertEquals("Cannot start journey. No tracker is active", exception.getMessage());
        verify(journeyRepository, times(1)).findById(journeyId);
        verify(journeyRepository, never()).save(any());
    }

    @Test
    void createNewParkSpotForJourneyNoNearBySpot_success() {
        // Arrange
        Long journeyId = 1L;
        String parkingSpotName = "Spot A";
        String parkingSpotDescription = "Description A";

        Imei imei = new Imei("12345", true, null, null, null, testUser);
        Position position = new Position(1.0f, 2.0f, (short) 3, (short) 4, (byte) 5, (short) 6, imei.getImei(), LocalDateTime.now());
        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setTrackedByImeis(Set.of(imei));

        ParkSpot expectedParkSpot = new ParkSpot();
        expectedParkSpot.setName(parkingSpotName);
        expectedParkSpot.setDescription(parkingSpotDescription);

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        Mockito.when(positionRepository.findTopByImeiOrderByDateTimeDesc(imei.getImei())).thenReturn(List.of(position));
        Mockito.when(parkSpotRepository.save(any())).thenReturn(expectedParkSpot);

        // Act
        ParkSpot result = null;
        result = journeyService.addOvernightParking(journeyId, parkingSpotName, parkingSpotDescription, true, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(parkingSpotName, result.getName());
        assertEquals(parkingSpotDescription, result.getDescription());
        verify(journeyRepository, times(1)).findById(journeyId);
        verify(wordPressPostService,times(1))
                    .createPost(parkingSpotName,parkingSpotDescription,position.getPoint().getY(), position.getPoint().getX() );
        ParkSpot save = verify(parkSpotRepository, times(1)).save(any());
    }


    @Test
    void createNewParkSpotForJourneyNearBySpotExists_success() {
        // Arrange
        Long journeyId = 1L;
        String parkingSpotName = "Spot A";
        String parkingSpotDescription = "Description A";

        Imei imei = new Imei("12345", true, null, null, null, testUser);
        Position position = new Position(1.0f, 2.0f, (short) 3, (short) 4, (byte) 5, (short) 6, imei.getImei(), LocalDateTime.now());
        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setTrackedByImeis(Set.of(imei));

        ParkSpot expectedParkSpot = new ParkSpot();
        expectedParkSpot.setName("near By Spot");
        expectedParkSpot.setDescription("near By Spot Description");

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        Mockito.when(positionRepository.findTopByImeiOrderByDateTimeDesc(imei.getImei())).thenReturn(List.of(position));
        Mockito.when(parkSpotRepository.save(any())).thenReturn(expectedParkSpot);
        Mockito.when(parkSpotRepository.findWithinDistance(1.0f, 2.0f, 50)).thenReturn(List.of(expectedParkSpot));

        // Act
        ParkSpot result = null;
        result = journeyService.addOvernightParking(journeyId, parkingSpotName, parkingSpotDescription, false, LocalDate.now() );


        // Assert
        assertNotNull(result);
        assertEquals(expectedParkSpot.getName(), result.getName());
        assertEquals(expectedParkSpot.getDescription(), result.getDescription());
        verify(journeyRepository, times(1)).findById(journeyId);
        verify(parkSpotRepository, times(1)).save(any());
        verify(journeyRepository,times(1)).save(any()) ;
        verify(parkSpotRepository, times(1)).findWithinDistance(1.0f, 2.0f, 50);
    }


    @Test
    void createNewParkSpotForJourney_noActiveTracker_throwsException() {
        // Arrange
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setTrackedByImeis(Set.of(new Imei("12345", false, null, null, null, testUser)));

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        // Act & Assert
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> journeyService.addOvernightParking(
                        journeyId, "Spot B", "Description B",false, LocalDate.now())
                );

        verify(journeyRepository, times(1)).findById(journeyId);
        verifyNoInteractions(positionRepository);
    }

    @Test
    void createNewParkSpotForJourney_noLastPosition_throwsException() {
        // Arrange
        Long journeyId = 1L;
        Imei imei = new Imei("12345", true, null, null, null, testUser);
        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setTrackedByImeis(Set.of(imei));

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        Mockito.when(positionRepository.findTopByImeiOrderByDateTimeDesc(imei.getImei())).thenReturn(List.of());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> journeyService
                        .addOvernightParking(journeyId, "Spot C", "Description C",false, LocalDate.now() ));

        assertEquals("No positions found for active tracker with IMEI: " + imei.getImei(), exception.getMessage());
        verify(journeyRepository, times(1)).findById(journeyId);
        verify(positionRepository, times(1)).findTopByImeiOrderByDateTimeDesc(imei.getImei());
        verifyNoInteractions(parkSpotRepository);
    }


    @Test
    void testIsJourneyActive_activeJourney() {
        // Setup
        LocalDate now = LocalDate.now();
        Journey journey = new Journey() ;
        journey.setStartDate(now.minusDays(1));
        journey.setEndDate(now.plusDays(1));

        // Act & Assert
        assertTrue(journeyService.isJourneyActive(journey));
    }

    @Test
    void testIsJourneyActive_journeyEnded() {
        LocalDate now = LocalDate.now();
        Journey journey = new Journey() ;
        journey.setStartDate(now.minusDays(2));
        journey.setEndDate(now.minusDays(1));
        assertFalse(journeyService.isJourneyActive(journey));
    }

    @Test
    void testIsJourneyActive_journeyNotStarted() {
        LocalDate now = LocalDate.now();
        Journey journey = new Journey() ;
        journey.setStartDate(now.plusDays(1));
        journey.setEndDate(now.minusDays(2));
        assertFalse(journeyService.isJourneyActive(journey));
    }

    @Test
    void testIsJourneyActive_nullDates() {
        Journey journey = new Journey();
        assertTrue(journeyService.isJourneyActive(journey));
    }


    @Test
    void testDetermineActiveImei_singleActive() {
        // Setup
        Journey journey = new Journey();
        journey.setTrackedByImeis(Set.of(
                new Imei("12345", false,null,null,null, testUser),
                new Imei("67890", true, null, null,null, testUser)
        ));

        // Act
        String activeImei = journeyService.determineActiveImei(journey);

        // Assert
        assertEquals("67890", activeImei);
    }

    @Test
    void testDetermineActiveImei_multipleActive() {
        // Setup
        Journey journey = new Journey();
        journey.setTrackedByImeis(Set.of(
                new Imei("12345", true,null,null,null, testUser),
                new Imei("67890", true, null, null, null, testUser)
        ));

        // Act
        String activeImei = journeyService.determineActiveImei(journey);
        boolean isValid = "12345".equals(activeImei) || "67890".equals(activeImei);
        // there is no specification what value it will be
        assertTrue(isValid, "The active IMEI should be either '12345' or '67890'");
    }

    @Test
    void testDetermineActiveImei_noActive() {
        Journey journey = new Journey();
        journey.setTrackedByImeis(Set.of(
                new Imei("12345", false,null,null,null, testUser),
                new Imei("67890", false, null, null, null, testUser)
        ));

        String activeImei = journeyService.determineActiveImei(journey);
        assertNull(activeImei);
    }

    @Test
    void testDetermineActiveImei_emptyList() {
        Journey journey = new Journey();
        journey.setTrackedByImeis(Set.of());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                journeyService.determineActiveImei(journey)
        );
        assertEquals("Journey or associated IMEIs cannot be null/empty.", exception.getMessage());
    }

    @Test
    void testDetermineActiveImei_nullJourney() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                journeyService.determineActiveImei(null)
        );
        assertEquals("Journey or associated IMEIs cannot be null/empty.", exception.getMessage());
    }

    @Test
    public void testCreateNewJourney_ShouldSetAuthenticatedUserAsOwner() {
        // Mock authenticated user in SecurityContextHolder
        User authenticatedUser = new User();
        authenticatedUser.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // Mock repository behavior
            Journey journey = new Journey();
            journey.setName("Test Journey");

            Journey savedJourney = new Journey();
            savedJourney.setId(1L);
            savedJourney.setName(journey.getName());
            savedJourney.setOwner(authenticatedUser);

            when(journeyRepository.save(any(Journey.class))).thenReturn(savedJourney);

            // Create a journey via service
            Journey createdJourney = journeyService.createNewJourney(journey);

            // Verify that the owner was set to the authenticated user
            assertNotNull(createdJourney);
            assertEquals(authenticatedUser, createdJourney.getOwner());
        }
    }
}