package com.andres.inventorymanager.application.service;

import com.andres.inventorymanager.application.port.in.AssetDetail;
import com.andres.inventorymanager.application.port.in.CreateAssetCommand;
import com.andres.inventorymanager.application.port.in.UpdateAssetCommand;
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
import java.util.List;
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

    private static final DepreciationResult DEPRECIATION = new DepreciationResult(
            3, new BigDecimal("40000.00"), new BigDecimal("120000.00"), new BigDecimal("880000.00"));

    private Asset sampleAsset(UUID id, String serial, AssetStatus status) {
        return Asset.restore(id, serial, "Laptop", "Laptop de pruebas",
                LocalDate.of(2021, 1, 15), new BigDecimal("1000000.00"),
                status, LocalDateTime.now(), LocalDateTime.now());
    }

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
        Asset asset = sampleAsset(id, "SN-001", AssetStatus.ACTIVE);

        when(assetRepositoryPort.findById(id)).thenReturn(Optional.of(asset));
        when(depreciationRateProvider.getAnnualRate()).thenReturn(new BigDecimal("0.04"));
        when(depreciationCalculator.calculate(any(), any(), any(), any())).thenReturn(DEPRECIATION);

        AssetDetail result = assetService.getAsset(id);

        assertThat(result.asset()).isEqualTo(asset);
        assertThat(result.depreciation()).isEqualTo(DEPRECIATION);
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

    @Test
    void listAssets_returnsAllWithDepreciation_whenStatusIsNull() {
        Asset first = sampleAsset(UUID.randomUUID(), "SN-001", AssetStatus.ACTIVE);
        Asset second = sampleAsset(UUID.randomUUID(), "SN-002", AssetStatus.RETIRED);

        when(assetRepositoryPort.findAll()).thenReturn(List.of(first, second));
        when(depreciationRateProvider.getAnnualRate()).thenReturn(new BigDecimal("0.04"));
        when(depreciationCalculator.calculate(any(), any(), any(), any())).thenReturn(DEPRECIATION);

        List<AssetDetail> result = assetService.listAssets(null);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(detail -> assertThat(detail.depreciation()).isEqualTo(DEPRECIATION));
        verify(assetRepositoryPort).findAll();
        verify(assetRepositoryPort, never()).findByStatus(any());
    }

    @Test
    void listAssets_filtersByStatus_whenStatusProvided() {
        Asset active = sampleAsset(UUID.randomUUID(), "SN-001", AssetStatus.ACTIVE);

        when(assetRepositoryPort.findByStatus(AssetStatus.ACTIVE)).thenReturn(List.of(active));
        when(depreciationRateProvider.getAnnualRate()).thenReturn(new BigDecimal("0.04"));
        when(depreciationCalculator.calculate(any(), any(), any(), any())).thenReturn(DEPRECIATION);

        List<AssetDetail> result = assetService.listAssets(AssetStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).asset().getSerial()).isEqualTo("SN-001");
        verify(assetRepositoryPort).findByStatus(AssetStatus.ACTIVE);
        verify(assetRepositoryPort, never()).findAll();
    }

    @Test
    void listAssets_returnsEmptyList_whenNoAssets() {
        when(assetRepositoryPort.findAll()).thenReturn(List.of());

        List<AssetDetail> result = assetService.listAssets(null);

        assertThat(result).isEmpty();
        verifyNoInteractions(depreciationCalculator, depreciationRateProvider);
    }

    @Test
    void updateAsset_updatesDetails_whenSerialUnchanged() {
        UUID id = UUID.randomUUID();
        Asset existing = sampleAsset(id, "SN-001", AssetStatus.ACTIVE);
        UpdateAssetCommand command = new UpdateAssetCommand(
                "SN-001", "Servidor", "nueva descripcion", LocalDate.of(2020, 5, 10), new BigDecimal("2000000.00"));

        when(assetRepositoryPort.findById(id)).thenReturn(Optional.of(existing));
        when(assetRepositoryPort.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(depreciationRateProvider.getAnnualRate()).thenReturn(new BigDecimal("0.04"));
        when(depreciationCalculator.calculate(any(), any(), any(), any())).thenReturn(DEPRECIATION);

        AssetDetail result = assetService.updateAsset(id, command);

        assertThat(result.asset().getName()).isEqualTo("Servidor");
        assertThat(result.asset().getDescription()).isEqualTo("nueva descripcion");
        assertThat(result.asset().getPurchaseValue()).isEqualByComparingTo("2000000.00");
        verify(assetRepositoryPort, never()).existsBySerial(any());
        verify(assetRepositoryPort).save(existing);
    }

    @Test
    void updateAsset_changesSerial_whenNewSerialIsUnique() {
        UUID id = UUID.randomUUID();
        Asset existing = sampleAsset(id, "SN-001", AssetStatus.ACTIVE);
        UpdateAssetCommand command = new UpdateAssetCommand(
                "SN-002", "Laptop", "Laptop de pruebas", LocalDate.of(2021, 1, 15), new BigDecimal("1000000.00"));

        when(assetRepositoryPort.findById(id)).thenReturn(Optional.of(existing));
        when(assetRepositoryPort.existsBySerial("SN-002")).thenReturn(false);
        when(assetRepositoryPort.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(depreciationRateProvider.getAnnualRate()).thenReturn(new BigDecimal("0.04"));
        when(depreciationCalculator.calculate(any(), any(), any(), any())).thenReturn(DEPRECIATION);

        AssetDetail result = assetService.updateAsset(id, command);

        assertThat(result.asset().getSerial()).isEqualTo("SN-002");
        verify(assetRepositoryPort).existsBySerial("SN-002");
    }

    @Test
    void updateAsset_throwsDuplicateSerial_whenNewSerialAlreadyExists() {
        UUID id = UUID.randomUUID();
        Asset existing = sampleAsset(id, "SN-001", AssetStatus.ACTIVE);
        UpdateAssetCommand command = new UpdateAssetCommand(
                "SN-999", "Laptop", "Laptop de pruebas", LocalDate.of(2021, 1, 15), new BigDecimal("1000000.00"));

        when(assetRepositoryPort.findById(id)).thenReturn(Optional.of(existing));
        when(assetRepositoryPort.existsBySerial("SN-999")).thenReturn(true);

        assertThatThrownBy(() -> assetService.updateAsset(id, command))
                .isInstanceOf(DuplicateSerialException.class)
                .hasMessageContaining("SN-999");

        verify(assetRepositoryPort, never()).save(any(Asset.class));
    }

    @Test
    void updateAsset_throwsAssetNotFound_whenAssetDoesNotExist() {
        UUID id = UUID.randomUUID();
        UpdateAssetCommand command = new UpdateAssetCommand(
                "SN-001", "Laptop", "Laptop de pruebas", LocalDate.of(2021, 1, 15), new BigDecimal("1000000.00"));

        when(assetRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.updateAsset(id, command))
                .isInstanceOf(AssetNotFoundException.class);

        verifyNoInteractions(depreciationCalculator, depreciationRateProvider);
    }
}
