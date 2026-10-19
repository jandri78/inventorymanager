# Inventory Manager

Servicio REST para el inventario y la depreciación de los activos de una compañía. Permite
registrar equipos (serial, nombre, descripción, fecha y valor de compra), consultar su valor
depreciado, actualizar su información y darlos de baja de forma lógica.

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Maven (wrapper incluido)
- Spring Data JPA + H2 (base de datos en memoria)
- Bean Validation
- Springdoc OpenAPI 3 (Swagger UI)
- Docker / Docker Compose

## Arquitectura

Se implementa una **arquitectura hexagonal (puertos y adaptadores)**:

- **Dominio:** modelo `Asset`, `DepreciationCalculator` y excepciones de negocio. No depende de Spring ni de JPA.
- **Aplicación:** puertos de entrada (`Create/Get/List/Update/Retire UseCase`), puertos de salida
  (`AssetRepositoryPort`, `DepreciationRateProvider`) y `AssetService` como orquestador transaccional.
- **Infraestructura:** adaptador REST (`AssetController`), adaptador de persistencia JPA,
  configuración y manejo global de errores.

La frontera transaccional está en `AssetService` y la tasa de depreciación se obtiene a través
de un puerto, de modo que puede cambiarse sin tocar el dominio.

## Reglas de negocio

- Depreciación **lineal sobre el valor de compra**: `valor * tasa * añosTranscurridos`.
- La depreciación acumulada nunca supera el valor de compra (el valor actual no baja de 0).
- Tasa anual por defecto: **4%** (`app.depreciation.rate: 0.04`), configurable.
- El `serial` es obligatorio y único.
- La baja de equipos es **lógica** (`status = RETIRED`); el registro se conserva para consulta.
- Un equipo retirado no se puede actualizar ni volver a dar de baja.

## Endpoints

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/v1/assets` | Registrar un equipo |
| `GET` | `/api/v1/assets` | Listar equipos (filtro opcional `?status=ACTIVE\|RETIRED`) |
| `GET` | `/api/v1/assets/{id}` | Consultar un equipo con su depreciación |
| `PUT` | `/api/v1/assets/{id}` | Actualizar un equipo |
| `PATCH` | `/api/v1/assets/{id}/retire` | Dar de baja lógica |

Documentación interactiva (Swagger UI): `http://localhost:8080/swagger-ui.html`

Ejemplo de registro:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/assets `
  -ContentType "application/json" `
  -Body '{"serial":"SN-001","name":"Laptop","description":"Equipo de pruebas","purchaseDate":"2021-01-15","purchaseValue":1000000.00}'
```

## Diagramas

- [Diagrama de clases](docs/class-diagram.md)
- [Diagrama de componentes](docs/component-diagram.md)
- [Diagramas de secuencia](docs/sequence-diagrams.md)

## Ejecución en local

### Requisitos

- JDK 21
- Maven (o usar el wrapper incluido `mvnw` / `mvnw.cmd`)
- Docker y Docker Compose (opcional, para las opciones con contenedores)

### Opción A: Java / Maven

```powershell
# Windows
.\mvnw.cmd spring-boot:run
```

```bash
# macOS / Linux
./mvnw spring-boot:run
```

La aplicación queda disponible en:

| Recurso | URL |
| --- | --- |
| API | `http://localhost:8080/api/v1/assets` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Health | `http://localhost:8080/actuator/health` |
| Consola H2 | `http://localhost:8080/h2-console` |

Consola H2: JDBC URL `jdbc:h2:mem:testdb`, usuario `sa`, contraseña vacía.

### Opción B: Docker (Dockerfile)

Construir la imagen:

```bash
docker build -t inventorymanager .
```

Ejecutar el contenedor:

```bash
docker run --rm -p 8080:8080 inventorymanager
```

### Opción C: Docker Compose

El repositorio incluye `docker-compose.yml`, que construye la imagen y publica el puerto 8080:

```bash
docker compose up --build
```

Para detenerlo:

```bash
docker compose down
```

### Nota sobre H2

H2 está configurado **en memoria** (`jdbc:h2:mem:testdb`), por lo que los datos se reinician
cada vez que se reinicia la aplicación o el contenedor.

## Configuración

La tasa de depreciación se configura en `src/main/resources/application.yaml`:

```yaml
app:
  depreciation:
    rate: 0.04
```

También se puede sobrescribir por variable de entorno o argumento de arranque, por ejemplo:

```bash
docker run --rm -p 8080:8080 -e APP_DEPRECIATION_RATE=0.05 inventorymanager
```

La propiedad se valida al arrancar: debe estar presente y ser un valor entre `0.0` y `1.0`.

## Pruebas

```powershell
# Windows
.\mvnw.cmd test
```

## Estructura del proyecto

```
src/main/java/com/andres/inventorymanager
├── domain
│   ├── model
│   ├── service
│   └── exception
├── application
│   ├── port
│   │   ├── in
│   │   └── out
│   └── service
└── infrastructure
    ├── adapter
    │   ├── in/rest
    │   └── out/persistence
    └── config
```
