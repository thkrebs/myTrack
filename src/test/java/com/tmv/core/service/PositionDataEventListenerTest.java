package com.tmv.core.service;

import com.tmv.core.model.Imei;
import com.tmv.core.model.User;
import com.tmv.core.persistence.ImeiRepository;
import com.tmv.core.persistence.PositionRepository;
import com.tmv.ingest.NewTcpDataPacketEvent;
import com.tmv.ingest.teltonika.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PositionDataEventListenerTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private ImeiRepository imeiRepository;

    @Mock
    private ExpoPushService expoPushService;

    @InjectMocks
    private PositionDataEventListener listener;

    private Imei testImei;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");

        testImei = new Imei();
        testImei.setImei("123456789012345");
        testImei.setOwner(testUser);
        testImei.setGeofenceActive(true);
        testImei.setGeofenceCenterLatitude(50.0);
        testImei.setGeofenceCenterLongitude(10.0);
        testImei.setGeofenceRadius(1000); // 1km radius
    }

    @Test
    void handle_insideGeofence_noAlert() {
        // Position inside geofence (very close to center)
        NewTcpDataPacketEvent event = createEvent(50.001f, 10.001f);

        when(imeiRepository.findByImei("123456789012345")).thenReturn(testImei);

        listener.handle(event);

        verify(positionRepository, times(1)).save(any());
        verify(expoPushService, never()).sendPushNotification(any(), any(), any(), any());
    }

    @Test
    void handle_outsideGeofence_triggersAlert() {
        // Position far outside geofence (e.g., 51.0, 11.0 is > 100km away)
        NewTcpDataPacketEvent event = createEvent(51.0f, 11.0f);

        when(imeiRepository.findByImei("123456789012345")).thenReturn(testImei);

        listener.handle(event);

        verify(positionRepository, times(1)).save(any());
        verify(expoPushService, times(1)).sendPushNotification(eq(testUser), anyString(), anyString(), anyMap());
        verify(imeiRepository, times(1)).save(testImei); // Should update lastAlertTime
        assertNotNull(testImei.getLastGeofenceAlertTime());
    }

    @Test
    void handle_outsideGeofence_debounced() {
        // Set last alert to just now
        testImei.setLastGeofenceAlertTime(LocalDateTime.now().minusMinutes(5));

        NewTcpDataPacketEvent event = createEvent(51.0f, 11.0f);

        when(imeiRepository.findByImei("123456789012345")).thenReturn(testImei);

        listener.handle(event);

        verify(positionRepository, times(1)).save(any());
        verify(expoPushService, never()).sendPushNotification(any(), any(), any(), any());
        verify(imeiRepository, never()).save(testImei); // Should NOT update lastAlertTime
    }

    @Test
    void handle_geofenceInactive_noAlert() {
        testImei.setGeofenceActive(false);
        NewTcpDataPacketEvent event = createEvent(51.0f, 11.0f);

        when(imeiRepository.findByImei("123456789012345")).thenReturn(testImei);

        listener.handle(event);

        verify(expoPushService, never()).sendPushNotification(any(), any(), any(), any());
    }

    private NewTcpDataPacketEvent createEvent(float lat, float lng) {
        GpsElement gps = GpsElement.create(lng, lat, (short) 100, (short) 0, (short) 0, (byte) 5);
        IoElement io = IoElement.create(0, 0, new java.util.HashMap<>());
        AvlData avlData = AvlData.create(0, LocalDateTime.now(), gps, io);
        AvlDataCollection collection = AvlDataCollection.create(0, 1, List.of(avlData));
        
        // Use static factory method and setter
        TcpDataPacket packet = TcpDataPacket.create(0, 0, 0, 0, collection);
        packet.setImei("123456789012345");
        
        return new NewTcpDataPacketEvent(this, packet);
    }
}
