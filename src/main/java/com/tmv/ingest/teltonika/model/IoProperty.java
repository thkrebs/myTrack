package com.tmv.ingest.teltonika.model;

import lombok.Getter;

@Getter
public class IoProperty {
    private final short id;
    private final Long value; // Using Long to replicate nullable long in C#
    private final byte[] arrayValue;

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