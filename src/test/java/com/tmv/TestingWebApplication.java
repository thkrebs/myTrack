package com.tmv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"com.tmv"})
public class TestingWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestingWebApplication.class, args);
    }
}
