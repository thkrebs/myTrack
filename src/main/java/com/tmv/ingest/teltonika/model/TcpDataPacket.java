package com.tmv.ingest.teltonika.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class TcpDataPacket {
    private final int preamble;
    private final int length;
    private final int crc;
    private final AvlDataCollection avlData;
    private final int codecId;
    @Setter
    private String imei;

    private TcpDataPacket(int preamble, int length, int crc, int codecId, AvlDataCollection avlDataCollection) {
        this.preamble = preamble;
        this.length = length;
        this.crc = crc;
        this.codecId = codecId;
        this.avlData = avlDataCollection;
    }
    public static TcpDataPacket create(int preamble, int length, int crc, int codecId, AvlDataCollection avlDataCollection) {
        return new TcpDataPacket(preamble, length, crc, codecId, avlDataCollection);
    }
}