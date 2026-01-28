package com.elektra.resumennomina.application.service;

import com.elektra.resumennomina.domain.model.EstadisticasSerie;
import com.elektra.resumennomina.domain.model.Periodo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Servicio para calculos estadisticos.
 * Calcula media, desviacion estandar y lineas de confianza.
 */
@Slf4j
@Service
public class EstadisticasService {

    /**
     * Calcula las estadisticas de una serie de valores
     */
    public EstadisticasSerie calcularEstadisticas(
            Map<Periodo, BigDecimal> serieValores,
            String negocio,
            String puesto,
            String indicador) {

        if (serieValores == null || serieValores.isEmpty()) {
            return EstadisticasSerie.builder()
                    .negocio(negocio)
                    .puesto(puesto)
                    .indicador(indicador)
                    .cantidadPuntos(0)
                    .build();
        }

        // Usar Apache Commons Math para calculos estadisticos
        DescriptiveStatistics stats = new DescriptiveStatistics();

        serieValores.values().stream()
                .filter(v -> v != null)
                .forEach(v -> stats.addValue(v.doubleValue()));

        if (stats.getN() == 0) {
            return EstadisticasSerie.builder()
                    .negocio(negocio)
                    .puesto(puesto)
                    .indicador(indicador)
                    .cantidadPuntos(0)
                    .build();
        }

        BigDecimal media = BigDecimal.valueOf(stats.getMean())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal desviacionEstandar = BigDecimal.valueOf(stats.getStandardDeviation())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal minimo = BigDecimal.valueOf(stats.getMin())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal maximo = BigDecimal.valueOf(stats.getMax())
                .setScale(2, RoundingMode.HALF_UP);

        // Calcular lineas de confianza
        BigDecimal sigma = desviacionEstandar;
        BigDecimal sigma15 = sigma.multiply(BigDecimal.valueOf(1.5));

        // Crear puntos de la serie con variacion vs semana anterior
        List<EstadisticasSerie.PuntoSerie> puntos = crearPuntosSerie(serieValores);

        EstadisticasSerie estadisticas = EstadisticasSerie.builder()
                .negocio(negocio)
                .puesto(puesto)
                .indicador(indicador)
                .media(media)
                .desviacionEstandar(desviacionEstandar)
                .minimo(minimo)
                .maximo(maximo)
                .cantidadPuntos((int) stats.getN())
                .limiteSuperior1Sigma(media.add(sigma))
                .limiteInferior1Sigma(media.subtract(sigma))
                .limiteSuperior15Sigma(media.add(sigma15))
                .limiteInferior15Sigma(media.subtract(sigma15))
                .puntos(puntos)
                .build();

        log.debug("Estadisticas calculadas para {}/{}/{}: media={}, desv={}, n={}",
                negocio, puesto, indicador, media, desviacionEstandar, stats.getN());

        return estadisticas;
    }

    /**
     * Crea la lista de puntos de la serie con variacion vs semana anterior
     */
    private List<EstadisticasSerie.PuntoSerie> crearPuntosSerie(Map<Periodo, BigDecimal> serieValores) {
        List<Periodo> periodosOrdenados = serieValores.keySet().stream()
                .sorted()
                .toList();

        return periodosOrdenados.stream()
                .map(periodo -> {
                    BigDecimal valorActual = serieValores.get(periodo);
                    BigDecimal valorAnterior = serieValores.get(periodo.anterior());

                    BigDecimal variacion = calcularVariacionPorcentual(valorActual, valorAnterior);

                    return EstadisticasSerie.PuntoSerie.builder()
                            .periodo(periodo)
                            .valor(valorActual)
                            .variacionVsSemanaAnterior(variacion)
                            .build();
                })
                .toList();
    }

    /**
     * Calcula la variacion porcentual: (actual / anterior) - 1
     */
    public BigDecimal calcularVariacionPorcentual(BigDecimal valorActual, BigDecimal valorAnterior) {
        if (valorActual == null || valorAnterior == null ||
            valorAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return valorActual
                .divide(valorAnterior, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la diferencia entre dos valores
     */
    public BigDecimal calcularDiferencia(BigDecimal valorActual, BigDecimal valorAnterior) {
        if (valorActual == null) return BigDecimal.ZERO;
        if (valorAnterior == null) return valorActual;
        return valorActual.subtract(valorAnterior);
    }
}
