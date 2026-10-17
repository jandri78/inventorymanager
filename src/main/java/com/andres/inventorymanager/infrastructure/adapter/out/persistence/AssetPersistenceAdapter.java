package com.andres.inventorymanager.infrastructure.adapter.out.persistence;

import com.andres.inventorymanager.application.port.out.AssetRepositoryPort;
import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.domain.model.AssetStatus;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public List<Asset> findAll() {
        return repository.findAll().stream()
                .map(AssetPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Asset> findByStatus(AssetStatus status) {
        return repository.findByStatus(status).stream()
                .map(AssetPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySerial(String serial) {
        return repository.existsBySerial(serial);
    }
}
