package com.elektra.resumennomina.api.controller;

import com.elektra.resumennomina.api.dto.*;
import com.elektra.resumennomina.api.mapper.ResponseMapper;
import com.elektra.resumennomina.domain.model.*;
import com.elektra.resumennomina.domain.port.input.CompensacionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para los endpoints de compensacion.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/compensacion")
@RequiredArgsConstructor
@Tag(name = "Compensación", description = "Endpoints para consulta de datos de compensación")
public class CompensacionController {

    private final CompensacionUseCase compensacionUseCase;
    private final ResponseMapper responseMapper;

    // ==================== VISTA NACIONAL ====================

    @GetMapping("/nacional")
    @Operation(summary = "Obtiene el resumen nacional", description = "Retorna totales y variaciones a nivel nacional")
    public ResponseEntity<ApiResponse<ResumenNacionalResponse>> obtenerResumenNacional(
            @Parameter(description = "Codigo del periodo (YYYYWW). Si no se especifica, usa el mas reciente")
            @RequestParam(required = false) Integer periodo) {

        Periodo periodoObj = periodo != null ?
                Periodo.fromCodigo(periodo) :
                compensacionUseCase.obtenerPeriodoActual();

        ResumenSemanal resumenGeneral = compensacionUseCase.obtenerResumenNacional(periodoObj);
        List<ResumenSemanal> resumenPorNegocio = compensacionUseCase.obtenerResumenNacionalPorNegocio(periodoObj);

        ResumenNacionalResponse response = responseMapper.toResumenNacionalResponse(
                periodoObj, resumenGeneral, resumenPorNegocio);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/nacional/serie")
    @Operation(summary = "Obtiene serie temporal nacional", description = "Serie historica con lineas de confianza")
    public ResponseEntity<ApiResponse<SerieTemporalResponse>> obtenerSerieNacional(
            @Parameter(description = "Periodo inicial (YYYYWW)")
            @RequestParam(required = false) Integer desde,
            @Parameter(description = "Periodo final (YYYYWW)")
            @RequestParam(required = false) Integer hasta) {

        Periodo periodoHasta = hasta != null ?
                Periodo.fromCodigo(hasta) :
                compensacionUseCase.obtenerPeriodoActual();

        Periodo periodoDesde = desde != null ?
                Periodo.fromCodigo(desde) :
                Periodo.fromCodigo(202301); // Por defecto desde 2023

        EstadisticasSerie estadisticas = compensacionUseCase.obtenerSerieNacionalTotal(periodoDesde, periodoHasta);

        SerieTemporalResponse response = responseMapper.toSerieTemporalResponse(estadisticas);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== VISTA POR NEGOCIO ====================

    @GetMapping("/negocio/{negocio}/serie")
    @Operation(summary = "Obtiene serie temporal por negocio", description = "Serie historica de un negocio especifico")
    public ResponseEntity<ApiResponse<SerieTemporalResponse>> obtenerSeriePorNegocio(
            @Parameter(description = "Nombre del negocio")
            @PathVariable String negocio,
            @RequestParam(required = false) Integer desde,
            @RequestParam(required = false) Integer hasta) {

        Periodo periodoHasta = hasta != null ?
                Periodo.fromCodigo(hasta) :
                compensacionUseCase.obtenerPeriodoActual();

        Periodo periodoDesde = desde != null ?
                Periodo.fromCodigo(desde) :
                Periodo.fromCodigo(202301);

        EstadisticasSerie estadisticas = compensacionUseCase.obtenerSeriePorNegocio(
                negocio, periodoDesde, periodoHasta);

        SerieTemporalResponse response = responseMapper.toSerieTemporalResponse(estadisticas);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/negocios/{negocio}/puestos/{puesto}/serie")
    @Operation(summary = "Obtiene serie temporal por negocio y puesto",
            description = "Serie historica de compensacion para un negocio y puesto especificos")
    public ResponseEntity<ApiResponse<SerieTemporalResponse>> obtenerSeriePorNegocioYPuesto(
            @Parameter(description = "Nombre del negocio")
            @PathVariable String negocio,
            @Parameter(description = "Nombre del puesto")
            @PathVariable String puesto,
            @RequestParam(required = false) Integer desde,
            @RequestParam(required = false) Integer hasta) {

        Periodo periodoHasta = hasta != null ?
                Periodo.fromCodigo(hasta) :
                compensacionUseCase.obtenerPeriodoActual();

        Periodo periodoDesde = desde != null ?
                Periodo.fromCodigo(desde) :
                Periodo.fromCodigo(202301);

        EstadisticasSerie estadisticas = compensacionUseCase.obtenerSeriePorNegocioYPuesto(
                negocio, puesto, periodoDesde, periodoHasta);

        SerieTemporalResponse response = responseMapper.toSerieTemporalResponse(estadisticas);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/negocios/{negocio}/puestos/{puesto}/resumen-indicadores")
    @Operation(summary = "Obtiene resumen de todos los indicadores por negocio y puesto",
            description = "Retorna todos los indicadores con valores de semana actual vs anterior")
    public ResponseEntity<ApiResponse<List<ResumenIndicador>>> obtenerResumenIndicadores(
            @Parameter(description = "Nombre del negocio")
            @PathVariable String negocio,
            @Parameter(description = "Nombre del puesto")
            @PathVariable String puesto,
            @RequestParam(required = false) Integer periodo) {

        Periodo periodoObj = periodo != null ?
                Periodo.fromCodigo(periodo) :
                compensacionUseCase.obtenerPeriodoActual();

        List<ResumenIndicador> resumenes = compensacionUseCase.obtenerResumenIndicadoresPorNegocioYPuesto(
                negocio, puesto, periodoObj);

        return ResponseEntity.ok(ApiResponse.success(resumenes));
    }

    @GetMapping("/negocios/{negocio}/puestos/{puesto}/indicadores/{indicador}/serie")
    @Operation(summary = "Obtiene serie temporal por negocio, puesto e indicador",
            description = "Serie historica filtrada por negocio, puesto e indicador especificos")
    public ResponseEntity<ApiResponse<SerieTemporalResponse>> obtenerSeriePorNegocioPuestoIndicador(
            @Parameter(description = "Nombre del negocio")
            @PathVariable String negocio,
            @Parameter(description = "Nombre del puesto")
            @PathVariable String puesto,
            @Parameter(description = "Nombre del indicador")
            @PathVariable String indicador,
            @RequestParam(required = false) Integer desde,
            @RequestParam(required = false) Integer hasta) {

        Periodo periodoHasta = hasta != null ?
                Periodo.fromCodigo(hasta) :
                compensacionUseCase.obtenerPeriodoActual();

        Periodo periodoDesde = desde != null ?
                Periodo.fromCodigo(desde) :
                Periodo.fromCodigo(202301);

        EstadisticasSerie estadisticas = compensacionUseCase.obtenerSeriePorNegocioPuestoIndicador(
                negocio, puesto, indicador, periodoDesde, periodoHasta);

        SerieTemporalResponse response = responseMapper.toSerieTemporalResponse(estadisticas);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== VISTA POR PUESTO ====================

    @GetMapping("/puesto/{funcionSap}")
    @Operation(summary = "Obtiene resumen de un puesto", description = "Resumen semanal de un puesto por funcion SAP")
    public ResponseEntity<ApiResponse<ResumenSemanal>> obtenerResumenPorPuesto(
            @Parameter(description = "Codigo de funcion SAP")
            @PathVariable Integer funcionSap,
            @RequestParam(required = false) Integer periodo) {

        Periodo periodoObj = periodo != null ?
                Periodo.fromCodigo(periodo) :
                compensacionUseCase.obtenerPeriodoActual();

        ResumenSemanal resumen = compensacionUseCase.obtenerResumenPorPuesto(funcionSap, periodoObj);

        return ResponseEntity.ok(ApiResponse.success(resumen));
    }

    @GetMapping("/puesto/{funcionSap}/serie/{indicador}")
    @Operation(summary = "Obtiene serie temporal por puesto e indicador")
    public ResponseEntity<ApiResponse<SerieTemporalResponse>> obtenerSeriePorPuestoIndicador(
            @PathVariable Integer funcionSap,
            @PathVariable String indicador,
            @RequestParam(required = false) Integer desde,
            @RequestParam(required = false) Integer hasta) {

        Periodo periodoHasta = hasta != null ?
                Periodo.fromCodigo(hasta) :
                compensacionUseCase.obtenerPeriodoActual();

        Periodo periodoDesde = desde != null ?
                Periodo.fromCodigo(desde) :
                Periodo.fromCodigo(202301);

        EstadisticasSerie estadisticas = compensacionUseCase.obtenerSeriePorPuestoIndicador(
                funcionSap, indicador, periodoDesde, periodoHasta);

        SerieTemporalResponse response = responseMapper.toSerieTemporalResponse(estadisticas);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== MAPA DE CALOR ====================

    @GetMapping("/mapa-calor")
    @Operation(summary = "Obtiene datos del mapa de calor", description = "Variaciones por puesto agrupadas por negocio")
    public ResponseEntity<ApiResponse<MapaCalorResponse>> obtenerMapaCalor(
            @RequestParam(required = false) Integer periodo) {

        Periodo periodoObj = periodo != null ?
                Periodo.fromCodigo(periodo) :
                compensacionUseCase.obtenerPeriodoActual();

        List<MapaCalorItem> items = compensacionUseCase.obtenerMapaCalor(periodoObj);

        MapaCalorResponse response = responseMapper.toMapaCalorResponse(periodoObj, items);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== TABLA DE RESULTADOS ====================

    @GetMapping("/tabla-resultados/total")
    @Operation(summary = "Obtiene tabla de resultados con totales")
    public ResponseEntity<ApiResponse<TablaResultadosResponse>> obtenerTablaResultadosTotal(
            @RequestParam(required = false) Integer periodo) {

        Periodo periodoObj = periodo != null ?
                Periodo.fromCodigo(periodo) :
                compensacionUseCase.obtenerPeriodoActual();

        List<ResumenSemanal> datos = compensacionUseCase.obtenerTablaResultadosTotal(periodoObj);

        TablaResultadosResponse response = responseMapper.toTablaResultadosResponse(
                periodoObj, datos, TablaResultadosResponse.TipoTabla.TOTAL);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tabla-resultados/promedio")
    @Operation(summary = "Obtiene tabla de resultados con promedios")
    public ResponseEntity<ApiResponse<TablaResultadosResponse>> obtenerTablaResultadosPromedio(
            @RequestParam(required = false) Integer periodo) {

        Periodo periodoObj = periodo != null ?
                Periodo.fromCodigo(periodo) :
                compensacionUseCase.obtenerPeriodoActual();

        List<ResumenSemanal> datos = compensacionUseCase.obtenerTablaResultadosPromedio(periodoObj);

        TablaResultadosResponse response = responseMapper.toTablaResultadosResponse(
                periodoObj, datos, TablaResultadosResponse.TipoTabla.PROMEDIO);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== CATALOGOS ====================

    @GetMapping("/negocios")
    @Operation(summary = "Lista de negocios disponibles")
    public ResponseEntity<ApiResponse<List<String>>> obtenerNegocios() {
        List<String> negocios = compensacionUseCase.obtenerNegocios();
        return ResponseEntity.ok(ApiResponse.success(negocios));
    }

    @GetMapping("/negocios/{negocio}/puestos")
    @Operation(summary = "Lista de puestos por negocio")
    public ResponseEntity<ApiResponse<List<String>>> obtenerPuestosPorNegocio(
            @PathVariable String negocio) {
        List<String> puestos = compensacionUseCase.obtenerPuestosPorNegocio(negocio);
        return ResponseEntity.ok(ApiResponse.success(puestos));
    }

    @GetMapping("/indicadores")
    @Operation(summary = "Lista de indicadores disponibles")
    public ResponseEntity<ApiResponse<List<String>>> obtenerIndicadores() {
        List<String> indicadores = compensacionUseCase.obtenerIndicadores();
        return ResponseEntity.ok(ApiResponse.success(indicadores));
    }

    @GetMapping("/negocios/{negocio}/indicadores")
    @Operation(summary = "Lista de indicadores por negocio")
    public ResponseEntity<ApiResponse<List<String>>> obtenerIndicadoresPorNegocio(
            @PathVariable String negocio) {
        List<String> indicadores = compensacionUseCase.obtenerIndicadoresPorNegocio(negocio);
        return ResponseEntity.ok(ApiResponse.success(indicadores));
    }

    @GetMapping("/negocios/{negocio}/puestos/{puesto}/indicadores")
    @Operation(summary = "Lista de indicadores por negocio y puesto")
    public ResponseEntity<ApiResponse<List<String>>> obtenerIndicadoresPorNegocioYPuesto(
            @PathVariable String negocio,
            @PathVariable String puesto) {
        List<String> indicadores = compensacionUseCase.obtenerIndicadoresPorNegocioYPuesto(negocio, puesto);
        return ResponseEntity.ok(ApiResponse.success(indicadores));
    }

    @GetMapping("/periodos")
    @Operation(summary = "Lista de periodos disponibles")
    public ResponseEntity<ApiResponse<List<String>>> obtenerPeriodos() {
        List<Periodo> periodos = compensacionUseCase.obtenerPeriodosDisponibles();
        List<String> periodosStr = periodos.stream()
                .map(Periodo::toDisplayString)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(periodosStr));
    }

    @GetMapping("/periodo-actual")
    @Operation(summary = "Periodo mas reciente disponible")
    public ResponseEntity<ApiResponse<String>> obtenerPeriodoActual() {
        Periodo periodo = compensacionUseCase.obtenerPeriodoActual();
        return ResponseEntity.ok(ApiResponse.success(periodo.toDisplayString()));
    }
}
