package com.tmv;

import com.tmv.core.dto.MapStructMapper;
import com.tmv.core.dto.MapStructMapperImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication(scanBasePackages={"com.tmv"})
public class TestingWebApplication {
/*
    @Configuration
    static class TestConfig {
        @Bean
        public MapStructMapper mapStructMapper() {
            return new MapStructMapperImpl(); // Ersatz oder Test-Implementierung
        }
    }
*/
    public static void main(String[] args) {
        SpringApplication.run(TestingWebApplication.class, args);
    }
}
