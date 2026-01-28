package com.elektra.resumennomina.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO de respuesta para el conjunto de alertas.
 */
@Data
@Builder
public class AlertasResponse {

    /** Periodo evaluado */
    private String periodo;

    /** Total de alertas Z-Score */
    private int totalAlertasZScore;

    /** Total de alertas de predicción */
    private int totalAlertasPrediccion;

    /** Lista de alertas Z-Score */
    private List<AlertaZScoreResponse> alertasZScore;

    /** Lista de alertas de predicción */
    private List<AlertaPrediccionResponse> alertasPrediccion;
}
