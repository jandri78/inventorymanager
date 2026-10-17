package com.andres.inventorymanager.infrastructure.adapter.in.rest;

import com.andres.inventorymanager.application.port.in.CreateAssetUseCase;
import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.infrastructure.adapter.in.rest.dto.AssetRequest;
import com.andres.inventorymanager.infrastructure.adapter.in.rest.dto.AssetResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final CreateAssetUseCase createAssetUseCase;
    private final AssetRestMapper assetRestMapper;

    public AssetController(CreateAssetUseCase createAssetUseCase, AssetRestMapper assetRestMapper) {
        this.createAssetUseCase = createAssetUseCase;
        this.assetRestMapper = assetRestMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse create(@Valid @RequestBody AssetRequest request) {
        Asset asset = createAssetUseCase.createAsset(assetRestMapper.toCommand(request));
        return assetRestMapper.toResponse(asset);
    }
}
