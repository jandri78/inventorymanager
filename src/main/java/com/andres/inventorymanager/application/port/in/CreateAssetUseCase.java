package com.andres.inventorymanager.application.port.in;

import com.andres.inventorymanager.domain.model.Asset;

public interface CreateAssetUseCase {

    Asset createAsset(CreateAssetCommand command);
}
