package com.andres.inventorymanager.application.port.in;

import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.domain.model.DepreciationResult;

public record AssetDetail(Asset asset, DepreciationResult depreciation) {
}
