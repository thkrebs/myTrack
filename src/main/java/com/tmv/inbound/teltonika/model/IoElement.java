package com.tmv.inbound.teltonika.model;

import java.util.List;

public class IoElement {
    private final int eventId;
    private final int propertiesCount;
    private final List<IoProperty> properties;
    private final Byte originType; // Using Byte to replicate nullable byte in C#

    private IoElement(int eventId, int propertiesCount, List<IoProperty> properties, Byte originType) {
        this.eventId = eventId;
        this.propertiesCount = propertiesCount;
        this.properties = properties;
        this.originType = originType;
    }

    public int getEventId() {
        return eventId;
    }

    public int getPropertiesCount() {
        return propertiesCount;
    }

    public List<IoProperty> getProperties() {
        return properties;
    }

    public Byte getOriginType() {
        return originType;
    }

    public static IoElement create(int eventId, int propertiesCount, List<IoProperty> properties, Byte originType) {
        return new IoElement(eventId, propertiesCount, properties, originType);
    }

    public static IoElement create(int eventId, int propertiesCount, List<IoProperty> properties) {
        return new IoElement(eventId, propertiesCount, properties, null);
    }
}