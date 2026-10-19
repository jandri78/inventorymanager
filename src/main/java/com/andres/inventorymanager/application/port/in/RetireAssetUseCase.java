package com.andres.inventorymanager.application.port.in;

import java.util.UUID;

public interface RetireAssetUseCase {

    AssetDetail retireAsset(UUID id);
}
