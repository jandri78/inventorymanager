package com.andres.inventorymanager.infrastructure.config;

import com.andres.inventorymanager.application.port.out.DepreciationRateProvider;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Component
@Validated
@ConfigurationProperties(prefix = "app.depreciation")
public class DepreciationProperties implements DepreciationRateProvider {

    @NotNull(message = "app.depreciation.rate must be configured")
    @DecimalMin(value = "0.0", message = "app.depreciation.rate must be greater than or equal to 0")
    @DecimalMax(value = "1.0", message = "app.depreciation.rate must be less than or equal to 1")
    private BigDecimal rate;

    @Override
    public BigDecimal getAnnualRate() {
        return rate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
