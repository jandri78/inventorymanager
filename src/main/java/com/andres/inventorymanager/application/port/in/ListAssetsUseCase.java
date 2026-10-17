package com.andres.inventorymanager.application.port.in;

import com.andres.inventorymanager.domain.model.AssetStatus;

import java.util.List;

public interface ListAssetsUseCase {

    List<AssetDetail> listAssets(AssetStatus status);
}
