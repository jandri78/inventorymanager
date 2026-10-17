package com.andres.inventorymanager.application.service;

import com.andres.inventorymanager.application.port.in.AssetDetail;
import com.andres.inventorymanager.application.port.in.CreateAssetCommand;
import com.andres.inventorymanager.application.port.out.AssetRepositoryPort;
import com.andres.inventorymanager.application.port.out.DepreciationRateProvider;
import com.andres.inventorymanager.domain.exception.AssetNotFoundException;
import com.andres.inventorymanager.domain.exception.DuplicateSerialException;
import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.domain.model.AssetStatus;
import com.andres.inventorymanager.domain.model.DepreciationResult;
import com.andres.inventorymanager.domain.service.DepreciationCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepositoryPort assetRepositoryPort;

    @Mock
    private DepreciationCalculator depreciationCalculator;

    @Mock
    private DepreciationRateProvider depreciationRateProvider;

    @InjectMocks
    private AssetService assetService;

    private static final CreateAssetCommand COMMAND = new CreateAssetCommand(
            "SN-001",
            "Laptop",
            "Laptop de pruebas",
            LocalDate.of(2024, 1, 15),
            new BigDecimal("1500000.00"));

    @Test
    void createAsset_savesAndReturnsAsset_whenSerialIsUnique() {
        when(assetRepositoryPort.existsBySerial("SN-001")).thenReturn(false);
        when(assetRepositoryPort.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asset result = assetService.createAsset(COMMAND);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getSerial()).isEqualTo("SN-001");
        assertThat(result.getName()).isEqualTo("Laptop");
        assertThat(result.getStatus()).isEqualTo(AssetStatus.ACTIVE);
        assertThat(result.getCreatedAt()).isNotNull();

        ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getPurchaseValue()).isEqualByComparingTo("1500000.00");
    }

    @Test
    void createAsset_throwsDuplicateSerial_whenSerialAlreadyExists() {
        when(assetRepositoryPort.existsBySerial("SN-001")).thenReturn(true);

        assertThatThrownBy(() -> assetService.createAsset(COMMAND))
                .isInstanceOf(DuplicateSerialException.class)
                .hasMessageContaining("SN-001");

        verify(assetRepositoryPort, never()).save(any(Asset.class));
    }

    @Test
    void getAsset_returnsAssetWithDepreciation_whenAssetExists() {
        UUID id = UUID.randomUUID();
        Asset asset = Asset.restore(id, "SN-001", "Laptop", "Laptop de pruebas",
                LocalDate.of(2021, 1, 15), new BigDecimal("1000000.00"),
                AssetStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        DepreciationResult depreciation = new DepreciationResult(
                3, new BigDecimal("40000.00"), new BigDecimal("120000.00"), new BigDecimal("880000.00"));

        when(assetRepositoryPort.findById(id)).thenReturn(Optional.of(asset));
        when(depreciationRateProvider.getAnnualRate()).thenReturn(new BigDecimal("0.04"));
        when(depreciationCalculator.calculate(any(), any(), any(), any())).thenReturn(depreciation);

        AssetDetail result = assetService.getAsset(id);

        assertThat(result.asset()).isEqualTo(asset);
        assertThat(result.depreciation()).isEqualTo(depreciation);
    }

    @Test
    void getAsset_throwsAssetNotFound_whenAssetDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(assetRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.getAsset(id))
                .isInstanceOf(AssetNotFoundException.class)
                .hasMessageContaining(id.toString());

        verifyNoInteractions(depreciationCalculator, depreciationRateProvider);
    }
}
