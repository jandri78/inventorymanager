package com.andres.inventorymanager.domain.service;

import com.andres.inventorymanager.domain.model.DepreciationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DepreciationCalculator {

    private static final int MONEY_SCALE = 2;

    public DepreciationResult calculate(BigDecimal purchaseValue,
                                        LocalDate purchaseDate,
                                        BigDecimal annualRate,
                                        LocalDate referenceDate) {
        long years = ChronoUnit.YEARS.between(purchaseDate, referenceDate);
        if (years < 0) {
            years = 0;
        }

        BigDecimal annualDepreciation = purchaseValue
                .multiply(annualRate)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal accumulatedDepreciation = annualDepreciation
                .multiply(BigDecimal.valueOf(years))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        if (accumulatedDepreciation.compareTo(purchaseValue) > 0) {
            accumulatedDepreciation = purchaseValue.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal currentValue = purchaseValue
                .subtract(accumulatedDepreciation)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        if (currentValue.signum() < 0) {
            currentValue = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }

        return new DepreciationResult(years, annualDepreciation, accumulatedDepreciation, currentValue);
    }
}
