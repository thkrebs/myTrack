package com.tmv.ingest.teltonika.model;

import lombok.Getter;

import java.util.List;

public class IoElement {
    @Getter private final int eventId;
    @Getter private final int propertiesCount;
    @Getter private final List<IoProperty> properties;
    @Getter private final Byte originType; // Using Byte to replicate nullable byte in C#

    private IoElement(int eventId, int propertiesCount, List<IoProperty> properties, Byte originType) {
        this.eventId = eventId;
        this.propertiesCount = propertiesCount;
        this.properties = properties;
        this.originType = originType;
    }

    public static IoElement create(int eventId, int propertiesCount, List<IoProperty> properties, Byte originType) {
        return new IoElement(eventId, propertiesCount, properties, originType);
    }

    public static IoElement create(int eventId, int propertiesCount, List<IoProperty> properties) {
        return new IoElement(eventId, propertiesCount, properties, null);
    }
}