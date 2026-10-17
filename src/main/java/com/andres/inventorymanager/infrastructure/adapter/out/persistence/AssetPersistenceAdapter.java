package com.andres.inventorymanager.infrastructure.adapter.out.persistence;

import com.andres.inventorymanager.application.port.out.AssetRepositoryPort;
import com.andres.inventorymanager.domain.model.Asset;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AssetPersistenceAdapter implements AssetRepositoryPort {

    private final AssetRepository repository;

    public AssetPersistenceAdapter(AssetRepository repository) {
        this.repository = repository;
    }

    @Override
    public Asset save(Asset asset) {
        AssetJpaEntity saved = repository.save(AssetPersistenceMapper.toEntity(asset));
        return AssetPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Asset> findById(UUID id) {
        return repository.findById(id).map(AssetPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsBySerial(String serial) {
        return repository.existsBySerial(serial);
    }
}
