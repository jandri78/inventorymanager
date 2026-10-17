package com.andres.inventorymanager.application.port.out;

import com.andres.inventorymanager.domain.model.Asset;

import java.util.Optional;
import java.util.UUID;

public interface AssetRepositoryPort {

    Asset save(Asset asset);

    Optional<Asset> findById(UUID id);

    boolean existsBySerial(String serial);
}
