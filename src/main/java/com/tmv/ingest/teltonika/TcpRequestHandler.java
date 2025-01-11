package com.tmv.ingest.teltonika;

import com.tmv.core.service.ImeiValidationService;
import com.tmv.ingest.NewTcpDataPacketEvent;
import com.tmv.ingest.RequestHandler;
import com.tmv.ingest.teltonika.model.TcpDataPacket;
import jakarta.transaction.NotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Not;
import org.springframework.context.ApplicationEventPublisher;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

@Slf4j
//@Component
public class TcpRequestHandler implements RequestHandler {
    static final int UNKNOWN_DEVICE = (byte) 0x00;
    static final int DEVICE_EXISTS = (byte) 0x01;

    private Socket clientSocket;

    // number of bytes representing the IMEI
    private final int nrOfIMEBytes = 17;

    private final ApplicationEventPublisher publisher;
    public final ImeiValidationService imeiValidationService;

    public TcpRequestHandler(ApplicationEventPublisher publisher, ImeiValidationService imeiValidationService) {
        this.publisher = publisher;
        this.imeiValidationService = imeiValidationService;
    }

    @Override
    public void run() {
        try (DataInputStream dataIn = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
             DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream())) {
            String imei = authorizeIMEI(dataOut, dataIn);
            if (imei != null) {
                log.info("IMEI : {}  Authorized. Continue reading", imei);
                TcpDataPacket tp = readAVLPacket(imei, dataIn, dataOut);
                if (tp != null) {
                    publishTcpDataPacket(tp);
                }
                Thread.sleep(100);  // introduced for testing purposes wait a moment before closing socket
            } else {
                log.error("Presented IMEI not active");
            }
        } catch (NotSupportedException e) {
            log.error("Codec not supported", e);
        } catch (IOException e) {
            log.error("Error while reading AVL packet", e);
        } catch (InterruptedException e) {
            log.error("Error while sleeping before closing socket", e);
        } finally {
            try {
                log.info("Closing socket");
                clientSocket.close();
            } catch (IOException e) {
                log.error("Error closing socket", e);
            }
        }
    }

    public void setSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * The data from Teltonika device is read here
     * The overview of the data goes like this
     * 1) Read IMEI
     * 2) Send IMEI read response
     * 3) Read AVL DataPacket header  : Four zeros + AVL data array length
     * 4) Read AVL Data : CodecID + Number of data + Data elements+ Number of data
     * 5) Read CRC
     * 6) Send data reception acknowledgment : 2 bytes(Number of data read)
     */
    private TcpDataPacket readAVLPacket(String imei, DataInputStream dataIn, DataOutputStream dataOut) throws IOException, NotSupportedException {
        log.info("Reading AVL Data Packet");

        DataDecoder dd = new DataDecoder(dataIn);
        TcpDataPacket tp = dd.decodeTcpData();
        if (tp != null) {
            tp.setImei(imei);
            log.info("Reply with count of packages received {}", tp.getAvlData().getDataCount());
            dataOut.writeInt(tp.getAvlData().getDataCount());
        }
        return tp;
    }

    private void publishTcpDataPacket(TcpDataPacket tp) {
        log.debug("Publishing tcpDataPacket event");
        this.publisher.publishEvent(new NewTcpDataPacketEvent(this, tp));
    }

    private String authorizeIMEI(DataOutputStream dataOut, DataInputStream dataIn) {
        String imei = readIMEI(dataIn);
        if (imei == null) {
            log.error("Error while reading IMEI ");
        } else {
            try {
                log.info("Sending the IMEI read response");
                if (sendIMEIReadResponse(dataOut, imei) == DEVICE_EXISTS) {
                    // Continue with data read
                    log.debug("Device : {} exists", imei);
                } else {
                    log.debug("Device : {} doesnt exist", imei);
                    imei = null;
                }
            } catch (IOException e) {
                log.error("Error while sending IMEI Read response", e);
            }
        }
        return imei;
    }

    /**
     * Read the IMEI from the Teltonika device
     *
     * @param dataIn
     * @return
     */
    private String readIMEI(DataInputStream dataIn) {
        log.debug("Reading IMEI ");
        String IMEI = null;
        try {
            byte[] imeiArrSample;
            imeiArrSample = dataIn.readNBytes(nrOfIMEBytes);
            log.debug("Read value : {}", bytesToHex(imeiArrSample, imeiArrSample.length));
            IMEI = new String(imeiArrSample, 2, nrOfIMEBytes - 2);
            log.debug("Received IMEI : {}", IMEI);
        } catch (IOException e) {
            log.error("Error while reading IMEI : ", e);
        }
        return IMEI;
    }

    private int sendIMEIReadResponse(DataOutputStream dataOut, String imei) throws IOException {
        byte response;
        if (imeiValidationService.isActive(imei)) {
            response = DEVICE_EXISTS;
        } else {
            response = UNKNOWN_DEVICE;
            log.debug("Response: {}", response);
        }
        log.debug("Response: {}", response);
        dataOut.write(response);
        return response;
    }

    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }

    // helper to read all bytes until end of stream
    private byte[] readUntilEnd(DataInputStream reader) throws IOException {
        try (java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream()) {
            byte[] temp = new byte[1024];
            int bytesRead;

            while ((bytesRead = reader.read(temp)) != -1) {
                buffer.write(temp, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }
}