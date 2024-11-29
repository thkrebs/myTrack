package com.tmv.inbound;

import com.tmv.inbound.teltonika.TcpRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcpServerConfig {

    @Bean(name="TcpRequestHandler")
    RequestHandler getRequestHandler() {
        return new TcpRequestHandler();
    }

    @Bean
    TcpServer getTcpServer() {
        return new TcpServer();
    }

}
