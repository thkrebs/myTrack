package com.tmv.inbound.teltonika;

import com.tmv.inbound.RequestHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@SpringBootTest
//@TestPropertySource(locations="application-test.properties")
public class TcpRequestHandlerTest {

    @Mock
    private Socket socket;

    @Autowired
    RequestHandler requestHandler;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
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
    }

    @Test
    public void testDecodeTcpData_UnknownIMEI()  throws IOException {
        // Prepare inputstream
        String hexString = "000F383636303639303634343833343134";  // use unknown IMEI
        byte[] byteStream = hexStringToByteArray(hexString);
        InputStream in = new DataInputStream(new ByteArrayInputStream(byteStream));

        // Prepare outputstream
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);

        //requestHandler = spy(requestHandler);
        requestHandler.setSocket(socket);
        requestHandler.run();

        byte[] responseBytes = out.toByteArray();
        assertEquals(0, responseBytes[0], "Response for invalid IMEI should be zero");

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
            // Nehmen Sie zwei Zeichen (einen Byte in Hexadezimaldarstellung)
            String hexPair = hex.substring(i, i + 2);
            // Konvertieren Sie den Hexadezimalwert in ein Byte
            byteArray[i / 2] = (byte) Integer.parseUnsignedInt(hexPair, 16);
        }
        return byteArray;
    }
}