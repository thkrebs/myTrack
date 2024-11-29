package com.tmv.inbound.teltonika.model;

import lombok.Getter;

public class IoProperty {
    @Getter private short id;
    @Getter private final Long value; // Using Long to replicate nullable long in C#
    @Getter private final byte[] arrayValue;

    private IoProperty(short id, Long value, byte[] arrayValue) {
        this.id = id;
        this.value = value;
        this.arrayValue = arrayValue;
    }

    public static IoProperty create(short id, Long value) {
        return new IoProperty(id, value, null);
    }
    public static IoProperty create(short id, byte[] value) {
        return new IoProperty(id, null, value);
    }
}