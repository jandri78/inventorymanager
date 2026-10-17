package com.andres.inventorymanager.infrastructure.adapter.out.persistence;

import com.andres.inventorymanager.domain.model.Asset;

final class AssetPersistenceMapper {

    private AssetPersistenceMapper() {
    }

    static AssetJpaEntity toEntity(Asset asset) {
        return new AssetJpaEntity(
                asset.getId(),
                asset.getSerial(),
                asset.getName(),
                asset.getDescription(),
                asset.getPurchaseDate(),
                asset.getPurchaseValue(),
                asset.getStatus(),
                asset.getCreatedAt(),
                asset.getUpdatedAt());
    }

    static Asset toDomain(AssetJpaEntity entity) {
        return Asset.restore(
                entity.getId(),
                entity.getSerial(),
                entity.getName(),
                entity.getDescription(),
                entity.getPurchaseDate(),
                entity.getPurchaseValue(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
