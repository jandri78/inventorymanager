package com.andres.inventorymanager.application.service;

import com.andres.inventorymanager.application.port.in.CreateAssetCommand;
import com.andres.inventorymanager.application.port.in.CreateAssetUseCase;
import com.andres.inventorymanager.application.port.out.AssetRepositoryPort;
import com.andres.inventorymanager.domain.exception.DuplicateSerialException;
import com.andres.inventorymanager.domain.model.Asset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssetService implements CreateAssetUseCase {

    private final AssetRepositoryPort assetRepositoryPort;

    public AssetService(AssetRepositoryPort assetRepositoryPort) {
        this.assetRepositoryPort = assetRepositoryPort;
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
}
