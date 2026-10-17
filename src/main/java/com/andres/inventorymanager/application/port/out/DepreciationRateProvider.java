package com.andres.inventorymanager.application.port.out;

import java.math.BigDecimal;

public interface DepreciationRateProvider {

    BigDecimal getAnnualRate();
}
