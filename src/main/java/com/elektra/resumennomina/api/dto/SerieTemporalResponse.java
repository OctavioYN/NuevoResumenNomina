package com.elektra.resumennomina.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para respuestas de series temporales con lineas de confianza.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SerieTemporalResponse {

    private String negocio;
    private String puesto;
    private String indicador;

    // Estadisticas
    private BigDecimal media;
    private BigDecimal desviacionEstandar;
    private BigDecimal minimo;
    private BigDecimal maximo;
    private Integer cantidadPuntos;

    // Lineas de confianza
    private LineasConfianza lineasConfianza;

    // Serie de datos
    private List<PuntoSerieDto> serie;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineasConfianza {
        private BigDecimal limiteSuperior1Sigma;    // Rojo claro
        private BigDecimal limiteInferior1Sigma;    // Rojo claro
        private BigDecimal limiteSuperior15Sigma;   // Rojo intenso
        private BigDecimal limiteInferior15Sigma;   // Rojo intenso
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PuntoSerieDto {
        private String semana;
        private BigDecimal valor;
        private BigDecimal variacionVsSemanaAnterior;
        private Boolean fueraDe1Sigma;
        private Boolean fueraDe15Sigma;
    }
}
