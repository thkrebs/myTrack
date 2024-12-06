package com.tmv.ingest.teltonika;

import com.tmv.ingest.teltonika.model.*;
import jakarta.transaction.NotSupportedException;
import java.io.DataInputStream;
import java.io.IOException;


public class DataDecoder {
    private final DataInputStream reader;

    public DataDecoder(DataInputStream reader) {
        if (reader == null) {
            throw new IllegalArgumentException("reader cannot be null");
        }
        this.reader = reader;
    }

    /**
     * Decode AVL Tcp data packet.
     *
     * @return the decoded TcpDataPacket
     * @throws IOException if an I/O error occurs
     */
    public TcpDataPacket decodeTcpData() throws IOException, NotSupportedException {
        int preamble = reader.readInt();
        int length = reader.readInt();
        reader.mark(length+8);
        byte[] data = reader.readNBytes(length);
        int crc = reader.readInt();
        reader.reset();

        if (preamble != 0) {
            throw new UnsupportedOperationException("Unable to decode. Missing package prefix.");
        }

        if (crc != CRC.DEFAULT.calcCrc16(data)) {
            throw new IllegalStateException("CRC does not match the expected.");
        }
        int codecId = reader.readUnsignedByte();

        AvlDataCollection avlDataCollection = null;

        switch (codecId) {
            case 0x8E:
                avlDataCollection = new Codec8E(reader).decodeAvlDataCollection();
                break;
            default:
                throw new NotSupportedException("Codec not supported: " + codecId);
        }
        return TcpDataPacket.create(preamble, length, crc, codecId, avlDataCollection);
    }
}