package com.andres.inventorymanager.domain.model;

import java.math.BigDecimal;

public record DepreciationResult(
        long yearsElapsed,
        BigDecimal annualDepreciation,
        BigDecimal accumulatedDepreciation,
        BigDecimal currentValue
) {
}
