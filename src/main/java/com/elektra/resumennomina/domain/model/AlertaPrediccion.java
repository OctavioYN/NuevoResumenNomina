package com.elektra.resumennomina.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Modelo de dominio para alertas basadas en intervalo de predicción 95%.
 * Detecta outliers cuando el valor observado está fuera del intervalo de predicción ARIMA.
 */
@Data
@Builder
public class AlertaPrediccion {

    private String negocio;
    private String puesto;
    private String indicador;

    /** Valor observado (real) del periodo actual */
    private BigDecimal observacionReal;

    /** Valor pronosticado por el modelo */
    private BigDecimal pronostico;

    /** Límite inferior del intervalo de predicción 95% */
    private BigDecimal limiteInferior;

    /** Límite superior del intervalo de predicción 95% */
    private BigDecimal limiteSuperior;

    /** Variación fuera del rango (% que excede el intervalo) */
    private BigDecimal variacionFueraRango;

    /** Indica si el puesto tiene suficiente historia para el modelo */
    private boolean historiaInsuficiente;

    /** Periodo evaluado */
    private Periodo periodo;
}
