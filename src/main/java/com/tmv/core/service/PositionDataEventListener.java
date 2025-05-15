package com.tmv.core.service;

import com.tmv.core.model.Position;
import com.tmv.core.persistence.PositionRepository;
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
            long totalOdometer = getTotalOdometer(element.getIoElement());
            Position position = new Position(lng, lat, altitude, angle, satellites, speed, imei, dateTime, totalOdometer);
            repository.save(position);
        }
        log.info("{} new position elements stored for IMEI={}",event.getDataPacket().getAvlData().getData().size(), imei);
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
