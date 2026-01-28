# Resumen de Nómina - Frontend

Interfaz de usuario para el sistema de análisis y visualización de compensaciones de Grupo Elektra.

## Descripción General

Esta aplicación Angular proporciona:
- Dashboard con resumen nacional de compensaciones
- Series temporales con análisis estadístico
- Sistema de alertas con detección de anomalías (Z-Score e Intervalo de Predicción)
- Mapas de calor para visualización de variaciones
- Tablas de resultados con métricas detalladas

## Stack Tecnológico

| Componente | Tecnología |
|------------|------------|
| Framework | Angular 21 (Standalone Components) |
| UI | Angular Material |
| Gráficos | Chart.js / ng2-charts |
| Gestión de Estado | Signals (Angular 17+) |
| HTTP | HttpClient con Interceptors |
| Build | Angular CLI |

---

## Arquitectura

```
src/app/
├── core/                     # Núcleo de la aplicación
│   ├── models/               # Interfaces TypeScript
│   │   ├── compensacion.model.ts
│   │   ├── serie-temporal.model.ts
│   │   └── alerta.model.ts
│   └── services/             # Servicios HTTP
│       ├── compensacion.service.ts
│       └── alertas.service.ts
├── features/                 # Módulos de funcionalidad
│   ├── dashboard/            # Dashboard principal
│   ├── series/               # Series temporales
│   ├── alertas/              # Sistema de alertas
│   ├── mapa-calor/           # Mapa de calor
│   └── tabla-resultados/     # Tablas de datos
├── shared/                   # Componentes compartidos
│   └── components/
└── environments/             # Configuración por ambiente
    ├── environment.ts
    └── environment.prod.ts
```

---

## Componentes Principales

### Dashboard (`/dashboard`)

Vista principal con resumen de compensaciones a nivel nacional.

**Características:**
- Tarjetas resumen con totales y variaciones
- Gráfico de barras por negocio
- Selector de periodo
- Indicadores de tendencia (positiva/negativa)

### Series Temporales (`/series`)

Visualización de series temporales con bandas de confianza.

**Características:**
- Gráfico de líneas con histórico
- Bandas de confianza 1σ y 1.5σ
- Marcadores de puntos fuera de rango
- Filtros por negocio, puesto e indicador
- Estadísticas: media, desviación estándar

### Alertas (`/alertas`)

Sistema de detección de anomalías con dos métodos.

**Pestañas:**

#### Z-Score
- Detecta variaciones que salen del rango habitual
- Tabla con: puesto, indicador, variación %, z-score
- Código de colores por severidad:
  - 🔴 CRÍTICA: Z-Score > 3 (probabilidad 0.3%)
  - 🟠 ALTA: Z-Score > 2 (probabilidad 5%)
  - 🟡 MODERADA: Z-Score > 1 (probabilidad 32%)

#### Intervalo de Predicción 95%
- Detecta valores fuera del pronóstico esperado
- Usa modelo EMA con tendencia
- Muestra: valor real, límites inferior/superior, variación fuera de rango

### Mapa de Calor (`/mapa-calor`)

Visualización matricial de variaciones por negocio y puesto.

**Características:**
- Escala de colores: rojo (negativo) → blanco (neutro) → verde (positivo)
- Valores en cada celda con porcentaje de variación
- Filtro por periodo

### Tabla de Resultados (`/tabla-resultados`)

Tablas detalladas de compensaciones.

**Modos:**
- **Total**: Suma de compensaciones por puesto
- **Promedio**: Promedio por empleado

### Carga de Datos (`/carga-datos`)

Interfaz para cargar archivos DatosInteligencia al sistema.

**Características:**
- Drag & drop de archivos
- Soporte para CSV y Excel (.xlsx, .xls)
- Tres modos de carga:
  - **Reemplazar Todo**: Elimina datos existentes y carga nuevos
  - **Incremental**: Agrega/actualiza sin eliminar
  - **Por Periodo**: Reemplaza solo el periodo especificado
- Barra de progreso en tiempo real
- Resultado detallado de la operación

**Estadísticas:**
- **Totales Generales** (parte superior): Resumen global de registros, periodos y negocios
- **Estadísticas por Año** (parte inferior): Cards individuales por cada año mostrando:
  - Total de registros del año
  - Cantidad de periodos cargados
  - Negocios disponibles con tags visuales

---

## Modelos de Datos

### Compensación Nacional

```typescript
interface ResumenNacional {
  periodoActual: string;
  periodoAnterior: string;
  totalSemanaActual: number;
  totalSemanaAnterior: number;
  diferencia: number;
  variacionPorcentual: number;
  negocios: NegocioResumen[];
}
```

### Serie Temporal

```typescript
interface SerieTemporal {
  negocio: string;
  puesto: string;
  indicador: string;
  serie: PuntoSerie[];
  lineasConfianza: LineasConfianza;
}

interface PuntoSerie {
  semana: string;
  valor: number;
  variacionVsSemanaAnterior: number;
  fueraDe1Sigma: boolean;
  fueraDe15Sigma: boolean;
}

interface LineasConfianza {
  media: number;
  desviacionEstandar: number;
  limiteSuperior1Sigma: number;
  limiteInferior1Sigma: number;
  limiteSuperior15Sigma: number;
  limiteInferior15Sigma: number;
}
```

### Alertas

```typescript
interface AlertaZScore {
  negocio: string;
  puesto: string;
  indicador: string;
  variacionPorcentualVsSA: number;
  variacionMedia: number;
  limiteInferior: number;
  limiteSuperior: number;
  severidad: 'CRITICA' | 'ALTA' | 'MODERADA';
  zScore: number;
}

interface AlertaPrediccion {
  negocio: string;
  puesto: string;
  indicador: string;
  observacionReal: number;
  limiteInferior: number;
  limiteSuperior: number;
  variacionFueraRango: number;
  historiaInsuficiente: boolean;
}
```

### Estadísticas

```typescript
interface EstadisticasAnio {
  anio: number;
  totalRegistros: number;
  totalPeriodos: number;
  periodos: number[];
  totalNegocios: number;
  negocios: string[];
}

interface Totales {
  totalRegistros: number;
  totalPeriodos: number;
  totalNegocios: number;
  negocios: string[];
}

interface Estadisticas {
  estadisticasPorAnio: EstadisticasAnio[];
  totales: Totales;
}
```

### Resultado de Carga

```typescript
interface ResultadoCarga {
  success: boolean;
  mensaje: string;
  fechaCarga: string;
  usuarioCarga: string;
  archivoOriginal: string;
  totalRegistros: number;
  registrosExitosos: number;
  registrosConError: number;
  tiempoProcesamientoMs: number;
  errores: string[];
}
```

---

## Servicios

### CompensacionService

```typescript
// Endpoints disponibles:
getResumenNacional(periodo?: string): Observable<ResumenNacional>
getSerieTemporal(negocio: string, puesto: string, indicador: string): Observable<SerieTemporal>
getNegocios(): Observable<string[]>
getPuestos(negocio: string): Observable<string[]>
getIndicadores(negocio: string, puesto: string): Observable<string[]>
getPeriodos(): Observable<string[]>
getPeriodoActual(): Observable<string>
getMapaCalor(periodo?: string): Observable<MapaCalorData>
getTablaResultados(tipo: 'total' | 'promedio', periodo?: string): Observable<TablaResultados>
```

### AlertasService

```typescript
// Endpoints disponibles:
getAlertasZScore(negocio?: string, periodo?: string): Observable<AlertaZScore[]>
getAlertasPrediccion(negocio?: string, periodo?: string): Observable<AlertaPrediccion[]>
```

### AdminService

```typescript
// Endpoints disponibles:
cargarReemplazar(file: File, usuario?: string): Observable<UploadProgress>
cargarIncremental(file: File, usuario?: string): Observable<UploadProgress>
cargarPorPeriodo(file: File, periodo: number, usuario?: string): Observable<UploadProgress>
eliminarPeriodo(periodo: number, usuario?: string): Observable<ResultadoCarga>
obtenerEstadisticas(): Observable<Estadisticas>
```

---

## Configuración

### Ambientes

**Desarrollo** (`environment.ts`):
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};
```

**Producción** (`environment.prod.ts`):
```typescript
export const environment = {
  production: true,
  apiUrl: '/api/v1'
};
```

---

## Ejecución

### Desarrollo Local

```bash
# Instalar dependencias
npm install

# Servidor de desarrollo
ng serve

# Abrir http://localhost:4200
```

### Build Producción

```bash
# Build optimizado
ng build --configuration production

# Los archivos se generan en dist/
```

### Docker

```bash
# Build
docker build -t resumen-nomina-frontend .

# Run
docker run -d -p 80:80 resumen-nomina-frontend
```

---

## Gráficos

La aplicación utiliza Chart.js para visualizaciones:

### Serie Temporal
- Tipo: Línea
- Dataset principal: valores históricos
- Líneas horizontales: media, límites de confianza
- Puntos destacados: valores fuera de rango

### Dashboard
- Tipo: Barras horizontales
- Comparación semana actual vs anterior
- Colores diferenciados por negocio

### Mapa de Calor
- Tipo: Matriz de colores
- Escala: divergente (rojo-blanco-verde)
- Tooltip con valores detallados

---

## Estructura de Carpetas

```
frontend/
├── src/
│   ├── app/
│   │   ├── app.component.ts         # Componente raíz
│   │   ├── app.config.ts            # Configuración de la app
│   │   ├── app.routes.ts            # Rutas de la aplicación
│   │   ├── core/                    # Servicios y modelos
│   │   ├── features/                # Componentes de páginas
│   │   └── shared/                  # Componentes compartidos
│   ├── assets/                      # Recursos estáticos
│   ├── environments/                # Configuración de ambientes
│   ├── index.html                   # HTML principal
│   ├── main.ts                      # Punto de entrada
│   └── styles.scss                  # Estilos globales
├── angular.json                     # Configuración Angular CLI
├── package.json                     # Dependencias NPM
└── tsconfig.json                    # Configuración TypeScript
```

---

## Navegación

| Ruta | Componente | Descripción |
|------|------------|-------------|
| `/` | NacionalComponent | Página principal |
| `/nacional` | NacionalComponent | Dashboard nacional |
| `/mapa-calor` | MapaCalorComponent | Mapa de calor |
| `/tabla-resultados` | TablaResultadosComponent | Tablas de datos |
| `/alertas` | AlertasComponent | Sistema de alertas |
| `/negocio/:negocio` | NegocioComponent | Detalle por negocio |
| `/carga-datos` | CargaDatosComponent | Carga de archivos |

---

## Contacto

**Equipo:** Inteligencia de Datos - Compensalia
**Organización:** Grupo Elektra
