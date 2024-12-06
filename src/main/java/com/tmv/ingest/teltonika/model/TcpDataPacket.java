package com.tmv.ingest.teltonika.model;

import lombok.Getter;
import lombok.Setter;

public class TcpDataPacket {
    @Getter private final int preamble;
    @Getter private final int length;
    @Getter private final int crc;
    @Getter private final AvlDataCollection avlData;
    @Getter private final int codecId;
    @Getter @Setter
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