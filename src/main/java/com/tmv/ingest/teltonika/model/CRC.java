package com.tmv.ingest.teltonika.model;

public final class CRC {
    private final int polynom;

    public static final CRC DEFAULT = new CRC(0xA001);

    public CRC(int polynom) {
        this.polynom = polynom;
    }

    public int calcCrc16(byte[] buffer) {
        return calcCrc16(buffer, 0, buffer.length, polynom, 0);
    }

    public static int calcCrc16(byte[] buffer, int offset, int bufLen, int polynom, int preset) {
        preset &= 0xFFFF;
        polynom &= 0xFFFF;

        int crc = preset;
        for (int i = 0; i < bufLen; i++) {
            int data = buffer[(i + offset) % buffer.length] & 0xFF;
            crc ^= data;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >> 1) ^ polynom;
                } else {
                    crc = crc >> 1;
                }
            }
        }
        return crc & 0xFFFF;
    }
}