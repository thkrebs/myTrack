package com.tmv.inbound.teltonika;

import com.tmv.inbound.RequestHandler;
import com.tmv.inbound.teltonika.model.TcpDataPacket;
import jakarta.transaction.NotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.Socket;
@Slf4j
public class TcpRequestHandler implements RequestHandler {
    static final int UNKNOWN_DEVICE = (byte)0x00;
    static final int DEVICE_EXISTS  = (byte)0x01;

    private Socket clientSocket;

    @Value( "${nrOfIMEBytes}")
    private int nrOfIMEBytes;    // number of bytes representing the IMEI

    @Override
    public void run() {
        try {
            readAVLPacket(clientSocket);
        } catch (IOException e) {
            log.error("Error while reading AVL packet", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.error("Error closing socket", e);
            }
        }
    }

    public void setSocket(Socket clientSocket) { this.clientSocket = clientSocket;}

    /**
     * The data from Teltonika device is read here
     * The overview of the data goes like this
     * 1) Read IMEI
     * 2) Send IMEI read response
     * 3) Read AVL DataPacket header  : Four zeros + AVL data array length
     * 4) Read AVL Data : CodecID + Number of data + Data elements+ Number of data
     * 5) Read CRC
     * 6) Send data reception acknowledgment : 2 bytes(Number of data read)
     * @param clientSocket
     * @throws IOException
     */
    private  void readAVLPacket (Socket clientSocket) throws IOException {
        try (DataInputStream dataIn = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
             DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());) {
            log.info("Reading AVL Data Packet");

            String imei = authorizeIMEI(dataOut, dataIn);
            log.info("IMEI : {}  Authorized. Continue reading", imei);

            DataDecoder dd = new DataDecoder(dataIn);
            TcpDataPacket tp = dd.decodeTcpData();
            tp.setImei(imei);

            log.info("Reply with count of packages received {}", tp.getAvlData().getDataCount());
            dataOut.writeShort((short)tp.getAvlData().getDataCount());
        } catch (NotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    private String authorizeIMEI(DataOutputStream dataOut, DataInputStream dataIn) {
        String imei = readIMEI(dataIn);
        if(imei == null){
            log.error("Error while reading IMEI ");
        }
        else {
            try {
                log.info("Sending the IMEI read response");
                if (sendIMEIReadResponse(dataOut, imei) == DEVICE_EXISTS) {
                    // Continue with data read
                    log.debug("Device : {} exists", imei);
                } else {
                    log.debug("Device : {} doesnt exist", imei);
                }
            } catch (IOException e) {
                log.error("Error while sending IMEI Read response", e);
            }
        }
        return imei;
    }

    /**
     * Read the IMEI from the Teltonika device
     * @param dataIn
     * @return
     */
    private String readIMEI(DataInputStream dataIn)  {
        log.debug("Reading IMEI ");
        String IMEI=null;
        try {
            byte[] imeiArrSample=new byte[17];
            imeiArrSample = dataIn.readNBytes(17);
            log.debug("Read value : {}", bytesToHex(imeiArrSample,imeiArrSample.length));
            IMEI =new String(imeiArrSample,2, 15);
            log.debug("Received IMEI : {}", IMEI);
        } catch (IOException e) {
            log.error("Error while reading IMEI : ",e);
        }
        return IMEI;
    }

    private int sendIMEIReadResponse(DataOutputStream dataOut, String IMEI) throws IOException {

        byte response;
        if(IMEI.equals("866069064483413")){
             response = DEVICE_EXISTS;
        } else {
            response = UNKNOWN_DEVICE;
            log.debug("Response: {}", response);
        }
        log.debug("Response: {}", response);
        dataOut.write(response);
        return response;
    }

    private  String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }
}