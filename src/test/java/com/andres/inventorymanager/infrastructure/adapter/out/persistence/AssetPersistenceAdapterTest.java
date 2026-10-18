package com.andres.inventorymanager.infrastructure.adapter.out.persistence;

import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.domain.model.AssetStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AssetPersistenceAdapter.class)
class AssetPersistenceAdapterTest {

    @Autowired
    private AssetPersistenceAdapter adapter;

    private Asset newAsset(String serial, AssetStatus status) {
        return Asset.restore(UUID.randomUUID(), serial, "name-" + serial, "desc",
                LocalDate.of(2021, 1, 15), new BigDecimal("1000000.00"),
                status, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void save_persistsAssetAndReturnsIt() {
        Asset saved = adapter.save(newAsset("SN-001", AssetStatus.ACTIVE));

        Optional<Asset> found = adapter.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSerial()).isEqualTo("SN-001");
        assertThat(found.get().getPurchaseValue()).isEqualByComparingTo("1000000.00");
    }

    @Test
    void findById_returnsEmpty_whenAssetDoesNotExist() {
        assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAll_returnsAllPersistedAssets() {
        adapter.save(newAsset("SN-001", AssetStatus.ACTIVE));
        adapter.save(newAsset("SN-002", AssetStatus.RETIRED));

        List<Asset> all = adapter.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Asset::getSerial).containsExactlyInAnyOrder("SN-001", "SN-002");
    }

    @Test
    void findByStatus_returnsOnlyMatchingAssets() {
        adapter.save(newAsset("SN-001", AssetStatus.ACTIVE));
        adapter.save(newAsset("SN-002", AssetStatus.RETIRED));

        List<Asset> active = adapter.findByStatus(AssetStatus.ACTIVE);

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getSerial()).isEqualTo("SN-001");
    }

    @Test
    void existsBySerial_reflectsPersistedState() {
        adapter.save(newAsset("SN-001", AssetStatus.ACTIVE));

        assertThat(adapter.existsBySerial("SN-001")).isTrue();
        assertThat(adapter.existsBySerial("SN-999")).isFalse();
    }

    @Test
    void save_persistsUpdatedDetails_whenAssetAlreadyExists() {
        Asset saved = adapter.save(newAsset("SN-001", AssetStatus.ACTIVE));
        saved.updateDetails("SN-001", "Servidor", "actualizado",
                LocalDate.of(2020, 5, 10), new BigDecimal("2000000.00"));

        adapter.save(saved);

        Asset found = adapter.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Servidor");
        assertThat(found.getDescription()).isEqualTo("actualizado");
        assertThat(found.getPurchaseValue()).isEqualByComparingTo("2000000.00");
        assertThat(found.getPurchaseDate()).isEqualTo(LocalDate.of(2020, 5, 10));
    }
}
