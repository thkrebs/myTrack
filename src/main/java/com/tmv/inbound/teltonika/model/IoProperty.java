package com.tmv.inbound.teltonika.model;

public class IoProperty {
    private short id;
    private final Long value; // Using Long to replicate nullable long in C#
    private final byte[] arrayValue;

    private IoProperty(short id, Long value, byte[] arrayValue) {
        this.id = id;
        this.value = value;
        this.arrayValue = arrayValue;
    }

    public int getId() {
        return id;
    }

    public Long getValue() {
        return value;
    }

    public byte[] getArrayValue() {
        return arrayValue;
    }

    public static IoProperty create(short id, Long value) {
        return new IoProperty(id, value, null);
    }

    public static IoProperty create(short id, byte[] value) {
        return new IoProperty(id, null, value);
    }
}