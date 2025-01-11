package com.tmv.core.service;


import com.tmv.core.model.Position;
import com.tmv.core.persistence.PositionRepository;
import com.tmv.ingest.NewTcpDataPacketEvent;
import com.tmv.ingest.teltonika.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ComponentScan(basePackages = "com.tmv")
public class PositionDataEventListenerTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @MockBean
    private PositionRepository repository;


    @Test
    public void testDataSavedToDB() {
        final float lng = 8.0f;
        final float lat = 50.0f;
        final short altitude = 100;
        final short angle = 0;
        final byte satellites = 10;
        final short speed = 5;
        final String imei = "xxx";

        final Date dateTime = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));

        List<AvlData> data = List.of(
                AvlData.create(0, dateTime, GpsElement.create(lng, lat, altitude, speed, angle, satellites), null),
                AvlData.create(0, dateTime, GpsElement.create(lng, lat, altitude, speed, angle, satellites), null));

        Position pos = new Position(lng, lat, altitude, angle, satellites, speed, imei, dateTime);

        AvlDataCollection avlData = AvlDataCollection.create(8, 2, data);
        TcpDataPacket tp = TcpDataPacket.create(0, 10, 11, 8, avlData);
        tp.setImei(imei);
        publisher.publishEvent(new NewTcpDataPacketEvent(this, tp));
        verify(repository, times(2)).save(argThat((p) -> {
            boolean lngMatches = Double.valueOf(lng).equals(p.getPoint().getCoordinate().x);
            boolean latMatches = Double.valueOf(lat).equals(p.getPoint().getCoordinate().y);
            boolean altMatches = Short.valueOf(altitude).equals(p.getAltitude());
            boolean angleMatches = Short.valueOf(angle).equals(p.getAngle());
            boolean satellitesMatches = Byte.valueOf(satellites).equals(p.getSatellites());
            boolean speedMatches = Short.valueOf(speed).equals(p.getSpeed());
            boolean imeiMatches = imei.equals(p.getImei());
            boolean dateTimeMatches = dateTime.equals(p.getDateTime());
            return lngMatches && latMatches && altMatches && angleMatches && satellitesMatches && speedMatches && imeiMatches && dateTimeMatches;
        }));
    }
}