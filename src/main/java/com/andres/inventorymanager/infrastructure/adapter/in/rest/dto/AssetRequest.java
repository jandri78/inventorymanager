package com.andres.inventorymanager.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetRequest(

        @NotBlank(message = "serial is required")
        @Size(max = 100, message = "serial must not exceed 100 characters")
        String serial,

        @NotBlank(message = "name is required")
        @Size(max = 200, message = "name must not exceed 200 characters")
        String name,

        @Size(max = 1000, message = "description must not exceed 1000 characters")
        String description,

        @NotNull(message = "purchaseDate is required")
        @PastOrPresent(message = "purchaseDate cannot be in the future")
        LocalDate purchaseDate,

        @NotNull(message = "purchaseValue is required")
        @Positive(message = "purchaseValue must be greater than zero")
        BigDecimal purchaseValue
) {
}
