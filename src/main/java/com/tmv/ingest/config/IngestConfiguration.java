package com.tmv.ingest.config;

import com.tmv.ingest.RequestHandler;
import com.tmv.ingest.TcpServer;
import com.tmv.ingest.teltonika.TcpRequestHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@org.springframework.context.annotation.Configuration
public class IngestConfiguration {

    @Bean(name="RequestHandler")
    RequestHandler getRequestHandler(ApplicationEventPublisher publisher) {
        return new TcpRequestHandler(publisher);
    }

    @Bean(name="TcpServer")
    TcpServer getTcpServer(RequestHandler requestHandler) {
        return new TcpServer(requestHandler);
    }


}
