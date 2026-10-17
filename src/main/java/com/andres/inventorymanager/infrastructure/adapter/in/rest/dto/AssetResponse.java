package com.andres.inventorymanager.infrastructure.adapter.in.rest.dto;

import com.andres.inventorymanager.domain.model.AssetStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssetResponse(
        UUID id,
        String serial,
        String name,
        String description,
        LocalDate purchaseDate,
        BigDecimal purchaseValue,
        AssetStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        DepreciationResponse depreciation
) {
}
