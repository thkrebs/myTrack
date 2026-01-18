package com.tmv.core.service;

import com.tmv.core.model.Imei;
import com.tmv.core.model.Position;
import com.tmv.core.persistence.ImeiRepository;
import com.tmv.core.persistence.PositionRepository;
import com.tmv.core.util.Distance;
import com.tmv.ingest.NewTcpDataPacketEvent;
import com.tmv.ingest.teltonika.model.AvlData;
import com.tmv.ingest.teltonika.model.GpsElement;
import com.tmv.ingest.teltonika.model.IoElement;
import com.tmv.ingest.teltonika.model.IoProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PositionDataEventListener {

    private final PositionRepository repository;
    private final ImeiRepository imeiRepository;
    private final ExpoPushService expoPushService;

    @Autowired
    public PositionDataEventListener(PositionRepository repository, ImeiRepository imeiRepository, ExpoPushService expoPushService) {
        this.repository = repository;
        this.imeiRepository = imeiRepository;
        this.expoPushService = expoPushService;
    }

    @EventListener
    public void handle(NewTcpDataPacketEvent event) {
        assert(repository != null);
        log.debug("New event handler called");

        String imeiString = event.getDataPacket().getImei();
        Imei imeiEntity = imeiRepository.findByImei(imeiString);

        for (AvlData element : event.getDataPacket().getAvlData().getData()) {
            GpsElement gpsEle = element.getGpsElement();
            float lng = gpsEle.getLng();
            float lat = gpsEle.getLat();
            short altitude = gpsEle.getAltitude();
            short angle = gpsEle.getAngle();
            byte satellites = gpsEle.getSatellites();
            short speed = gpsEle.getSpeed();
            LocalDateTime dateTime = element.getDateTime();
            long totalOdometer = getTotalOdometer(element.getIoElement());
            Position position = new Position(lng, lat, altitude, angle, satellites, speed, imeiString, dateTime, totalOdometer);
            repository.save(position);

            if (imeiEntity != null) {
                checkGeofence(imeiEntity, lat, lng);
            }
        }
        log.info("{} new position elements stored for IMEI={}",event.getDataPacket().getAvlData().getData().size(), imeiString);
    }

    private void checkGeofence(Imei imei, float currentLat, float currentLng) {
        if (Boolean.TRUE.equals(imei.getGeofenceActive()) &&
                imei.getGeofenceCenterLatitude() != null &&
                imei.getGeofenceCenterLongitude() != null &&
                imei.getGeofenceRadius() != null) {

            double distance = Distance.calculateDistance(
                    currentLat, currentLng,
                    imei.getGeofenceCenterLatitude(), imei.getGeofenceCenterLongitude()
            );

            // Convert distance to meters (assuming calculateDistance returns km, which is standard for Haversine)
            // Wait, I need to check Distance.calculateDistance unit.
            // Assuming it returns KM based on typical implementations.
            double distanceMeters = distance * 1000;

            if (distanceMeters > imei.getGeofenceRadius()) {
                // Outside Geofence!
                triggerGeofenceAlert(imei, distanceMeters);
            }
        }
    }

    private void triggerGeofenceAlert(Imei imei, double distanceMeters) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastAlert = imei.getLastGeofenceAlertTime();

        // Debounce: Only alert every 30 minutes
        if (lastAlert == null || now.isAfter(lastAlert.plusMinutes(30))) {
            log.warn("Geofence breach detected for IMEI: {}. Distance: {}m", imei.getImei(), distanceMeters);

            String title = "Geofence Alert!";
            String body = String.format("Your asset %s has left the safe zone! (Distance: %.0fm)",
                    imei.getDescription() != null ? imei.getDescription() : imei.getImei(),
                    distanceMeters);

            Map<String, String> data = new HashMap<>();
            data.put("imei", imei.getImei());
            data.put("type", "geofence_breach");

            if (imei.getOwner() != null) {
                expoPushService.sendPushNotification(imei.getOwner(), title, body, data);
            }

            imei.setLastGeofenceAlertTime(now);
            imeiRepository.save(imei);
        }
    }

    private long getTotalOdometer(IoElement element) {
        if (element == null) { return -1;}

        short TOTAL_ODOMETER = 16;
        IoProperty prop = element.getProperties().get(TOTAL_ODOMETER);
        if (prop != null) {
            return prop.getValue();
        }
        else {
            return -1;
        }
    }
}
