package com.andres.inventorymanager.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateAssetCommand(
        String serial,
        String name,
        String description,
        LocalDate purchaseDate,
        BigDecimal purchaseValue
) {
}
