package com.andres.inventorymanager.application.port.in;

import java.util.UUID;

public interface GetAssetUseCase {

    AssetDetail getAsset(UUID id);
}
