package com.tmv.position;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.DateTimeException;
import java.util.Date;

@Slf4j
@Configuration
class LoadDatabase {
/*
    private PositionRepository repository;

    @Autowired
    LoadDatabase(PositionRepository repository) {
        this.repository = repository;
    }


    @Bean
    CommandLineRunner initDatabase(PositionRepository repository) {
            return args -> {
            log.info("Preloading " + this.repository.save(new Position(49.0F,8.0F, (short) 0,(short) 2,(byte) 10,(short) 10,"023442", new Date() )));
            log.info("Preloading " + this.repository.save(new Position(50.0F,9.10F,(short) 5,(short) 3,(byte) 8,(short) 20,"023142", new Date() )));
        };
    }
*/

}