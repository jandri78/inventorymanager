package com.andres.inventorymanager.domain.service;

import com.andres.inventorymanager.domain.model.DepreciationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DepreciationCalculatorTest {

    private static final BigDecimal RATE = new BigDecimal("0.04");
    private final DepreciationCalculator calculator = new DepreciationCalculator();

    @Test
    void returnsFullValue_whenNoYearsHaveElapsed() {
        DepreciationResult result = calculator.calculate(
                new BigDecimal("1000000.00"), LocalDate.of(2024, 6, 1), RATE, LocalDate.of(2024, 12, 31));

        assertThat(result.yearsElapsed()).isZero();
        assertThat(result.annualDepreciation()).isEqualByComparingTo("40000.00");
        assertThat(result.accumulatedDepreciation()).isEqualByComparingTo("0.00");
        assertThat(result.currentValue()).isEqualByComparingTo("1000000.00");
    }

    @Test
    void computesLinearDepreciation_overSeveralYears() {
        DepreciationResult result = calculator.calculate(
                new BigDecimal("1000000.00"), LocalDate.of(2021, 1, 15), RATE, LocalDate.of(2024, 1, 15));

        assertThat(result.yearsElapsed()).isEqualTo(3);
        assertThat(result.annualDepreciation()).isEqualByComparingTo("40000.00");
        assertThat(result.accumulatedDepreciation()).isEqualByComparingTo("120000.00");
        assertThat(result.currentValue()).isEqualByComparingTo("880000.00");
    }

    @Test
    void capsCurrentValueAtZero_whenAccumulatedDepreciationExceedsPurchaseValue() {
        DepreciationResult result = calculator.calculate(
                new BigDecimal("1000000.00"), LocalDate.of(1990, 1, 1), RATE, LocalDate.of(2024, 1, 1));

        assertThat(result.yearsElapsed()).isEqualTo(34);
        assertThat(result.accumulatedDepreciation()).isEqualByComparingTo("1000000.00");
        assertThat(result.currentValue()).isEqualByComparingTo("0.00");
    }

    @Test
    void treatsFuturePurchaseDateAsZeroYearsElapsed() {
        DepreciationResult result = calculator.calculate(
                new BigDecimal("500000.00"), LocalDate.of(2030, 1, 1), RATE, LocalDate.of(2024, 1, 1));

        assertThat(result.yearsElapsed()).isZero();
        assertThat(result.currentValue()).isEqualByComparingTo("500000.00");
    }

    @Test
    void roundsMonetaryValuesToTwoDecimals() {
        DepreciationResult result = calculator.calculate(
                new BigDecimal("333333.33"), LocalDate.of(2023, 1, 1), RATE, LocalDate.of(2024, 1, 1));

        assertThat(result.annualDepreciation()).isEqualByComparingTo("13333.33");
        assertThat(result.currentValue()).isEqualByComparingTo("320000.00");
    }
}
