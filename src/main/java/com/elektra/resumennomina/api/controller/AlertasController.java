package com.elektra.resumennomina.api.controller;

import com.elektra.resumennomina.api.dto.*;
import com.elektra.resumennomina.application.service.AlertasService;
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
 * Controlador REST para los endpoints de alertas estadísticas.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Endpoints para consulta de alertas estadísticas (Z-Score e Intervalo de Predicción)")
public class AlertasController {

    private final AlertasService alertasService;
    private final CompensacionUseCase compensacionUseCase;

    @GetMapping("/zscore")
    @Operation(summary = "Obtiene alertas Z-Score",
            description = "Retorna alertas cuando la variación semanal sale del rango habitual")
    public ResponseEntity<ApiResponse<List<AlertaZScoreResponse>>> obtenerAlertasZScore(
            @Parameter(description = "Codigo del periodo (YYYYWW). Si no se especifica, usa el más reciente")
            @RequestParam(required = false) Integer periodo) {

        Periodo periodoObj = periodo != null ?
                Periodo.fromCodigo(periodo) :
                compensacionUseCase.obtenerPeriodoActual();

        List<AlertaZScore> alertas = alertasService.obtenerAlertasZScore(periodoObj);

        List<AlertaZScoreResponse> response = alertas.stream()
                .map(this::toZScoreResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/prediccion")
    @Operation(summary = "Obtiene alertas de Intervalo de Predicción 95%",
            description = "Retorna alertas cuando el valor observado está fuera del intervalo de predicción ARIMA")
    public ResponseEntity<ApiResponse<List<AlertaPrediccionResponse>>> obtenerAlertasPrediccion(
            @Parameter(description = "Codigo del periodo (YYYYWW). Si no se especifica, usa el más reciente")
            @RequestParam(required = false) Integer periodo) {

        Periodo periodoObj = periodo != null ?
                Periodo.fromCodigo(periodo) :
                compensacionUseCase.obtenerPeriodoActual();

        List<AlertaPrediccion> alertas = alertasService.obtenerAlertasPrediccion(periodoObj);

        List<AlertaPrediccionResponse> response = alertas.stream()
                .map(this::toPrediccionResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Obtiene todas las alertas",
            description = "Retorna tanto alertas Z-Score como de Intervalo de Predicción")
    public ResponseEntity<ApiResponse<AlertasResponse>> obtenerTodasLasAlertas(
            @Parameter(description = "Codigo del periodo (YYYYWW). Si no se especifica, usa el más reciente")
            @RequestParam(required = false) Integer periodo) {

        Periodo periodoObj = periodo != null ?
                Periodo.fromCodigo(periodo) :
                compensacionUseCase.obtenerPeriodoActual();

        List<AlertaZScore> alertasZScore = alertasService.obtenerAlertasZScore(periodoObj);
        List<AlertaPrediccion> alertasPrediccion = alertasService.obtenerAlertasPrediccion(periodoObj);

        AlertasResponse response = AlertasResponse.builder()
                .periodo(periodoObj.toString())
                .totalAlertasZScore(alertasZScore.size())
                .totalAlertasPrediccion(alertasPrediccion.size())
                .alertasZScore(alertasZScore.stream().map(this::toZScoreResponse).toList())
                .alertasPrediccion(alertasPrediccion.stream().map(this::toPrediccionResponse).toList())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private AlertaZScoreResponse toZScoreResponse(AlertaZScore alerta) {
        return AlertaZScoreResponse.builder()
                .negocio(alerta.getNegocio())
                .puesto(alerta.getPuesto())
                .indicador(alerta.getIndicador())
                .variacionPorcentualVsSA(alerta.getVariacionPorcentual())
                .variacionMedia(alerta.getVariacionMedia())
                .limiteInferior(alerta.getLimiteInferior())
                .limiteSuperior(alerta.getLimiteSuperior())
                .zScore(alerta.getZScore())
                .severidad(alerta.getSeveridad().name())
                .build();
    }

    private AlertaPrediccionResponse toPrediccionResponse(AlertaPrediccion alerta) {
        return AlertaPrediccionResponse.builder()
                .negocio(alerta.getNegocio())
                .puesto(alerta.getPuesto())
                .indicador(alerta.getIndicador())
                .observacionReal(alerta.getObservacionReal())
                .limiteInferior(alerta.getLimiteInferior())
                .limiteSuperior(alerta.getLimiteSuperior())
                .variacionFueraRango(alerta.getVariacionFueraRango())
                .historiaInsuficiente(alerta.isHistoriaInsuficiente())
                .build();
    }
}
