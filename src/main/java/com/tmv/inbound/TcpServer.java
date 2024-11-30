package com.tmv.inbound;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
@Component
public class TcpServer implements Runnable {
    //private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);

    @Setter private int port;

    @Autowired
    RequestHandler requestHandler;


    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Server started on port {}", port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.info("New client connected: {}", clientSocket.getRemoteSocketAddress());

                // Start a new thread to handle the client connection
                requestHandler.setSocket(clientSocket);
                new Thread(requestHandler).start();
            }
        } catch (IOException e) {
            log.error("Error starting server on port {}",port, e);
        }
    }
}