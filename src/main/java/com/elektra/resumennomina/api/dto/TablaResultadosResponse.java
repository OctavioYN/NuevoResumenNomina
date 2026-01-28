package com.elektra.resumennomina.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO para la respuesta de la tabla de resultados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TablaResultadosResponse {

    private String periodoActual;
    private String periodoAnterior;
    private TipoTabla tipo; // TOTAL o PROMEDIO

    // Filas agrupadas por negocio
    private Map<String, List<FilaResultado>> filasPorNegocio;

    // Lista plana
    private List<FilaResultado> filas;

    public enum TipoTabla {
        TOTAL, PROMEDIO
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilaResultado {
        private String puesto;
        private String negocio;
        private BigDecimal semanaActual;
        private BigDecimal semanaAnterior;
        private BigDecimal variacionMonto;
        private BigDecimal variacionPorcentual;
        private Boolean variacionPositiva;
    }
}
