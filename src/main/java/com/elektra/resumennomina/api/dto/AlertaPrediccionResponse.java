package com.elektra.resumennomina.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de respuesta para alertas de intervalo de predicción 95%.
 */
@Data
@Builder
public class AlertaPrediccionResponse {

    private String negocio;
    private String puesto;
    private String indicador;

    /** Valor observado (real) del periodo actual */
    private BigDecimal observacionReal;

    /** Límite inferior del intervalo de predicción 95% */
    private BigDecimal limiteInferior;

    /** Límite superior del intervalo de predicción 95% */
    private BigDecimal limiteSuperior;

    /** Variación fuera del rango (%) */
    private BigDecimal variacionFueraRango;

    /** Indica si tiene historia insuficiente (*) */
    private boolean historiaInsuficiente;
}
