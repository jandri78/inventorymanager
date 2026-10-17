package com.andres.inventorymanager.application.service;

import com.andres.inventorymanager.application.port.in.AssetDetail;
import com.andres.inventorymanager.application.port.in.CreateAssetCommand;
import com.andres.inventorymanager.application.port.in.CreateAssetUseCase;
import com.andres.inventorymanager.application.port.in.GetAssetUseCase;
import com.andres.inventorymanager.application.port.out.AssetRepositoryPort;
import com.andres.inventorymanager.application.port.out.DepreciationRateProvider;
import com.andres.inventorymanager.domain.exception.AssetNotFoundException;
import com.andres.inventorymanager.domain.exception.DuplicateSerialException;
import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.domain.model.DepreciationResult;
import com.andres.inventorymanager.domain.service.DepreciationCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class AssetService implements CreateAssetUseCase, GetAssetUseCase {

    private final AssetRepositoryPort assetRepositoryPort;
    private final DepreciationCalculator depreciationCalculator;
    private final DepreciationRateProvider depreciationRateProvider;

    public AssetService(AssetRepositoryPort assetRepositoryPort,
                        DepreciationCalculator depreciationCalculator,
                        DepreciationRateProvider depreciationRateProvider) {
        this.assetRepositoryPort = assetRepositoryPort;
        this.depreciationCalculator = depreciationCalculator;
        this.depreciationRateProvider = depreciationRateProvider;
    }

    @Override
    public Asset createAsset(CreateAssetCommand command) {
        if (assetRepositoryPort.existsBySerial(command.serial())) {
            throw new DuplicateSerialException(command.serial());
        }
        Asset asset = Asset.create(
                command.serial(),
                command.name(),
                command.description(),
                command.purchaseDate(),
                command.purchaseValue());
        return assetRepositoryPort.save(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetDetail getAsset(UUID id) {
        Asset asset = assetRepositoryPort.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));

        DepreciationResult depreciation = depreciationCalculator.calculate(
                asset.getPurchaseValue(),
                asset.getPurchaseDate(),
                depreciationRateProvider.getAnnualRate(),
                LocalDate.now());

        return new AssetDetail(asset, depreciation);
    }
}
