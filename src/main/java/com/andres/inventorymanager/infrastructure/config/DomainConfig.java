package com.andres.inventorymanager.infrastructure.config;

import com.andres.inventorymanager.domain.service.DepreciationCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public DepreciationCalculator depreciationCalculator() {
        return new DepreciationCalculator();
    }
}
