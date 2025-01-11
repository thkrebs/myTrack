package com.tmv.core.config;

import com.tmv.core.util.ImeiUrlGuardian;
import com.tmv.core.util.LoggerInterceptor;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@org.springframework.context.annotation.Configuration
public class CoreConfiguration implements WebMvcConfigurer {

    public final static int SRID = 4326;

    @Autowired
    ImeiUrlGuardian imeiUrlGuardian;

    // needed otherwise serialization of geometric data will not work
    @Bean
    public JtsModule jtsModule() {return new JtsModule();}

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new LoggerInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(imeiUrlGuardian).addPathPatterns("/api/v1/**");
    }

}
