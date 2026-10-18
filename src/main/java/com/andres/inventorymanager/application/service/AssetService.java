package com.andres.inventorymanager.application.service;

import com.andres.inventorymanager.application.port.in.AssetDetail;
import com.andres.inventorymanager.application.port.in.CreateAssetCommand;
import com.andres.inventorymanager.application.port.in.CreateAssetUseCase;
import com.andres.inventorymanager.application.port.in.GetAssetUseCase;
import com.andres.inventorymanager.application.port.in.ListAssetsUseCase;
import com.andres.inventorymanager.application.port.in.UpdateAssetCommand;
import com.andres.inventorymanager.application.port.in.UpdateAssetUseCase;
import com.andres.inventorymanager.application.port.out.AssetRepositoryPort;
import com.andres.inventorymanager.application.port.out.DepreciationRateProvider;
import com.andres.inventorymanager.domain.exception.AssetNotFoundException;
import com.andres.inventorymanager.domain.exception.DuplicateSerialException;
import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.domain.model.AssetStatus;
import com.andres.inventorymanager.domain.model.DepreciationResult;
import com.andres.inventorymanager.domain.service.DepreciationCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AssetService implements CreateAssetUseCase, GetAssetUseCase, ListAssetsUseCase, UpdateAssetUseCase {

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
        Asset asset = findAssetOrThrow(id);
        return new AssetDetail(asset, calculateDepreciation(asset));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetDetail> listAssets(AssetStatus status) {
        List<Asset> assets = (status == null)
                ? assetRepositoryPort.findAll()
                : assetRepositoryPort.findByStatus(status);

        return assets.stream()
                .map(asset -> new AssetDetail(asset, calculateDepreciation(asset)))
                .toList();
    }

    @Override
    public AssetDetail updateAsset(UUID id, UpdateAssetCommand command) {
        Asset asset = findAssetOrThrow(id);

        boolean serialChanged = !asset.getSerial().equals(command.serial());
        if (serialChanged && assetRepositoryPort.existsBySerial(command.serial())) {
            throw new DuplicateSerialException(command.serial());
        }

        asset.updateDetails(
                command.serial(),
                command.name(),
                command.description(),
                command.purchaseDate(),
                command.purchaseValue());

        Asset updated = assetRepositoryPort.save(asset);
        return new AssetDetail(updated, calculateDepreciation(updated));
    }

    private Asset findAssetOrThrow(UUID id) {
        return assetRepositoryPort.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));
    }

    private DepreciationResult calculateDepreciation(Asset asset) {
        BigDecimal annualRate = depreciationRateProvider.getAnnualRate();
        return depreciationCalculator.calculate(
                asset.getPurchaseValue(),
                asset.getPurchaseDate(),
                annualRate,
                LocalDate.now());
    }
}
