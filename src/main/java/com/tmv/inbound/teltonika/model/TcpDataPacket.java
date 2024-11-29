package com.tmv.inbound.teltonika.model;

public class TcpDataPacket {
    private final int preamble;
    private final int length;
    private final int crc;
    private final AvlDataCollection avlData;
    private final int codecId;
    private String imei;

    private TcpDataPacket(int preamble, int length, int crc, int codecId, AvlDataCollection avlDataCollection) {
        this.preamble = preamble;
        this.length = length;
        this.crc = crc;
        this.codecId = codecId;
        this.avlData = avlDataCollection;
    }

    public int getPreamble() {
        return preamble;
    }

    public int getLength() {
        return length;
    }

    public int getCrc() {
        return crc;
    }

    public AvlDataCollection getAvlData() {
        return avlData;
    }

    public int getCodecId() {
        return codecId;
    }

    public String getImei() { return imei; }

    public void setImei(String imei) { this.imei = imei; }

    public static TcpDataPacket create(int preamble, int length, int crc, int codecId, AvlDataCollection avlDataCollection) {
        return new TcpDataPacket(preamble, length, crc, codecId, avlDataCollection);
    }
}