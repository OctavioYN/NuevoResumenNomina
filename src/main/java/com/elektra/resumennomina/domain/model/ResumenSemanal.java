package com.elektra.resumennomina.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Representa el resumen de compensacion para una semana especifica.
 * Incluye totales, promedios y variaciones.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenSemanal {

    private Periodo periodo;
    private String negocio;
    private String puesto;

    // Valores de la semana actual
    private BigDecimal totalCompensacion;
    private BigDecimal promedioCompensacion;
    private Integer cantidadEmpleados;

    // Valores de la semana anterior (para calculo de variaciones)
    private BigDecimal totalAnterior;
    private BigDecimal promedioAnterior;
    private Integer empleadosAnterior;

    // Variaciones calculadas
    private BigDecimal diferenciaMonto;
    private BigDecimal variacionPorcentual;

    /**
     * Calcula la diferencia de monto respecto a la semana anterior
     */
    public BigDecimal calcularDiferenciaMonto() {
        if (totalCompensacion == null || totalAnterior == null) {
            return BigDecimal.ZERO;
        }
        this.diferenciaMonto = totalCompensacion.subtract(totalAnterior);
        return this.diferenciaMonto;
    }

    /**
     * Calcula la variacion porcentual: (actual / anterior) - 1
     */
    public BigDecimal calcularVariacionPorcentual() {
        if (totalCompensacion == null || totalAnterior == null ||
            totalAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        this.variacionPorcentual = totalCompensacion
                .divide(totalAnterior, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return this.variacionPorcentual;
    }

    /**
     * Calcula el promedio de compensacion
     */
    public BigDecimal calcularPromedio() {
        if (totalCompensacion == null || cantidadEmpleados == null ||
            cantidadEmpleados == 0) {
            return BigDecimal.ZERO;
        }

        this.promedioCompensacion = totalCompensacion
                .divide(BigDecimal.valueOf(cantidadEmpleados), 2, RoundingMode.HALF_UP);

        return this.promedioCompensacion;
    }
}
