package com.tmv.ingest.config;

import com.tmv.core.service.imei.ImeiValidationService;
import com.tmv.ingest.RequestHandler;
import com.tmv.ingest.TcpServer;
import com.tmv.ingest.teltonika.TcpRequestHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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
