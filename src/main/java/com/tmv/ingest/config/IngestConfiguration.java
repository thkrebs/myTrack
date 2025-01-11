package com.tmv.ingest.config;

import com.tmv.core.service.ImeiValidationService;
import com.tmv.ingest.RequestHandler;
import com.tmv.ingest.TcpServer;
import com.tmv.ingest.teltonika.TcpRequestHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class IngestConfiguration {

    @Bean(name="RequestHandler")
    RequestHandler getRequestHandler(ApplicationEventPublisher publisher, ImeiValidationService imeiValidationService) {
        return new TcpRequestHandler(publisher, imeiValidationService);
    }

    @Bean(name="TcpServer")
    TcpServer getTcpServer(RequestHandler requestHandler) {
        return new TcpServer(requestHandler);
    }


}
