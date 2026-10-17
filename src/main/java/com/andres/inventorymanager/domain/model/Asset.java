package com.andres.inventorymanager.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Asset {

    private final UUID id;
    private String serial;
    private String name;
    private String description;
    private LocalDate purchaseDate;
    private BigDecimal purchaseValue;
    private AssetStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Asset(UUID id, String serial, String name, String description,
                  LocalDate purchaseDate, BigDecimal purchaseValue,
                  AssetStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.serial = serial;
        this.name = name;
        this.description = description;
        this.purchaseDate = purchaseDate;
        this.purchaseValue = purchaseValue;
        this.status = status != null ? status : AssetStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Asset create(String serial, String name, String description,
                               LocalDate purchaseDate, BigDecimal purchaseValue) {
        return new Asset(null, serial, name, description, purchaseDate, purchaseValue,
                AssetStatus.ACTIVE, null, null);
    }

    public static Asset restore(UUID id, String serial, String name, String description,
                                LocalDate purchaseDate, BigDecimal purchaseValue,
                                AssetStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Asset(id, serial, name, description, purchaseDate, purchaseValue,
                status, createdAt, updatedAt);
    }

    public void updateDetails(String serial, String name, String description,
                              LocalDate purchaseDate, BigDecimal purchaseValue) {
        this.serial = serial;
        this.name = name;
        this.description = description;
        this.purchaseDate = purchaseDate;
        this.purchaseValue = purchaseValue;
        this.updatedAt = LocalDateTime.now();
    }

    public void retire() {
        this.status = AssetStatus.RETIRED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isRetired() {
        return this.status == AssetStatus.RETIRED;
    }

    public UUID getId() {
        return id;
    }

    public String getSerial() {
        return serial;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public BigDecimal getPurchaseValue() {
        return purchaseValue;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
