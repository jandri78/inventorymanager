package com.andres.inventorymanager.infrastructure.adapter.in.rest.dto;

import com.andres.inventorymanager.domain.model.AssetStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AssetResponse(
        UUID id,
        String serial,
        String name,
        String description,
        LocalDate purchaseDate,
        BigDecimal purchaseValue,
        AssetStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
