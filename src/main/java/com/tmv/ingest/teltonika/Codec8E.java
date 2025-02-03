package com.tmv.ingest.teltonika;

import java.io.DataInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.tmv.ingest.teltonika.model.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Codec8E {
    private static final LocalDateTime AvlEpoch = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC); // 1. Januar 1970, 00:00:00 UTC
    private final DataInputStream reader;

    public Codec8E(DataInputStream reader) {
        if (reader == null) {
            throw new IllegalArgumentException("reader cannot be null");
        }
        this.reader = reader;
    }

    /**
     * Decode AVL data packet
     */
    public AvlDataCollection decodeAvlDataCollection() throws IOException {
        byte dataCount = reader.readByte();
        List<AvlData> data = new ArrayList<>();

        for (int i = 0; i < dataCount; i++) {
            log.debug("decode paket {}", i);
            AvlData avlData = decodeAvlData();
            data.add(avlData);
        }
        return AvlDataCollection.create(0x8E, dataCount, data);
    }

    /**
     * Decode single AVL data
     */
    private AvlData decodeAvlData() throws IOException {
        long timestamp = reader.readLong();
        LocalDateTime dateTime = AvlEpoch.plusNanos(timestamp * 1_000_000);
        log.debug("decoded DateTime {}", dateTime);

        int priority = reader.readByte();
        log.debug("decoded priority {}", priority);

        // GPS element decoding
        GpsElement gpsElement = decodeGpsElement();
        log.debug("lng: {}, lat: {}, speed: {}, alt: {}, angle: {}, satellites: {}",
                    gpsElement.getLng(), gpsElement.getLat(), gpsElement.getSpeed(),
                    gpsElement.getAltitude(), gpsElement.getAngle(), gpsElement.getSatellites());

        // IO Element decoding
        int eventId = reader.readShort();
        int propertiesCount = reader.readShort();

        // IO Element Properties decoding
        List<IoProperty> ioProperties = decodeIoProperties();
        IoElement ioElement = IoElement.create(eventId, propertiesCount, ioProperties);

        return AvlData.create(priority, dateTime, gpsElement, ioElement);
    }

    /**
     * Decode Gps element
     */
    private GpsElement decodeGpsElement() throws IOException {
        final float divisor =  10000000L; // shift
        int longitude = reader.readInt();
        int latitude = reader.readInt();
        short altitude = reader.readShort();
        short angle = reader.readShort();
        byte satellites = reader.readByte();
        short speed = reader.readShort();

        return GpsElement.create(longitude/divisor, latitude/divisor, altitude, speed, angle, satellites);
    }

    private List<IoProperty> decodeIoProperties() throws IOException {
        List<IoProperty> result = new ArrayList<>();

        // total number of I/O properties which length is 1 byte
        int ioCountInt8 = reader.readShort();
        for (int i = 0; i < ioCountInt8; i++) {
            short propertyId = reader.readShort();
            long value = reader.readByte();
            log.debug("decoded propertyId {} : {}", propertyId, value);
            result.add(IoProperty.create(propertyId, value));
        }

        // total number of I/O properties which length is 2 bytes
        int ioCountInt16 = reader.readShort();
        for (int i = 0; i < ioCountInt16; i++) {
            short propertyId = reader.readShort();
            long value = reader.readShort();
            log.debug("decoded propertyId (2 bytes) {} : {}", propertyId, value);
            result.add(IoProperty.create(propertyId, value));
        }

        // total number of I/O properties which length is 4 bytes
        int ioCountInt32 = reader.readShort();
        for (int i = 0; i < ioCountInt32; i++) {
            short propertyId = reader.readShort();
            long value = reader.readInt();
            log.debug("decoded propertyId (4 bytes) {} : {}", propertyId, value);
            result.add(IoProperty.create(propertyId, value));
        }

        // total number of I/O properties which length is 8 bytes
        int ioCountInt64 = reader.readShort();
        for (int i = 0; i < ioCountInt64; i++) {
            short propertyId = reader.readShort();
            long value = reader.readLong();
            log.debug("decoded propertyId (8 bytes) {} : {}", propertyId, value);
            result.add(IoProperty.create(propertyId, value));
        }

        int ioCountX = reader.readShort();
        for (int i = 0; i < ioCountX; i++) {
            short propertyId = reader.readShort();
            int elementLength = reader.readShort();
            byte[] value = new byte[elementLength];
            value = reader.readNBytes(elementLength);
            log.debug("decoded multibyte propertyId (byte array) {}", propertyId);
            result.add(IoProperty.create(propertyId, value));
        }
        return result;
    }
}
