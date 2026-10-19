# Diagramas de secuencia

Flujos principales del servicio REST.

## 1. Registrar equipo — `POST /api/v1/assets`

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant C as AssetController
    participant M as AssetRestMapper
    participant S as AssetService
    participant R as AssetRepositoryPort
    participant A as Asset

    Client->>C: POST /api/v1/assets (AssetRequest)
    C->>C: @Valid valida el request
    C->>M: toCommand(request)
    M-->>C: CreateAssetCommand
    C->>S: createAsset(command)
    S->>R: existsBySerial(serial)
    R-->>S: boolean
    alt serial ya existe
        S-->>C: DuplicateSerialException
        C-->>Client: 409 Conflict (ProblemDetail)
    else serial único
        S->>A: Asset.create(...)
        A-->>S: Asset (id, status ACTIVE, timestamps)
        S->>R: save(asset)
        R-->>S: Asset persistido
        S-->>C: Asset
        C->>M: toResponse(asset)
        M-->>C: AssetResponse
        C-->>Client: 201 Created
    end
```

## 2. Consultar equipo por id (con depreciación) — `GET /api/v1/assets/{id}`

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant C as AssetController
    participant S as AssetService
    participant R as AssetRepositoryPort
    participant P as DepreciationRateProvider
    participant D as DepreciationCalculator

    Client->>C: GET /api/v1/assets/{id}
    C->>S: getAsset(id)
    S->>R: findById(id)
    R-->>S: Optional<Asset>
    alt no existe
        S-->>C: AssetNotFoundException
        C-->>Client: 404 Not Found (ProblemDetail)
    else existe
        S->>P: getAnnualRate()
        P-->>S: rate (ej. 0.04)
        S->>D: calculate(purchaseValue, purchaseDate, rate, today)
        Note over D: años = YEARS.between(fechaCompra, hoy)<br/>anual = valor * tasa<br/>acumulada = min(anual * años, valor)<br/>actual = valor - acumulada (>= 0)
        D-->>S: DepreciationResult
        S-->>C: AssetDetail(asset, depreciation)
        C-->>Client: 200 OK (datos + bloque depreciation)
    end
```

## 3. Actualizar equipo — `PUT /api/v1/assets/{id}`

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant C as AssetController
    participant S as AssetService
    participant R as AssetRepositoryPort
    participant A as Asset

    Client->>C: PUT /api/v1/assets/{id} (AssetRequest)
    C->>C: @Valid valida el request
    C->>S: updateAsset(id, command)
    S->>R: findById(id)
    R-->>S: Optional<Asset>
    alt no existe
        S-->>C: AssetNotFoundException
        C-->>Client: 404 Not Found
    else está retirado
        S-->>C: AssetAlreadyRetiredException
        C-->>Client: 409 Conflict
    else actualizable
        opt el serial cambió
            S->>R: existsBySerial(nuevoSerial)
            R-->>S: boolean
            alt serial ya usado
                S-->>C: DuplicateSerialException
                C-->>Client: 409 Conflict
            end
        end
        S->>A: updateDetails(...)
        S->>R: save(asset)
        R-->>S: Asset persistido
        S-->>C: AssetDetail (con depreciación recalculada)
        C-->>Client: 200 OK
    end
```

## 4. Dar de baja lógica — `PATCH /api/v1/assets/{id}/retire`

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant C as AssetController
    participant S as AssetService
    participant R as AssetRepositoryPort
    participant A as Asset

    Client->>C: PATCH /api/v1/assets/{id}/retire
    C->>S: retireAsset(id)
    S->>R: findById(id)
    R-->>S: Optional<Asset>
    alt no existe
        S-->>C: AssetNotFoundException
        C-->>Client: 404 Not Found
    else ya está retirado
        S-->>C: AssetAlreadyRetiredException
        C-->>Client: 409 Conflict
    else activo
        S->>A: retire()
        Note over A: status = RETIRED, updatedAt = now
        S->>R: save(asset)
        R-->>S: Asset persistido
        S-->>C: AssetDetail (status RETIRED)
        C-->>Client: 200 OK
    end
```

[← Volver al README](../README.md)
