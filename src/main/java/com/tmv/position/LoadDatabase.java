package com.tmv.position;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Date;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(PositionRepository repository) {

        return args -> {
            log.info("Preloading " + repository.save(new Position("first position", new Date() )));
            log.info("Preloading " + repository.save(new Position("second position", new Date())));
        };
    }
}