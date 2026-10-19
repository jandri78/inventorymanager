# Diagrama de componentes

Vista por capas de la arquitectura hexagonal: el cliente entra por el adaptador REST, la
aplicación orquesta los casos de uso a través de puertos, el dominio contiene las reglas de
negocio y la persistencia se resuelve con un adaptador JPA hacia H2.

```mermaid
flowchart LR
    Client(["Cliente HTTP"])
    Swagger["Swagger UI / OpenAPI 3"]
    H2[("H2 Database")]

    subgraph REST["Adaptador de entrada - REST"]
        direction TB
        Controller["AssetController"]
        RestMapper["AssetRestMapper"]
        DTOs["DTOs<br/>AssetRequest / AssetResponse / DepreciationResponse"]
        ErrorHandler["GlobalExceptionHandler"]
    end

    subgraph APP["Capa de aplicación"]
        direction TB
        InPorts["Puertos de entrada<br/>Create / Get / List / Update / Retire UseCase"]
        Service["AssetService<br/>@Transactional"]
        OutPortRepo["AssetRepositoryPort"]
        OutPortRate["DepreciationRateProvider"]
    end

    subgraph DOMAIN["Capa de dominio"]
        direction TB
        Model["Asset / AssetStatus"]
        Calculator["DepreciationCalculator"]
        DepResult["DepreciationResult"]
        DomExceptions["Excepciones de dominio"]
    end

    subgraph PERSIST["Adaptador de salida - Persistencia"]
        direction TB
        PersistAdapter["AssetPersistenceAdapter"]
        JpaRepository["AssetRepository<br/>Spring Data JPA"]
        Entity["AssetJpaEntity"]
        PersistMapper["AssetPersistenceMapper"]
    end

    subgraph CONFIG["Configuración"]
        direction TB
        Properties["DepreciationProperties<br/>app.depreciation.rate"]
        DomainConfig["DomainConfig"]
    end

    Client -->|"HTTP / JSON"| Controller
    Client -.->|"documentación"| Swagger
    Swagger -.->|"expone"| Controller
    Controller --> DTOs
    Controller --> RestMapper
    Controller --> InPorts
    Controller -.-> ErrorHandler
    RestMapper --> DTOs
    Service -.->|"implementa"| InPorts
    Service --> Model
    Service --> Calculator
    Service --> OutPortRepo
    Service --> OutPortRate
    Calculator --> Model
    Calculator --> DepResult
    Properties -.->|"implementa"| OutPortRate
    PersistAdapter -.->|"implementa"| OutPortRepo
    PersistAdapter --> JpaRepository
    PersistAdapter --> PersistMapper
    PersistMapper --> Model
    PersistMapper --> Entity
    JpaRepository --> Entity
    JpaRepository --> H2
    DomainConfig --> Calculator
    ErrorHandler -.-> DomExceptions
    Service -.-> DomExceptions
```

**Regla de dependencia:** las flechas de infraestructura apuntan hacia las interfaces
(puertos) de la aplicación, y el dominio no depende de ninguna capa externa. Esto permite
sustituir H2, la fuente de la tasa de depreciación o el adaptador REST sin tocar las reglas
de negocio.

[← Volver al README](../README.md)
