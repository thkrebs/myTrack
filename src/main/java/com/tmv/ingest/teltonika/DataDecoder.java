package com.tmv.ingest.teltonika;

import com.tmv.ingest.teltonika.model.AvlDataCollection;
import com.tmv.ingest.teltonika.model.CRC;
import com.tmv.ingest.teltonika.model.TcpDataPacket;
import jakarta.transaction.NotSupportedException;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.IOException;

@Slf4j
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
        // check whether first byte indicates a ping
        reader.mark(2);
        int firstByte = reader.readUnsignedByte();
        if (firstByte == 0xFF) // ping
        {
            log.info("Received PING: {}", firstByte); // + preamble and crc
            return null;
        }
        reader.reset();

        int preamble = reader.readInt();
        int length = reader.readInt();
        log.debug("Received AVL data packet. Length: {}", length);
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

    // helper to read all bytes until end of stream
    private byte[] readUntilEnd(DataInputStream reader) throws IOException {
        try (java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream()) {
            byte[] temp = new byte[1024];
            int bytesRead;
            bytesRead = reader.read(temp);
            log.debug("Read {} bytes from tail", bytesRead);
            buffer.write(temp, 0, bytesRead);
            return buffer.toByteArray();
        }
    }
}