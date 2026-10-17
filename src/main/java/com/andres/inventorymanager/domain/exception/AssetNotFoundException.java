package com.andres.inventorymanager.domain.exception;

import java.util.UUID;

public class AssetNotFoundException extends RuntimeException {

    public AssetNotFoundException(UUID id) {
        super("Asset not found with id: " + id);
    }

    public AssetNotFoundException(String message) {
        super(message);
    }
}
