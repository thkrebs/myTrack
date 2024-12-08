package com.tmv.core.config;


import com.tmv.core.controller.position.PositionDataEventListener;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class CoreConfiguration {

    @Bean(name="PositionDataEventListener")
    PositionDataEventListener getPositionDataEventListener() {
        return new PositionDataEventListener();
    }

}
