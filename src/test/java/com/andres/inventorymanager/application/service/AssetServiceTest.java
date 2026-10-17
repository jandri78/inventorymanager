package com.andres.inventorymanager.application.service;

import com.andres.inventorymanager.application.port.in.CreateAssetCommand;
import com.andres.inventorymanager.application.port.out.AssetRepositoryPort;
import com.andres.inventorymanager.domain.exception.DuplicateSerialException;
import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.domain.model.AssetStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepositoryPort assetRepositoryPort;

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
}
