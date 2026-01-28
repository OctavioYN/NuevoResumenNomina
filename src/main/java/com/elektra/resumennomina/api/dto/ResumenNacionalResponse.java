package com.elektra.resumennomina.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la respuesta de la vista Nacional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumenNacionalResponse {

    private String periodoActual;
    private String periodoAnterior;

    // Totales generales
    private BigDecimal totalSemanaActual;
    private BigDecimal totalSemanaAnterior;
    private BigDecimal diferencia;
    private BigDecimal variacionPorcentual;

    // Desglose por negocio
    private List<ResumenNegocioDto> negocios;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenNegocioDto {
        private String negocio;
        private String slug;
        private BigDecimal semanaActual;
        private BigDecimal semanaAnterior;
        private BigDecimal diferencia;
        private BigDecimal variacionPorcentual;
        private Integer empleados;
        private List<String> puestos; // Para la nota de puestos que conforman el negocio
    }
}
