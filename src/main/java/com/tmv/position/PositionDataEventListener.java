package com.tmv.position;

import com.tmv.inbound.NewTcpDataPacketEvent;
import com.tmv.inbound.teltonika.model.AvlData;
import com.tmv.inbound.teltonika.model.GpsElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class PositionDataEventListener {

    @Autowired
    private PositionRepository repository;

    @EventListener
    public void handle(NewTcpDataPacketEvent event) {
        assert(repository != null);

        String imei = event.getDataPacket().getImei();
        for (AvlData element : event.getDataPacket().getAvlData().getData()) {
            GpsElement gpsEle = element.getGpsElement();
            float lng = gpsEle.getLng();
            float lat = gpsEle.getLat();
            short altitude = gpsEle.getAltitude();
            short angle = gpsEle.getAngle();
            byte satellites = gpsEle.getSatellites();
            short speed = gpsEle.getSpeed();
            Date dateTime = element.getDateTime();
            Position position = new Position(lng, lat, altitude, angle, satellites, speed, imei, dateTime);
            repository.save(position);
        }
        log.info("{} new position elements stored for IMEI={}",event.getDataPacket().getAvlData().getData().size(), imei);
    }
}
