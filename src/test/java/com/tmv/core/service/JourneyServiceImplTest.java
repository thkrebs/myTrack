package com.tmv.core.service;

import com.tmv.core.config.CoreConfiguration;
import com.tmv.core.exception.ConstraintViolationException;
import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.*;
import com.tmv.core.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
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

    @InjectMocks
    private JourneyServiceImpl journeyService;

    @Captor
    private ArgumentCaptor<List<String>> imeiCaptor;

    @Captor
    private ArgumentCaptor<Journey> journeyCaptor;

    @BeforeEach
    void setUp() {
        journeyService = new JourneyServiceImpl(positionRepository, journeyRepository, parkSpotRepository, overnightParkingRepository, imeiRepository);
    }

    @Test
    void trackForJourney_success() {
        // Arrange
        Long journeyId = 1L;
        final String imei = "12345";
        final LocalDateTime dt = LocalDateTime.now();

        Imei imei1 = new Imei(imei, true, null, null, null);
        Imei imei2 = new Imei("67890", true, null, null, null);

        Journey journey = new Journey();
        journey.setTrackedByImeis(new HashSet<>(List.of(imei1, imei2)));

        Position position1 = new Position(1.0f,2.0f, (short) 3, (short) 4, (byte) 5, (short) 6,imei, dt );
        Position position2 = new Position(3.0f,4.0f, (short) 4, (short) 5, (byte) 6, (short) 7,imei, dt );

        List<Position> positions = List.of(position1, position2);

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        Mockito.when(positionRepository.findByImeiInOrderByDateTimeAsc(anyList())).thenReturn(positions);

        // Act
        LineString result = journeyService.trackForJourney(journeyId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getNumPoints());
        assertEquals(1.0, result.getCoordinateN(0).getX());
        assertEquals(2.0, result.getCoordinateN(0).getY());
        assertEquals(3.0, result.getCoordinateN(1).getX());
        assertEquals(4.0, result.getCoordinateN(1).getY());

        verify(journeyRepository, times(1)).findById(journeyId);
        verify(positionRepository, times(1)).findByImeiInOrderByDateTimeAsc(imeiCaptor.capture());
        List<String> capturedImeis = imeiCaptor.getValue();
        assertEquals(2, capturedImeis.size());
        assertTrue(capturedImeis.contains("12345"));
        assertTrue(capturedImeis.contains("67890"));
    }

    @Test
    void trackForJourney_journeyNotFound_throwsException() {
        // Arrange
        Long journeyId = 1L;
        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                journeyService.trackForJourney(journeyId));

        assertEquals("Journey not found with id: " + journeyId, exception.getMessage());
        verify(journeyRepository, times(1)).findById(journeyId);
        verifyNoInteractions(positionRepository);
    }

    @Test
    void trackForJourney_noPositions_returnsEmptyLineString() {
        // Arrange
        Long journeyId = 1L;

        Imei imei1 = new Imei("98765", true, null, null, null);
        Journey journey = new Journey();
        journey.setTrackedByImeis(Set.of(imei1));

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        Mockito.when(positionRepository.findByImeiInOrderByDateTimeAsc(anyList())).thenReturn(List.of());

        // Act
        LineString result = journeyService.trackForJourney(journeyId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getNumPoints());

        verify(journeyRepository, times(1)).findById(journeyId);
        verify(positionRepository, times(1)).findByImeiInOrderByDateTimeAsc(anyList());
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
        newJourneyData.setTrackedByImeis(Set.of(new Imei("12345", true, null, null, null)));

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
        newJourneyData.setTrackedByImeis(Set.of(new Imei("67890", true, null, null, null)));

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
        List<String> capturedImeis = jCapt.getTrackedByImeis().stream().map(Imei::getImei).toList();
        assertEquals(1, capturedImeis.size());
        assertTrue(capturedImeis.contains("67890"));
    }

    @Test
    void startJourney_success() {
        // Arrange
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setTrackedByImeis(Set.of(new Imei("12345", true, null, null, null)));

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        Mockito.when(journeyRepository.save(journey)).thenReturn(journey);

        // Act
        Journey result = journeyService.startJourney(journeyId);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getStartDate());
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
        journey.setTrackedByImeis(Set.of(new Imei("12345", false, null, null, null)));
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

        Imei imei = new Imei("12345", true, null, null, null);
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
        ParkSpot result = journeyService.addOvernightParking(journeyId, parkingSpotName, parkingSpotDescription);

        // Assert
        assertNotNull(result);
        assertEquals(parkingSpotName, result.getName());
        assertEquals(parkingSpotDescription, result.getDescription());
        verify(journeyRepository, times(1)).findById(journeyId);
        verify(parkSpotRepository, times(1)).save(any());
    }


    @Test
    void createNewParkSpotForJourneyNearBySpotExists_success() {
        // Arrange
        Long journeyId = 1L;
        String parkingSpotName = "Spot A";
        String parkingSpotDescription = "Description A";

        Imei imei = new Imei("12345", true, null, null, null);
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
        ParkSpot result = journeyService.addOvernightParking(journeyId, parkingSpotName, parkingSpotDescription);

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
        journey.setTrackedByImeis(Set.of(new Imei("12345", false, null, null, null)));

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        // Act & Assert
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> journeyService.addOvernightParking(journeyId, "Spot B", "Description B"));

        verify(journeyRepository, times(1)).findById(journeyId);
        verifyNoInteractions(positionRepository);
    }

    @Test
    void createNewParkSpotForJourney_noLastPosition_throwsException() {
        // Arrange
        Long journeyId = 1L;
        Imei imei = new Imei("12345", true, null, null, null);
        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setTrackedByImeis(Set.of(imei));

        Mockito.when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        Mockito.when(positionRepository.findTopByImeiOrderByDateTimeDesc(imei.getImei())).thenReturn(List.of());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> journeyService.addOvernightParking(journeyId, "Spot C", "Description C"));

        assertEquals("No positions found for active tracker with IMEI: " + imei.getImei(), exception.getMessage());
        verify(journeyRepository, times(1)).findById(journeyId);
        verify(positionRepository, times(1)).findTopByImeiOrderByDateTimeDesc(imei.getImei());
        verifyNoInteractions(parkSpotRepository);
    }
}