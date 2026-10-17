package com.andres.inventorymanager.infrastructure.adapter.out.persistence;

import com.andres.inventorymanager.domain.model.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<AssetJpaEntity, UUID> {

    boolean existsBySerial(String serial);

    List<AssetJpaEntity> findByStatus(AssetStatus status);
}
