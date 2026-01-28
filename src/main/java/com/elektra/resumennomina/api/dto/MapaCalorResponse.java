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
 * DTO para la respuesta del mapa de calor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MapaCalorResponse {

    private String periodoActual;
    private String periodoAnterior;

    // Items agrupados por negocio
    private Map<String, List<ItemMapaCalor>> itemsPorNegocio;

    // Lista plana para flexibilidad
    private List<ItemMapaCalor> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemMapaCalor {
        private String negocio;
        private String puesto;
        private BigDecimal variacionPorcentual;
        private BigDecimal valorAbsoluto;
        private Boolean esPositivo;
        private Double intensidadColor; // 0 a 1 para determinar opacidad/tamaño

        // Para tooltip
        private BigDecimal valorSemanaActual;
        private BigDecimal valorSemanaAnterior;
    }
}
