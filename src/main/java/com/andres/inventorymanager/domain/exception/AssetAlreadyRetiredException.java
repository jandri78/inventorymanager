package com.andres.inventorymanager.domain.exception;

import java.util.UUID;

public class AssetAlreadyRetiredException extends RuntimeException {

    public AssetAlreadyRetiredException(UUID id) {
        super("Asset with id " + id + " is already retired and cannot be modified");
    }
}
