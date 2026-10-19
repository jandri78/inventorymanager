# Diagrama de clases

Vista acotada al **núcleo** del sistema: el modelo de dominio y la capa de aplicación
(puertos y servicio). La infraestructura (controlador REST, mappers, DTOs, entidad JPA) se
describe en el [diagrama de componentes](component-diagram.md).

## Modelo de dominio

```mermaid
classDiagram
    direction LR

    class Asset {
        +create(serial, name, description, purchaseDate, purchaseValue) Asset
        +restore(id, serial, name, description, purchaseDate, purchaseValue, status, createdAt, updatedAt) Asset
        +updateDetails(serial, name, description, purchaseDate, purchaseValue) void
        +retire() void
        +isRetired() boolean
    }

    class AssetStatus {
        <<enumeration>>
        ACTIVE
        RETIRED
    }

    class DepreciationResult {
        <<record>>
        +yearsElapsed
        +annualDepreciation
        +accumulatedDepreciation
        +currentValue
    }

    class DepreciationCalculator {
        +calculate(purchaseValue, purchaseDate, annualRate, referenceDate) DepreciationResult
    }

    Asset --> AssetStatus : status
    DepreciationCalculator ..> DepreciationResult : crea
```


## Capa de aplicación

```mermaid
classDiagram
    direction LR

    class CreateAssetUseCase {
        <<interface>>
        +createAsset(command) Asset
    }
    class GetAssetUseCase {
        <<interface>>
        +getAsset(id) AssetDetail
    }
    class ListAssetsUseCase {
        <<interface>>
        +listAssets(status) List~AssetDetail~
    }
    class UpdateAssetUseCase {
        <<interface>>
        +updateAsset(id, command) AssetDetail
    }
    class RetireAssetUseCase {
        <<interface>>
        +retireAsset(id) AssetDetail
    }

    class AssetService {
        +createAsset(command) Asset
        +getAsset(id) AssetDetail
        +listAssets(status) List~AssetDetail~
        +updateAsset(id, command) AssetDetail
        +retireAsset(id) AssetDetail
    }

    class AssetRepositoryPort {
        <<interface>>
        +save(asset) Asset
        +findById(id) Optional~Asset~
        +findAll() List~Asset~
        +findByStatus(status) List~Asset~
        +existsBySerial(serial) boolean
    }
    class DepreciationRateProvider {
        <<interface>>
        +getAnnualRate() BigDecimal
    }

    class AssetDetail {
        <<record>>
        +Asset asset
        +DepreciationResult depreciation
    }
    class CreateAssetCommand {
        <<record>>
    }
    class UpdateAssetCommand {
        <<record>>
    }

    AssetService ..|> CreateAssetUseCase
    AssetService ..|> GetAssetUseCase
    AssetService ..|> ListAssetsUseCase
    AssetService ..|> UpdateAssetUseCase
    AssetService ..|> RetireAssetUseCase
    AssetService --> AssetRepositoryPort : usa
    AssetService --> DepreciationRateProvider : usa
    CreateAssetUseCase ..> CreateAssetCommand
    UpdateAssetUseCase ..> UpdateAssetCommand
    GetAssetUseCase ..> AssetDetail
    ListAssetsUseCase ..> AssetDetail
    UpdateAssetUseCase ..> AssetDetail
    RetireAssetUseCase ..> AssetDetail
```

[← Volver al README](../README.md)
