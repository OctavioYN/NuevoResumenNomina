package com.elektra.resumennomina.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Modelo de dominio para alertas basadas en Z-Score.
 * Detecta outliers cuando la variación semanal sale del rango habitual.
 */
@Data
@Builder
public class AlertaZScore {

    private String negocio;
    private String puesto;
    private String indicador;

    /** Variación porcentual vs semana anterior */
    private BigDecimal variacionPorcentual;

    /** Media histórica de variaciones */
    private BigDecimal variacionMedia;

    /** Desviación estándar de variaciones */
    private BigDecimal desviacionEstandar;

    /** Límite inferior del umbral dinámico */
    private BigDecimal limiteInferior;

    /** Límite superior del umbral dinámico */
    private BigDecimal limiteSuperior;

    /** Z-Score calculado */
    private BigDecimal zScore;

    /** Severidad de la alerta: CRITICA, ALTA, MODERADA */
    private Severidad severidad;

    /** Periodo evaluado */
    private Periodo periodo;

    public enum Severidad {
        CRITICA,  // z-score > 3 (0.3% probabilidad)
        ALTA,     // z-score > 2 (5% probabilidad)
        MODERADA  // z-score > 1
    }
}
