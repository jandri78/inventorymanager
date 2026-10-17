package com.andres.inventorymanager.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssetRepository extends JpaRepository<AssetJpaEntity, UUID> {

    boolean existsBySerial(String serial);
}
