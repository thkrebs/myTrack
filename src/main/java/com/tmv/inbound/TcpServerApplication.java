package com.tmv.inbound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;


@EnableAsync
@SpringBootApplication(scanBasePackages={"com.tmv"})
@ComponentScan(basePackages = "com.tmv")
@Component
public class TcpServerApplication implements CommandLineRunner {

    @Value("${port}")
    private int port;

    @Autowired
    private final TcpServer tcpServer;
    public TcpServerApplication(TcpServer server) {
        this.tcpServer = server;
    }

    public static void main(String[] args) {
        SpringApplication.run(TcpServerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        tcpServer.setPort(port);
        new Thread(tcpServer).start();
    }
}