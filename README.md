# Resumen de Nómina API - Grupo Elektra

Sistema de análisis y visualización de compensaciones para la Red Grupo Elektra.

## Descripción General

Este sistema permite el análisis de datos de compensación de empleados, incluyendo:
- Visualización de compensaciones a nivel nacional y por negocio
- Series temporales con análisis estadístico
- Detección de anomalías mediante Z-Score e intervalos de predicción
- Mapas de calor para identificar variaciones significativas

## Arquitectura

El proyecto sigue una **Arquitectura Hexagonal** (Ports & Adapters):

```
src/main/java/com/elektra/resumennomina/
├── api/                    # Capa de presentación (REST Controllers)
│   ├── controller/         # Controladores REST
│   ├── dto/                # Data Transfer Objects
│   ├── exception/          # Manejo de excepciones
│   └── mapper/             # Mappers DTO <-> Domain
├── application/            # Capa de aplicación (Casos de uso)
│   └── service/            # Servicios de aplicación
├── domain/                 # Capa de dominio (Núcleo del negocio)
│   ├── model/              # Modelos de dominio
│   └── port/               # Puertos (interfaces)
│       ├── input/          # Puertos de entrada (casos de uso)
│       └── output/         # Puertos de salida (repositorios)
└── infrastructure/         # Capa de infraestructura
    ├── config/             # Configuraciones
    │   ├── aws/            # Configuración AWS (SSM, DocumentDB)
    │   └── cache/          # Configuración de caché
    └── persistence/        # Implementación de persistencia
        ├── adapter/        # Adaptadores de repositorio
        └── document/       # Documentos MongoDB
```

## Stack Tecnológico

| Componente | Tecnología |
|------------|------------|
| Backend | Spring Boot 3.2.2 |
| Base de datos | MongoDB / AWS DocumentDB |
| Build | Maven |
| Java | 17+ |
| Cloud | AWS (SSM, Secrets Manager, DocumentDB) |

---

## API REST

Base URL: `http://localhost:8080/api/v1`

### Compensación

#### Nacional

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/compensacion/nacional` | Resumen nacional de compensaciones |
| GET | `/compensacion/nacional/serie` | Serie temporal nacional |

**GET /compensacion/nacional**

Obtiene el resumen de compensaciones a nivel nacional.

```
Parámetros:
  - periodo (opcional): Código del periodo YYYYWW (ej: 202544)

Respuesta:
{
  "success": true,
  "data": {
    "periodoActual": "2025-44",
    "periodoAnterior": "2025-43",
    "totalSemanaActual": 150000000,
    "totalSemanaAnterior": 145000000,
    "diferencia": 5000000,
    "variacionPorcentual": 3.45,
    "negocios": [...]
  }
}
```

#### Negocios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/compensacion/negocios` | Lista de negocios disponibles |
| GET | `/compensacion/negocios/{negocio}/serie` | Serie temporal por negocio |
| GET | `/compensacion/negocios/{negocio}/puestos` | Puestos de un negocio |
| GET | `/compensacion/negocios/{negocio}/puestos/{puesto}/serie` | Serie por negocio y puesto |
| GET | `/compensacion/negocios/{negocio}/puestos/{puesto}/resumen-indicadores` | Resumen de indicadores |
| GET | `/compensacion/negocios/{negocio}/puestos/{puesto}/indicadores/{indicador}/serie` | Serie por indicador |

**GET /compensacion/negocios/{negocio}/puestos/{puesto}/resumen-indicadores**

Obtiene el resumen de todos los indicadores para un puesto específico.

```
Parámetros:
  - negocio: Nombre del negocio (ej: "Cobranza")
  - puesto: Nombre del puesto (ej: "GCA")
  - periodo (opcional): Código del periodo YYYYWW

Respuesta:
{
  "success": true,
  "data": [
    {
      "indicador": "Compensación",
      "valorActual": 5000,
      "valorAnterior": 4800,
      "diferencia": 200,
      "variacionPorcentual": 4.17,
      "variacionPositiva": true
    },
    ...
  ]
}
```

#### Periodos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/compensacion/periodos` | Lista de periodos disponibles |
| GET | `/compensacion/periodo-actual` | Periodo actual del sistema |

#### Indicadores

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/compensacion/indicadores` | Lista de indicadores disponibles |
| GET | `/compensacion/negocios/{negocio}/indicadores` | Indicadores por negocio |
| GET | `/compensacion/negocios/{negocio}/puestos/{puesto}/indicadores` | Indicadores por puesto |

#### Mapa de Calor

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/compensacion/mapa-calor` | Datos para mapa de calor |

**GET /compensacion/mapa-calor**

Obtiene datos de variación para visualización en mapa de calor.

```
Parámetros:
  - periodo (opcional): Código del periodo YYYYWW

Respuesta:
{
  "success": true,
  "data": {
    "items": [
      {
        "negocio": "Elektra",
        "puesto": "Vendedor",
        "valorSemanaActual": 50000,
        "valorSemanaAnterior": 48000,
        "variacionPorcentual": 4.17
      },
      ...
    ]
  }
}
```

#### Tabla de Resultados

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/compensacion/tabla-resultados/total` | Tabla con valores totales |
| GET | `/compensacion/tabla-resultados/promedio` | Tabla con promedios por empleado |

---

### Alertas

#### Z-Score

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/alertas/zscore` | Alertas basadas en Z-Score |

**GET /alertas/zscore**

Detecta anomalías cuando la variación semanal sale del rango habitual.

**Lógica de cálculo:**
1. Calcula variación % entre periodos consecutivos
2. Obtiene media y desviación estándar históricas
3. Calcula umbrales dinámicos según volatilidad:
   - σ < 1%: margen = 1.5%
   - 1% ≤ σ < 5%: margen = 1.3×σ
   - σ ≥ 5%: margen = σ
4. Valida alerta si cumple 3 condiciones:
   - Supera umbrales dinámicos
   - Diferencia absoluta > 1%
   - Z-Score > 1

**Severidad:**
| Nivel | Condición | Probabilidad |
|-------|-----------|--------------|
| CRÍTICA | Z-Score > 3 | 0.3% |
| ALTA | Z-Score > 2 | 5% |
| MODERADA | Z-Score > 1 | 32% |

```
Respuesta:
{
  "success": true,
  "data": [
    {
      "puesto": "GCA",
      "indicador": "Compensación",
      "variacionPorcentualVsSA": 205.50,
      "variacionMedia": 5.93,
      "limiteInferior": -14.07,
      "limiteSuperior": 25.93,
      "severidad": "CRITICA",
      "zScore": 6.67
    },
    ...
  ]
}
```

#### Intervalo de Predicción 95%

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/alertas/prediccion` | Alertas de intervalo de predicción |

**GET /alertas/prediccion**

Detecta anomalías usando modelo de predicción con intervalo de confianza 95%.

**Lógica de cálculo:**
1. Ajusta modelo EMA (Exponential Moving Average) con tendencia
2. Calcula error estándar del pronóstico
3. Intervalo = Pronóstico ± 1.96 × SE
4. Alerta si valor real está fuera del intervalo

```
Respuesta:
{
  "success": true,
  "data": [
    {
      "puesto": "Vendedor",
      "indicador": "Compensación",
      "observacionReal": 15417131.25,
      "limiteInferior": 15461521.11,
      "limiteSuperior": 17320477.54,
      "variacionFueraRango": -0.27,
      "historiaInsuficiente": false
    },
    ...
  ]
}
```

---

### Administración (Carga de Datos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/admin/carga/reemplazar` | Carga masiva reemplazando todos los datos |
| POST | `/admin/carga/incremental` | Carga incremental (agregar/actualizar) |
| POST | `/admin/carga/periodo/{periodo}` | Carga para un periodo específico |
| DELETE | `/admin/periodo/{periodo}` | Elimina datos de un periodo |
| GET | `/admin/estadisticas` | Estadísticas de datos cargados |

**POST /admin/carga/reemplazar**

Carga masiva de datos desde archivo CSV o Excel, reemplazando todos los datos existentes.

```
Parámetros (multipart/form-data):
  - file: Archivo CSV o Excel (.xlsx)
  - usuario (opcional): Usuario que realiza la carga

Respuesta:
{
  "success": true,
  "data": {
    "success": true,
    "mensaje": "Carga completa: 15000 registros insertados",
    "fechaCarga": "2025-01-23T10:30:00",
    "usuarioCarga": "admin",
    "archivoOriginal": "DatosInteligencia_202544.csv",
    "totalRegistros": 15000,
    "registrosExitosos": 15000,
    "registrosConError": 0,
    "tiempoProcesamientoMs": 3500,
    "errores": []
  }
}
```

**Formato del archivo CSV:**

```csv
PkiPuesto,PkiSucursal,PkiEmpleado,PkiCDGenerico,PkiPais,PkiPeriodo,PkiGrupoNegocio,PkiCanal,PkiConceptoDetalle,FnValor,FnDetalle1,FnDetalle2,PkcDetalle3,FcDetalle4,FcDetalle5,FcDetalle6,FnDetalle7
2367,0,0,10011,1,202544,2,1103,1001,5540790.24,0,0,0,Crédito,AdP SF Entrenamiento,Compensación,0
```

**GET /admin/estadisticas**

Obtiene estadísticas de los datos cargados, agrupadas por año y con totales generales.

```
Respuesta:
{
  "success": true,
  "data": {
    "estadisticasPorAnio": [
      {
        "anio": 2025,
        "totalRegistros": 120000,
        "totalPeriodos": 44,
        "periodos": [202501, 202502, ..., 202544],
        "totalNegocios": 8,
        "negocios": ["Elektra", "Crédito", "Cobranza", ...]
      },
      {
        "anio": 2026,
        "totalRegistros": 30000,
        "totalPeriodos": 3,
        "periodos": [202601, 202602, 202603],
        "totalNegocios": 6,
        "negocios": ["Elektra", "Crédito", ...]
      }
    ],
    "totales": {
      "totalRegistros": 150000,
      "totalPeriodos": 47,
      "totalNegocios": 8,
      "negocios": ["Elektra", "Crédito", "Cobranza", ...]
    }
  }
}
```

---

## Series Temporales

Todas las series temporales incluyen:

```json
{
  "negocio": "Elektra",
  "puesto": "Vendedor",
  "indicador": "Compensación",
  "serie": [
    {
      "semana": "2025-40",
      "valor": 2500.00,
      "variacionVsSemanaAnterior": 2.5,
      "fueraDe1Sigma": false,
      "fueraDe15Sigma": false
    },
    ...
  ],
  "lineasConfianza": {
    "media": 2450.00,
    "desviacionEstandar": 150.00,
    "limiteSuperior1Sigma": 2600.00,
    "limiteInferior1Sigma": 2300.00,
    "limiteSuperior15Sigma": 2675.00,
    "limiteInferior15Sigma": 2225.00
  }
}
```

---

## Configuración

### Perfiles de Spring

| Perfil | Uso | Base de datos |
|--------|-----|---------------|
| `local` | Desarrollo local | MongoDB localhost:27017 |
| `qa` | Ambiente QA | AWS DocumentDB |
| `prod` | Producción | AWS DocumentDB |

### Variables de Entorno

| Variable | Descripción | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo | local |
| `AWS_REGION` | Región AWS | us-east-1 |
| `CORS_ALLOWED_ORIGINS` | Orígenes CORS | http://localhost:4200 |

### Configuración QA/PROD (AWS)

El sistema lee credenciales de AWS Systems Manager Parameter Store:

```
Parámetro: /aws/reference/secretsmanager/qa/resumennomina/database/documentdb
```

El secreto debe contener:
```json
{
  "documentdb.uri": "mongodb://user:pass@host:27017/?ssl=true&authMechanism=SCRAM-SHA-1",
  "documentdb.ssl.truststore.path": "documentdb-truststore.jks",
  "documentdb.ssl.truststore.password": "changeit"
}
```

---

## Ejecución

### Local

```bash
# Compilar
mvn clean package -DskipTests

# Ejecutar
java -jar target/resumen-nomina-1.0.0-SNAPSHOT.jar

# O con Maven
mvn spring-boot:run -Dspring.profiles.active=local
```

### Configuración en IntelliJ IDEA (QA/PROD)

Para ejecutar el proyecto con perfil QA o PROD desde IntelliJ:

#### 1. Truststore de DocumentDB

El proyecto requiere el archivo `documentdb-truststore.jks` para conectarse a AWS DocumentDB con SSL.

**Ubicación requerida:** `backend/opt/documentdb-truststore.jks`

Si no existe, copiar desde otro proyecto o generarlo:
```bash
# Crear directorio si no existe
mkdir -p opt

# Copiar truststore (si existe en otro proyecto)
cp /ruta/otro/proyecto/opt/documentdb-truststore.jks opt/
```

#### 2. Configurar Run/Debug Configuration

1. Ir a **Run > Edit Configurations...**
2. Crear nueva configuración de **Spring Boot** o editar existente
3. Configurar los siguientes campos:

| Campo | Valor |
|-------|-------|
| **Main class** | `com.elektra.resumennomina.ResumenNominaApplication` |
| **Active profiles** | `qa` (o `prod`) |
| **VM options** | `--add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED` |

#### 3. VM Options (Obligatorio para Java 17+)

Las siguientes opciones de JVM son **requeridas** debido a restricciones del sistema de módulos de Java:

```
--add-opens java.base/java.math=ALL-UNNAMED
--add-opens java.base/java.lang=ALL-UNNAMED
```

Sin estas opciones, la aplicación fallará con error `InaccessibleObjectException`.

#### 4. Credenciales AWS

Para ambientes QA/PROD, el equipo debe tener configuradas las credenciales AWS:
- Via AWS CLI: `aws configure`
- Via variables de entorno: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
- Via IAM Role (en EC2/ECS)

### Docker (QA)

```bash
# Build
mvn clean package -DskipTests -Pqa
docker build -t resumen-nomina-api:qa .

# Run
docker run -d \
  -e AWS_REGION=us-east-1 \
  -e SPRING_PROFILES_ACTIVE=qa \
  -p 8080:8080 \
  resumen-nomina-api:qa
```

---

## Estructura de Datos

### Colección: datos_compensacion

```json
{
  "_id": "ObjectId",
  "codigoPeriodo": 202544,
  "negocio": "Elektra",
  "puesto": "Vendedor",
  "indicador": "Compensación",
  "valor": 5000.00,
  "funcionSap": 12345
}
```

### Mapeo de campos:
| Campo MongoDB | Descripción |
|---------------|-------------|
| FcDetalle4 | Negocio |
| FcDetalle5 | Puesto |
| FcDetalle6 | Indicador |
| FnSaldo | Valor |
| FnFuncionSap | Función SAP |

---

## Endpoints de Monitoreo

| Endpoint | Descripción |
|----------|-------------|
| `/actuator/health` | Estado de salud |
| `/actuator/info` | Información de la app |
| `/actuator/metrics` | Métricas |
| `/swagger-ui.html` | Documentación Swagger |
| `/api-docs` | OpenAPI JSON |

---

## Contacto

**Equipo:** Inteligencia de Datos - Compensalia
**Organización:** Yo y el Alexis jaja
