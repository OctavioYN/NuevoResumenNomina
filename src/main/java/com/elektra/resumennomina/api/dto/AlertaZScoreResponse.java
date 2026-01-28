package com.elektra.resumennomina.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de respuesta para alertas Z-Score.
 */
@Data
@Builder
public class AlertaZScoreResponse {

    private String negocio;
    private String puesto;
    private String indicador;

    /** Variación porcentual vs semana anterior (%) */
    private BigDecimal variacionPorcentualVsSA;

    /** Media histórica de variaciones (%) */
    private BigDecimal variacionMedia;

    /** Límite inferior del umbral dinámico (%) */
    private BigDecimal limiteInferior;

    /** Límite superior del umbral dinámico (%) */
    private BigDecimal limiteSuperior;

    /** Z-Score calculado */
    private BigDecimal zScore;

    /** Severidad: CRITICA, ALTA, MODERADA */
    private String severidad;
}
