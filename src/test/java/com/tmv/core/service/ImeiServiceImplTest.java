package com.tmv.core.service;

import com.tmv.core.config.CoreConfiguration;
import com.tmv.core.exception.ConstraintViolationException;
import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.Imei;
import com.tmv.core.model.Journey;
import com.tmv.core.model.User;
import com.tmv.core.persistence.ImeiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ComponentScan(basePackages = "com.tmv")
@PropertySource("classpath:application.properties")
@Import(CoreConfiguration.class)
@ExtendWith(MockitoExtension.class)
class ImeiServiceImplTest {

    @Mock
    private ImeiRepository imeiRepository;

    @InjectMocks
    private ImeiServiceImpl imeiService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L); // Mocked user ID
        testUser.setUsername("testuser");
        imeiService = new ImeiServiceImpl(imeiRepository);
    }

    @Test
    void testIsActive_ActiveRecord() {
        // Arrange
        String imei = "123456789";
        Imei imeiRecord = new Imei();
        imeiRecord.setActive(true);
        when(imeiRepository.findByImei(imei)).thenReturn(imeiRecord);

        // Act
        boolean isActive = imeiService.isActive(imei);

        // Assert
        assertTrue(isActive);
        verify(imeiRepository, times(1)).findByImei(imei);
    }

    @Test
    void testIsActive_InactiveRecord() {
        // Arrange
        String imei = "123456789";
        Imei imeiRecord = new Imei();
        imeiRecord.setActive(false);
        when(imeiRepository.findByImei(imei)).thenReturn(imeiRecord);

        // Act
        boolean isActive = imeiService.isActive(imei);

        // Assert
        assertFalse(isActive);
        verify(imeiRepository, times(1)).findByImei(imei);
    }

    @Test
    void testIsActive_NoRecordFound() {
        // Arrange
        String imei = "123456789";
        when(imeiRepository.findByImei(imei)).thenReturn(null);

        // Act
        boolean isActive = imeiService.isActive(imei);

        // Assert
        assertFalse(isActive);
        verify(imeiRepository, times(1)).findByImei(imei);
    }

    @Test
    void testCreateNewImei() {
        // Arrange
        Imei newImei = new Imei();
        when(imeiRepository.save(newImei)).thenReturn(newImei);

        // Act
        Imei createdImei = imeiService.createNewImei(newImei);

        // Assert
        assertNotNull(createdImei);
        verify(imeiRepository, times(1)).save(newImei);
    }

    @Test
    void testGetImeiById_Found() {
        // Arrange
        Long id = 1L;
        Imei imei = new Imei();
        when(imeiRepository.findById(id)).thenReturn(Optional.of(imei));

        // Act
        Optional<Imei> foundImei = imeiService.getImeiById(id);

        // Assert
        assertTrue(foundImei.isPresent());
        assertEquals(imei, foundImei.get());
        verify(imeiRepository, times(1)).findById(id);
    }

    @Test
    void testGetImeiById_NotFound() {
        // Arrange
        Long id = 1L;
        when(imeiRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Imei> foundImei = imeiService.getImeiById(id);

        // Assert
        assertFalse(foundImei.isPresent());
        verify(imeiRepository, times(1)).findById(id);
    }

    @Test
    void testUpdateImei_Found() {
        // Arrange
        Long id = 1L;
        Imei existingImei = new Imei();
        Imei newImei = new Imei();
        newImei.setImei("123456789");
        newImei.setPhoneNumber("1234567890");
        newImei.setActive(true);

        when(imeiRepository.findById(id)).thenReturn(Optional.of(existingImei));
        when(imeiRepository.save(any(Imei.class))).thenReturn(newImei);

        // Act
        Imei updatedImei = imeiService.updateImei(id, newImei);

        // Assert
        assertNotNull(updatedImei);
        assertEquals(updatedImei.getImei(), newImei.getImei());
        assertEquals(updatedImei.getPhoneNumber(), newImei.getPhoneNumber());
        verify(imeiRepository, times(1)).findById(id);
        verify(imeiRepository, times(1)).save(existingImei);
    }

    @Test
    void testUpdateImei_NotFound() {
        // Arrange
        Long id = 1L;
        Imei newImei = new Imei();
        newImei.setId(id);

        when(imeiRepository.findById(id)).thenReturn(Optional.empty());
        when(imeiRepository.save(newImei)).thenReturn(newImei);

        // Act
        Imei updatedImei = imeiService.updateImei(id, newImei);

        // Assert
        assertNotNull(updatedImei);
        assertEquals(id, updatedImei.getId());
        verify(imeiRepository, times(1)).findById(id);
        verify(imeiRepository, times(1)).save(newImei);
    }

    @Test
    void testDeleteImei_Found() {
        // Arrange
        Long id = 1L;
        when(imeiRepository.existsById(id)).thenReturn(true);

        // Act
        imeiService.deleteImei(id);

        // Assert
        verify(imeiRepository, times(1)).existsById(id);
        verify(imeiRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteImei_NotFound() {
        // Arrange
        Long id = 1L;
        when(imeiRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> imeiService.deleteImei(id));
        verify(imeiRepository, times(1)).existsById(id);
        verify(imeiRepository, times(0)).deleteById(anyLong());
    }


    @Test
    void testDeleteImei_JourneysExist() {

        // setup journey
        final String imeiStr1 = "123456789012345";
        final String imeiStr2 = "123456789012365";

        Journey testJourney = new Journey();
        testJourney.setId(1L);
        testJourney.setDescription("Test Journey");
        ;
        Imei firstImei =  new Imei(imeiStr1, true, Date.from(Instant.now()), Date.from(Instant.now()), "123", testUser);
        firstImei.setJourneys(Set.of(testJourney));

        // Arrange
        Long id = 1L;
        when(imeiRepository.existsById(id)).thenReturn(true);
        when(imeiRepository.findById(id)).thenReturn(Optional.of(firstImei));

        // Assert
        assertThrows(ConstraintViolationException.class, () -> imeiService.deleteImei(id));
        verify(imeiRepository, times(1)).existsById(id);
        verify(imeiRepository, times(1)).findById(id);
    }
}