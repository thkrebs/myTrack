package com.tmv.inbound.teltonika;

import com.tmv.inbound.NewTcpDataPacketEvent;
import com.tmv.inbound.RequestHandler;
import com.tmv.inbound.teltonika.model.TcpDataPacket;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@SpringBootTest
@Component
//@TestPropertySource(locations="application-test.properties")
public class TcpRequestHandlerTest {

    @Mock
    private Socket socket;

    @Autowired
    RequestHandler requestHandler;

    private final String VALID_IMEI  ="000F383636303639303634343833343133";
    private final String INVALID_IMEI="000F383636303639303634343833343134";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // tried to have it on instance level, but the event listener did not work, for whatever reason
    public static TcpDataPacket tcpDataPacketRvc;

    @BeforeEach
    public void prepareTests() {
        TcpRequestHandlerTest.tcpDataPacketRvc = null;  // to be reset before each test
    }

    @Test
    public void testDecodeTcpData_ValidData() throws IOException {

        // Prepare inputstream
        File file = ResourceUtils.getFile("classpath:tektonika_sample_input.bin");
        InputStream in = new FileInputStream(file);

        // Prepare outputstream
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);

        //requestHandler = spy(requestHandler);
        requestHandler.setSocket(socket);
        requestHandler.run();

        byte[] responseBytes = out.toByteArray();
        assertEquals(1, responseBytes[0], "Response for valid IMEI should be one");
        assertEquals(19, byte2Short(responseBytes[1],responseBytes[2]), "handler should send the number of received data packets");
        assertNotNull(tcpDataPacketRvc, "packet should have been received by listener");
        assertEquals(19,tcpDataPacketRvc.getAvlData().getDataCount(),"test data has 19 data packets");
    }

    @Test
    public void testDecodeTcpData_UnknownIMEI()  throws IOException {
        // Prepare inputstream
        InputStream in = createInputStream(INVALID_IMEI);

        // Prepare outputstream
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);

        //requestHandler = spy(requestHandler);
        requestHandler.setSocket(socket);
        requestHandler.run();

        byte[] responseBytes = out.toByteArray();
        assertEquals(0, responseBytes[0], "Response for invalid IMEI should be zero");
        assertNull(tcpDataPacketRvc, "there should be no packet received by listener");
    }

    @Test
    public void testDecodeTcpData_InvalidPreamble() throws IOException {
        // Prepare inputstream
        String hexString = VALID_IMEI + /* preamble */ "1000" + /* some trailing bytes */ "00000000000000000000000000000000";  // preamble has to be 0
        InputStream in = createInputStream(hexString);

        // Prepare outputstream
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            when(socket.getInputStream()).thenReturn(in);
            when(socket.getOutputStream()).thenReturn(out);

            requestHandler.setSocket(socket);
            requestHandler.run();
        });

        String expectedMessage = "Unable to decode. Missing package prefix.";
        String actualMessage = exception.getMessage();

        byte[] responseBytes = out.toByteArray();
        assertEquals(1, responseBytes[0], "Response for valid IMEI should be 1");
        assertNull(tcpDataPacketRvc, "there should be no packet received by listener");
    }

    private InputStream createInputStream(String hexString) {
        byte[] byteStream = hexStringToByteArray(hexString);
        return new DataInputStream(new ByteArrayInputStream(byteStream));
    }

    private short byte2Short(byte firstByte, byte secondByte) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.put(firstByte);
        bb.put(secondByte);
        return bb.getShort(0);
    }

    public static byte[] hexStringToByteArray(String hex) {
        int length = hex.length();
        byte[] byteArray = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            String hexPair = hex.substring(i, i + 2);
            byteArray[i / 2] = (byte) Integer.parseUnsignedInt(hexPair, 16);
        }
        return byteArray;
    }

    @EventListener
    public void handle(NewTcpDataPacketEvent event) {
        TcpRequestHandlerTest.tcpDataPacketRvc = event.getDataPacket();
    }
}