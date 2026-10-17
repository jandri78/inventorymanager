package com.andres.inventorymanager.infrastructure.adapter.in.rest;

import com.andres.inventorymanager.application.port.in.AssetDetail;
import com.andres.inventorymanager.application.port.in.CreateAssetCommand;
import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.domain.model.DepreciationResult;
import com.andres.inventorymanager.infrastructure.adapter.in.rest.dto.AssetRequest;
import com.andres.inventorymanager.infrastructure.adapter.in.rest.dto.AssetResponse;
import com.andres.inventorymanager.infrastructure.adapter.in.rest.dto.DepreciationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

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
                asset.getUpdatedAt(),
                null);
    }

    public AssetResponse toResponse(AssetDetail detail) {
        Asset asset = detail.asset();
        DepreciationResult depreciation = detail.depreciation();

        return new AssetResponse(
                asset.getId(),
                asset.getSerial(),
                asset.getName(),
                asset.getDescription(),
                asset.getPurchaseDate(),
                asset.getPurchaseValue(),
                asset.getStatus(),
                asset.getCreatedAt(),
                asset.getUpdatedAt(),
                new DepreciationResponse(
                        depreciation.yearsElapsed(),
                        depreciation.annualDepreciation(),
                        depreciation.accumulatedDepreciation(),
                        depreciation.currentValue()));
    }

    public List<AssetResponse> toResponseList(List<AssetDetail> details) {
        return details.stream()
                .map(this::toResponse)
                .toList();
    }
}
