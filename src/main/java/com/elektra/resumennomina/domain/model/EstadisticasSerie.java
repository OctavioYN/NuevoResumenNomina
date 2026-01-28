package com.elektra.resumennomina.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contiene las estadisticas de una serie de tiempo.
 * Incluye media, desviacion estandar y lineas de confianza.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasSerie {

    private String negocio;
    private String puesto;
    private String indicador;

    // Estadisticas basicas
    private BigDecimal media;
    private BigDecimal desviacionEstandar;
    private BigDecimal minimo;
    private BigDecimal maximo;
    private int cantidadPuntos;

    // Lineas de confianza
    private BigDecimal limiteSuperior1Sigma;    // Media + 1 desv. std (rojo claro)
    private BigDecimal limiteInferior1Sigma;    // Media - 1 desv. std (rojo claro)
    private BigDecimal limiteSuperior15Sigma;   // Media + 1.5 desv. std (rojo intenso)
    private BigDecimal limiteInferior15Sigma;   // Media - 1.5 desv. std (rojo intenso)

    // Serie de datos
    private List<PuntoSerie> puntos;

    /**
     * Calcula las lineas de confianza basadas en la media y desviacion estandar
     */
    public void calcularLineasConfianza() {
        if (media == null || desviacionEstandar == null) {
            return;
        }

        BigDecimal sigma = desviacionEstandar;
        BigDecimal sigma15 = sigma.multiply(BigDecimal.valueOf(1.5));

        this.limiteSuperior1Sigma = media.add(sigma);
        this.limiteInferior1Sigma = media.subtract(sigma);
        this.limiteSuperior15Sigma = media.add(sigma15);
        this.limiteInferior15Sigma = media.subtract(sigma15);
    }

    /**
     * Representa un punto en la serie de tiempo
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PuntoSerie {
        private Periodo periodo;
        private BigDecimal valor;
        private BigDecimal variacionVsSemanaAnterior;

        /**
         * Verifica si el punto esta fuera de 1 desviacion estandar
         */
        public boolean estaFuera1Sigma(BigDecimal limiteSuperior, BigDecimal limiteInferior) {
            if (valor == null) return false;
            return valor.compareTo(limiteSuperior) > 0 || valor.compareTo(limiteInferior) < 0;
        }

        /**
         * Verifica si el punto esta fuera de 1.5 desviaciones estandar
         */
        public boolean estaFuera15Sigma(BigDecimal limiteSuperior, BigDecimal limiteInferior) {
            if (valor == null) return false;
            return valor.compareTo(limiteSuperior) > 0 || valor.compareTo(limiteInferior) < 0;
        }
    }
}
