package com.andres.inventorymanager.infrastructure.adapter.in.rest;

import com.andres.inventorymanager.application.port.in.AssetDetail;
import com.andres.inventorymanager.application.port.in.CreateAssetUseCase;
import com.andres.inventorymanager.application.port.in.GetAssetUseCase;
import com.andres.inventorymanager.domain.exception.AssetNotFoundException;
import com.andres.inventorymanager.domain.exception.DuplicateSerialException;
import com.andres.inventorymanager.domain.model.Asset;
import com.andres.inventorymanager.domain.model.AssetStatus;
import com.andres.inventorymanager.domain.model.DepreciationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetController.class)
@Import(AssetRestMapper.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateAssetUseCase createAssetUseCase;

    @MockitoBean
    private GetAssetUseCase getAssetUseCase;

    private static final String VALID_BODY = """
            {
              "serial": "SN-001",
              "name": "Laptop",
              "description": "Laptop de pruebas",
              "purchaseDate": "2024-01-15",
              "purchaseValue": 1500000.00
            }
            """;

    private Asset sampleAsset(UUID id) {
        return Asset.restore(
                id, "SN-001", "Laptop", "Laptop de pruebas",
                LocalDate.of(2021, 1, 15), new BigDecimal("1000000.00"),
                AssetStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void create_returns201WithBody_whenRequestIsValid() throws Exception {
        Asset asset = sampleAsset(UUID.randomUUID());
        when(createAssetUseCase.createAsset(any())).thenReturn(asset);

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(asset.getId().toString()))
                .andExpect(jsonPath("$.serial").value("SN-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void create_returns400_whenRequestIsInvalid() throws Exception {
        String invalidBody = """
                {
                  "serial": "",
                  "name": "",
                  "purchaseDate": "2999-01-01",
                  "purchaseValue": -1
                }
                """;

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns409_whenSerialAlreadyExists() throws Exception {
        when(createAssetUseCase.createAsset(any()))
                .thenThrow(new DuplicateSerialException("SN-001"));

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_returns200WithDepreciation_whenAssetExists() throws Exception {
        UUID id = UUID.randomUUID();
        Asset asset = sampleAsset(id);
        DepreciationResult depreciation = new DepreciationResult(
                3, new BigDecimal("40000.00"), new BigDecimal("120000.00"), new BigDecimal("880000.00"));
        when(getAssetUseCase.getAsset(id)).thenReturn(new AssetDetail(asset, depreciation));

        mockMvc.perform(get("/api/v1/assets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.serial").value("SN-001"))
                .andExpect(jsonPath("$.depreciation.yearsElapsed").value(3))
                .andExpect(jsonPath("$.depreciation.annualDepreciation").value(40000.00))
                .andExpect(jsonPath("$.depreciation.accumulatedDepreciation").value(120000.00))
                .andExpect(jsonPath("$.depreciation.currentValue").value(880000.00));
    }

    @Test
    void getById_returns404_whenAssetDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(getAssetUseCase.getAsset(id)).thenThrow(new AssetNotFoundException(id));

        mockMvc.perform(get("/api/v1/assets/{id}", id))
                .andExpect(status().isNotFound());
    }
}
