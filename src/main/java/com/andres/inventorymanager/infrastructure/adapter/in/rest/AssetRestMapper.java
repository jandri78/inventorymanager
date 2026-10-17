package com.andres.inventorymanager.infrastructure.adapter.in.rest;

import com.andres.inventorymanager.application.port.in.CreateAssetCommand;
import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.infrastructure.adapter.in.rest.dto.AssetRequest;
import com.andres.inventorymanager.infrastructure.adapter.in.rest.dto.AssetResponse;
import org.springframework.stereotype.Component;

@Component
public class AssetRestMapper {

    public CreateAssetCommand toCommand(AssetRequest request) {
        return new CreateAssetCommand(
                request.serial(),
                request.name(),
                request.description(),
                request.purchaseDate(),
                request.purchaseValue());
    }

    public AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getSerial(),
                asset.getName(),
                asset.getDescription(),
                asset.getPurchaseDate(),
                asset.getPurchaseValue(),
                asset.getStatus(),
                asset.getCreatedAt(),
                asset.getUpdatedAt());
    }
}
