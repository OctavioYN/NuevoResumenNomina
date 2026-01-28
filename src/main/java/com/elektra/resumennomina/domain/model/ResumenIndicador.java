package com.elektra.resumennomina.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Representa el resumen de un indicador específico con valores actuales y anteriores.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenIndicador {

    private String indicador;
    private BigDecimal valorActual;
    private BigDecimal valorAnterior;
    private BigDecimal diferencia;
    private BigDecimal variacionPorcentual;
    private boolean variacionPositiva;
}
