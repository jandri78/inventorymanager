package com.andres.inventorymanager.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DepreciationResponse(
        long yearsElapsed,
        BigDecimal annualDepreciation,
        BigDecimal accumulatedDepreciation,
        BigDecimal currentValue
) {
}
