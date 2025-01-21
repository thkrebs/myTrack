package com.tmv.core.service;

import com.tmv.core.config.CoreConfiguration;
import com.tmv.core.model.Imei;
import com.tmv.core.model.Journey;
import com.tmv.core.model.Position;
import com.tmv.core.persistence.JourneyRepository;
import com.tmv.core.persistence.PositionRepository;
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

import java.util.*;

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

    @InjectMocks
    private JourneyServiceImpl journeyService;

    @Captor
    private ArgumentCaptor<List<String>> imeiCaptor;

    @Captor
    private ArgumentCaptor<Journey> journeyCaptor;

    @BeforeEach
    void setUp() {
        journeyService = new JourneyServiceImpl(positionRepository, journeyRepository);
    }

    @Test
    void trackForJourney_success() {
        // Arrange
        Long journeyId = 1L;
        final String imei = "12345";
        final Date dt = new GregorianCalendar(2023, Calendar.OCTOBER, 31).getTime();

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
        final Date dtStart = new GregorianCalendar(2023, Calendar.OCTOBER, 31).getTime();
        final Date dtEnd = new GregorianCalendar(2023, Calendar.NOVEMBER, 31).getTime();

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
        final Date dtStart = new GregorianCalendar(2023, Calendar.OCTOBER, 31).getTime();
        final Date dtEnd = new GregorianCalendar(2023, Calendar.NOVEMBER, 31).getTime();

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

}