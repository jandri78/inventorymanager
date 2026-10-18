package com.andres.inventorymanager.infrastructure.adapter.in.rest;

import com.andres.inventorymanager.application.port.in.CreateAssetUseCase;
import com.andres.inventorymanager.application.port.in.GetAssetUseCase;
import com.andres.inventorymanager.application.port.in.ListAssetsUseCase;
import com.andres.inventorymanager.application.port.in.UpdateAssetUseCase;
import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.domain.model.AssetStatus;
import com.andres.inventorymanager.infrastructure.adapter.in.rest.dto.AssetRequest;
import com.andres.inventorymanager.infrastructure.adapter.in.rest.dto.AssetResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final CreateAssetUseCase createAssetUseCase;
    private final GetAssetUseCase getAssetUseCase;
    private final ListAssetsUseCase listAssetsUseCase;
    private final UpdateAssetUseCase updateAssetUseCase;
    private final AssetRestMapper assetRestMapper;

    public AssetController(CreateAssetUseCase createAssetUseCase,
                           GetAssetUseCase getAssetUseCase,
                           ListAssetsUseCase listAssetsUseCase,
                           UpdateAssetUseCase updateAssetUseCase,
                           AssetRestMapper assetRestMapper) {
        this.createAssetUseCase = createAssetUseCase;
        this.getAssetUseCase = getAssetUseCase;
        this.listAssetsUseCase = listAssetsUseCase;
        this.updateAssetUseCase = updateAssetUseCase;
        this.assetRestMapper = assetRestMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse create(@Valid @RequestBody AssetRequest request) {
        Asset asset = createAssetUseCase.createAsset(assetRestMapper.toCommand(request));
        return assetRestMapper.toResponse(asset);
    }

    @GetMapping
    public List<AssetResponse> list(@RequestParam(required = false) AssetStatus status) {
        return assetRestMapper.toResponseList(listAssetsUseCase.listAssets(status));
    }

    @GetMapping("/{id}")
    public AssetResponse getById(@PathVariable UUID id) {
        return assetRestMapper.toResponse(getAssetUseCase.getAsset(id));
    }

    @PutMapping("/{id}")
    public AssetResponse update(@PathVariable UUID id, @Valid @RequestBody AssetRequest request) {
        return assetRestMapper.toResponse(
                updateAssetUseCase.updateAsset(id, assetRestMapper.toUpdateCommand(request)));
    }
}
