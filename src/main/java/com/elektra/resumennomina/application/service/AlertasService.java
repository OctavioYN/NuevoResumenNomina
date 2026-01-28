package com.elektra.resumennomina.application.service;

import com.elektra.resumennomina.domain.model.*;
import com.elektra.resumennomina.domain.port.output.DatoCompensacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generación de alertas estadísticas.
 * Implementa detección de outliers mediante Z-Score e intervalos de predicción.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertasService {

    private final DatoCompensacionRepository repository;
    private static final String INDICADOR_EMPLEADO = "Empleado";
    private static final int MIN_PERIODOS_HISTORIA = 10;

    // ==================== ALERTAS Z-SCORE ====================

    /**
     * Obtiene alertas Z-Score para todos los puestos e indicadores.
     * Se activa cuando la variación semanal sale del rango habitual.
     */
    public List<AlertaZScore> obtenerAlertasZScore(Periodo periodo) {
        log.info("Calculando alertas Z-Score para periodo: {}", periodo);

        List<AlertaZScore> alertas = new ArrayList<>();
        List<String> negocios = repository.findDistinctNegocios();

        for (String negocio : negocios) {
            List<String> puestos = repository.findDistinctPuestosByNegocio(negocio);

            for (String puesto : puestos) {
                List<String> indicadores = repository.findDistinctIndicadoresByNegocioAndPuesto(negocio, puesto);

                for (String indicador : indicadores) {
                    if (INDICADOR_EMPLEADO.equalsIgnoreCase(indicador.trim())) {
                        continue; // Excluir indicador de empleados
                    }

                    try {
                        AlertaZScore alerta = calcularAlertaZScore(negocio, puesto, indicador, periodo);
                        if (alerta != null) {
                            alertas.add(alerta);
                        }
                    } catch (Exception e) {
                        log.warn("Error calculando Z-Score para {}/{}/{}: {}",
                                negocio, puesto, indicador, e.getMessage());
                    }
                }
            }
        }

        // Ordenar por severidad (CRITICA primero) y luego por Z-Score descendente
        alertas.sort(Comparator
                .comparing(AlertaZScore::getSeveridad)
                .thenComparing(a -> a.getZScore().abs(), Comparator.reverseOrder()));

        log.info("Se encontraron {} alertas Z-Score", alertas.size());
        return alertas;
    }

    private AlertaZScore calcularAlertaZScore(String negocio, String puesto, String indicador, Periodo periodo) {
        // Obtener serie histórica (últimos 52 periodos aproximadamente)
        Periodo desde = calcularPeriodoInicio(periodo, 52);
        List<DatoCompensacion> datos = repository.findByNegocioAndPuestoAndIndicadorAndPeriodoBetween(
                negocio, puesto, indicador, desde.toCodigo(), periodo.toCodigo());

        if (datos.size() < MIN_PERIODOS_HISTORIA) {
            return null; // Historia insuficiente
        }

        // Agrupar por periodo y calcular promedios
        Map<Periodo, BigDecimal> valoresPorPeriodo = datos.stream()
                .collect(Collectors.groupingBy(
                        DatoCompensacion::getPeriodo,
                        Collectors.reducing(BigDecimal.ZERO, DatoCompensacion::getValor, BigDecimal::add)
                ));

        // Ordenar periodos
        List<Periodo> periodosOrdenados = valoresPorPeriodo.keySet().stream()
                .sorted()
                .toList();

        if (periodosOrdenados.size() < 3) {
            return null;
        }

        // Calcular variaciones porcentuales entre periodos consecutivos
        List<BigDecimal> variaciones = new ArrayList<>();
        for (int i = 1; i < periodosOrdenados.size(); i++) {
            BigDecimal valorAnterior = valoresPorPeriodo.get(periodosOrdenados.get(i - 1));
            BigDecimal valorActual = valoresPorPeriodo.get(periodosOrdenados.get(i));

            if (valorAnterior.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal variacion = valorActual.subtract(valorAnterior)
                        .divide(valorAnterior, 6, RoundingMode.HALF_UP);
                variaciones.add(variacion);
            }
        }

        if (variaciones.size() < 2) {
            return null;
        }

        // La última variación es la actual (del periodo evaluado)
        BigDecimal variacionActual = variaciones.get(variaciones.size() - 1);

        // Excluir primera y última variación para análisis histórico
        List<BigDecimal> variacionesHistoricas = variaciones.subList(0, variaciones.size() - 1);

        if (variacionesHistoricas.isEmpty()) {
            return null;
        }

        // Calcular media y desviación estándar de variaciones históricas
        BigDecimal media = calcularMedia(variacionesHistoricas);
        BigDecimal desviacion = calcularDesviacionEstandar(variacionesHistoricas, media);

        if (desviacion.compareTo(BigDecimal.ZERO) == 0) {
            desviacion = new BigDecimal("0.01"); // Evitar división por cero
        }

        // Calcular umbrales dinámicos según volatilidad
        BigDecimal margenDinamico = calcularMargenDinamico(desviacion);
        BigDecimal limiteSuperior = media.add(margenDinamico);
        BigDecimal limiteInferior = media.subtract(margenDinamico);

        // Calcular Z-Score
        BigDecimal zScore = variacionActual.subtract(media)
                .divide(desviacion, 4, RoundingMode.HALF_UP)
                .abs();

        // Validar si es alerta (3 condiciones simultáneas)
        boolean superaUmbrales = variacionActual.compareTo(limiteSuperior) > 0 ||
                variacionActual.compareTo(limiteInferior) < 0;
        boolean diferenciaSignificativa = variacionActual.subtract(media).abs()
                .compareTo(new BigDecimal("0.01")) > 0;
        boolean zScoreSignificativo = zScore.compareTo(BigDecimal.ONE) > 0;

        if (!superaUmbrales || !diferenciaSignificativa || !zScoreSignificativo) {
            return null; // No es una alerta válida
        }

        // Clasificar severidad
        AlertaZScore.Severidad severidad = clasificarSeveridad(zScore);

        return AlertaZScore.builder()
                .negocio(negocio.trim())
                .puesto(puesto.trim())
                .indicador(indicador.trim())
                .variacionPorcentual(variacionActual.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP))
                .variacionMedia(media.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP))
                .desviacionEstandar(desviacion.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP))
                .limiteInferior(limiteInferior.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP))
                .limiteSuperior(limiteSuperior.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP))
                .zScore(zScore.setScale(2, RoundingMode.HALF_UP))
                .severidad(severidad)
                .periodo(periodo)
                .build();
    }

    private BigDecimal calcularMargenDinamico(BigDecimal desviacionEstandar) {
        BigDecimal margen;

        // Estrategia adaptativa según volatilidad
        if (desviacionEstandar.compareTo(new BigDecimal("0.01")) < 0) {
            // Series muy estables: margen fijo de 1.5%
            margen = new BigDecimal("0.015");
        } else if (desviacionEstandar.compareTo(new BigDecimal("0.05")) < 0) {
            // Volatilidad media: factor de 1.3×σ
            margen = desviacionEstandar.multiply(new BigDecimal("1.3"));
        } else {
            // Alta volatilidad: usa σ directamente
            margen = desviacionEstandar;
        }

        // Acotar entre 1% y 20%
        BigDecimal minMargen = new BigDecimal("0.01");
        BigDecimal maxMargen = new BigDecimal("0.20");

        return margen.max(minMargen).min(maxMargen);
    }

    private AlertaZScore.Severidad clasificarSeveridad(BigDecimal zScore) {
        if (zScore.compareTo(new BigDecimal("3")) > 0) {
            return AlertaZScore.Severidad.CRITICA;
        } else if (zScore.compareTo(new BigDecimal("2")) > 0) {
            return AlertaZScore.Severidad.ALTA;
        } else {
            return AlertaZScore.Severidad.MODERADA;
        }
    }

    // ==================== ALERTAS INTERVALO DE PREDICCIÓN ====================

    /**
     * Obtiene alertas de intervalo de predicción 95% para todos los puestos e indicadores.
     * Usa modelo simplificado basado en media móvil y error estándar.
     */
    public List<AlertaPrediccion> obtenerAlertasPrediccion(Periodo periodo) {
        log.info("Calculando alertas de predicción para periodo: {}", periodo);

        List<AlertaPrediccion> alertas = new ArrayList<>();
        List<String> negocios = repository.findDistinctNegocios();

        for (String negocio : negocios) {
            List<String> puestos = repository.findDistinctPuestosByNegocio(negocio);

            for (String puesto : puestos) {
                List<String> indicadores = repository.findDistinctIndicadoresByNegocioAndPuesto(negocio, puesto);

                for (String indicador : indicadores) {
                    if (INDICADOR_EMPLEADO.equalsIgnoreCase(indicador.trim())) {
                        continue;
                    }

                    try {
                        AlertaPrediccion alerta = calcularAlertaPrediccion(negocio, puesto, indicador, periodo);
                        if (alerta != null) {
                            alertas.add(alerta);
                        }
                    } catch (Exception e) {
                        log.warn("Error calculando predicción para {}/{}/{}: {}",
                                negocio, puesto, indicador, e.getMessage());
                    }
                }
            }
        }

        // Ordenar por variación fuera del rango (mayor primero)
        alertas.sort(Comparator.comparing(
                a -> a.getVariacionFueraRango().abs(),
                Comparator.reverseOrder()));

        log.info("Se encontraron {} alertas de predicción", alertas.size());
        return alertas;
    }

    private AlertaPrediccion calcularAlertaPrediccion(String negocio, String puesto,
                                                       String indicador, Periodo periodo) {
        // Obtener serie histórica
        Periodo desde = calcularPeriodoInicio(periodo, 52);
        List<DatoCompensacion> datos = repository.findByNegocioAndPuestoAndIndicadorAndPeriodoBetween(
                negocio, puesto, indicador, desde.toCodigo(), periodo.toCodigo());

        // Agrupar por periodo
        Map<Periodo, BigDecimal> valoresPorPeriodo = datos.stream()
                .collect(Collectors.groupingBy(
                        DatoCompensacion::getPeriodo,
                        Collectors.reducing(BigDecimal.ZERO, DatoCompensacion::getValor, BigDecimal::add)
                ));

        List<Periodo> periodosOrdenados = valoresPorPeriodo.keySet().stream()
                .sorted()
                .toList();

        boolean historiaInsuficiente = periodosOrdenados.size() < MIN_PERIODOS_HISTORIA;

        if (periodosOrdenados.size() < 3) {
            return null;
        }

        // Separar observación actual de la historia
        Periodo periodoActual = periodosOrdenados.get(periodosOrdenados.size() - 1);
        BigDecimal observacionReal = valoresPorPeriodo.get(periodoActual);

        // Usar historia sin la última observación para predecir
        List<BigDecimal> valoresHistoricos = periodosOrdenados.subList(0, periodosOrdenados.size() - 1)
                .stream()
                .map(valoresPorPeriodo::get)
                .toList();

        if (valoresHistoricos.isEmpty()) {
            return null;
        }

        // Calcular predicción usando modelo simplificado (media móvil ponderada + tendencia)
        BigDecimal pronostico = calcularPronostico(valoresHistoricos);
        BigDecimal errorEstandar = calcularErrorEstandar(valoresHistoricos, pronostico);

        // Intervalo de predicción 95%: pronóstico ± 1.96 × SE
        BigDecimal factor196 = new BigDecimal("1.96");
        BigDecimal margen = factor196.multiply(errorEstandar);
        BigDecimal limiteInferior = pronostico.subtract(margen);
        BigDecimal limiteSuperior = pronostico.add(margen);

        // Verificar si está fuera del intervalo
        boolean fueraRango = observacionReal.compareTo(limiteInferior) < 0 ||
                observacionReal.compareTo(limiteSuperior) > 0;

        if (!fueraRango) {
            return null; // No es alerta si está dentro del intervalo
        }

        // Calcular variación fuera del rango
        BigDecimal variacionFuera;
        if (observacionReal.compareTo(limiteSuperior) > 0) {
            // Por encima del límite superior
            variacionFuera = pronostico.compareTo(BigDecimal.ZERO) != 0 ?
                    observacionReal.subtract(limiteSuperior)
                            .divide(pronostico, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100")) :
                    BigDecimal.ZERO;
        } else {
            // Por debajo del límite inferior
            variacionFuera = pronostico.compareTo(BigDecimal.ZERO) != 0 ?
                    observacionReal.subtract(limiteInferior)
                            .divide(pronostico, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100")) :
                    BigDecimal.ZERO;
        }

        return AlertaPrediccion.builder()
                .negocio(negocio.trim())
                .puesto(puesto.trim())
                .indicador(indicador.trim())
                .observacionReal(observacionReal.setScale(2, RoundingMode.HALF_UP))
                .pronostico(pronostico.setScale(2, RoundingMode.HALF_UP))
                .limiteInferior(limiteInferior.setScale(2, RoundingMode.HALF_UP))
                .limiteSuperior(limiteSuperior.setScale(2, RoundingMode.HALF_UP))
                .variacionFueraRango(variacionFuera.setScale(2, RoundingMode.HALF_UP))
                .historiaInsuficiente(historiaInsuficiente)
                .periodo(periodo)
                .build();
    }

    /**
     * Calcula pronóstico usando media móvil ponderada exponencial (EMA) con tendencia.
     */
    private BigDecimal calcularPronostico(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int n = valores.size();

        // EMA con alpha = 2/(n+1) para dar más peso a valores recientes
        BigDecimal alpha = new BigDecimal("2").divide(
                new BigDecimal(n + 1), 6, RoundingMode.HALF_UP);

        BigDecimal ema = valores.get(0);
        for (int i = 1; i < n; i++) {
            ema = alpha.multiply(valores.get(i))
                    .add(BigDecimal.ONE.subtract(alpha).multiply(ema));
        }

        // Calcular tendencia (diferencia promedio entre últimos períodos)
        if (n >= 3) {
            BigDecimal tendencia = valores.get(n - 1).subtract(valores.get(n - 3))
                    .divide(new BigDecimal("2"), 6, RoundingMode.HALF_UP);
            return ema.add(tendencia);
        }

        return ema;
    }

    /**
     * Calcula error estándar del pronóstico.
     */
    private BigDecimal calcularErrorEstandar(List<BigDecimal> valores, BigDecimal pronostico) {
        if (valores.size() < 2) {
            return pronostico.multiply(new BigDecimal("0.1")); // 10% como fallback
        }

        // Calcular residuos (diferencias con media)
        BigDecimal media = calcularMedia(valores);
        BigDecimal sumaCuadrados = valores.stream()
                .map(v -> v.subtract(media).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal varianza = sumaCuadrados.divide(
                new BigDecimal(valores.size() - 1), 6, RoundingMode.HALF_UP);

        // Error estándar = sqrt(varianza) / sqrt(n) * factor de ajuste
        double se = Math.sqrt(varianza.doubleValue()) *
                Math.sqrt(1 + 1.0 / valores.size());

        return new BigDecimal(se).setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== UTILIDADES ====================

    private BigDecimal calcularMedia(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal suma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(new BigDecimal(valores.size()), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularDesviacionEstandar(List<BigDecimal> valores, BigDecimal media) {
        if (valores.size() < 2) {
            return BigDecimal.ZERO;
        }

        BigDecimal sumaCuadrados = valores.stream()
                .map(v -> v.subtract(media).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal varianza = sumaCuadrados.divide(
                new BigDecimal(valores.size() - 1), 6, RoundingMode.HALF_UP);

        return new BigDecimal(Math.sqrt(varianza.doubleValue())).setScale(6, RoundingMode.HALF_UP);
    }

    private Periodo calcularPeriodoInicio(Periodo hasta, int semanas) {
        int anio = hasta.getAnio();
        int semana = hasta.getSemana() - semanas;

        while (semana < 1) {
            anio--;
            semana += 52;
        }

        // Construir código de periodo: YYYYWW
        int codigo = anio * 100 + semana;
        return Periodo.fromCodigo(codigo);
    }
}
