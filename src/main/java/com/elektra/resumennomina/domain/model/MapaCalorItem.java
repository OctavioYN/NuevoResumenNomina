package com.elektra.resumennomina.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Representa un item del mapa de calor.
 * Cada item muestra la variacion porcentual de un puesto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapaCalorItem {

    private String negocio;
    private String puesto;
    private BigDecimal variacionPorcentual;
    private BigDecimal valorAbsoluto;  // Para determinar el tamaño del recuadro

    // Valores de semanas para tooltip
    private BigDecimal valorSemanaActual;
    private BigDecimal valorSemanaAnterior;
    private Periodo periodoActual;
    private Periodo periodoAnterior;

    /**
     * Determina si la variacion es positiva
     */
    public boolean esPositiva() {
        return variacionPorcentual != null && variacionPorcentual.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Obtiene el valor absoluto de la variacion (para tamaño del recuadro)
     */
    public BigDecimal getValorAbsoluto() {
        if (valorAbsoluto == null && variacionPorcentual != null) {
            valorAbsoluto = variacionPorcentual.abs();
        }
        return valorAbsoluto;
    }

    /**
     * Determina la intensidad del color basado en la magnitud de la variacion
     * Retorna un valor entre 0 y 1
     */
    public double getIntensidadColor() {
        if (variacionPorcentual == null) return 0;

        BigDecimal abs = variacionPorcentual.abs();

        // Escala: 0% = 0.2, 10% o mas = 1.0
        double intensidad = 0.2 + (abs.doubleValue() / 10.0) * 0.8;
        return Math.min(1.0, intensidad);
    }
}
