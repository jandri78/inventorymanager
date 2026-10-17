package com.andres.inventorymanager.application.port.out;

import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.domain.model.AssetStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepositoryPort {

    Asset save(Asset asset);

    Optional<Asset> findById(UUID id);

    List<Asset> findAll();

    List<Asset> findByStatus(AssetStatus status);

    boolean existsBySerial(String serial);
}
