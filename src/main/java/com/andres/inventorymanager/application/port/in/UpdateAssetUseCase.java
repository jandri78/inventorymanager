package com.andres.inventorymanager.application.port.in;

import java.util.UUID;

public interface UpdateAssetUseCase {

    AssetDetail updateAsset(UUID id, UpdateAssetCommand command);
}
