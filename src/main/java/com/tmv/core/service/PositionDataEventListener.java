package com.tmv.core.service;

import com.tmv.core.model.Position;
import com.tmv.core.persistence.PositionRepository;
import com.tmv.ingest.NewTcpDataPacketEvent;
import com.tmv.ingest.teltonika.model.AvlData;
import com.tmv.ingest.teltonika.model.GpsElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class PositionDataEventListener {

    @Autowired
    private PositionRepository repository;

    @EventListener
    public void handle(NewTcpDataPacketEvent event) {
        assert(repository != null);
        log.debug("New event handler called");

        String imei = event.getDataPacket().getImei();
        for (AvlData element : event.getDataPacket().getAvlData().getData()) {
            GpsElement gpsEle = element.getGpsElement();
            float lng = gpsEle.getLng();
            float lat = gpsEle.getLat();
            short altitude = gpsEle.getAltitude();
            short angle = gpsEle.getAngle();
            byte satellites = gpsEle.getSatellites();
            short speed = gpsEle.getSpeed();
            LocalDateTime dateTime = element.getDateTime();
            Position position = new Position(lng, lat, altitude, angle, satellites, speed, imei, dateTime);
            repository.save(position);
        }
        log.info("{} new position elements stored for IMEI={}",event.getDataPacket().getAvlData().getData().size(), imei);
    }
}
