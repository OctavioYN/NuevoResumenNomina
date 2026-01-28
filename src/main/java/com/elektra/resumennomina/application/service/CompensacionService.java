package com.elektra.resumennomina.application.service;

import com.elektra.resumennomina.domain.model.*;
import com.elektra.resumennomina.domain.port.input.CompensacionUseCase;
import com.elektra.resumennomina.domain.port.output.DatoCompensacionRepository;
import com.elektra.resumennomina.infrastructure.config.cache.CacheConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio principal que implementa los casos de uso de compensacion.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompensacionService implements CompensacionUseCase {

    private final DatoCompensacionRepository repository;
    private final EstadisticasService estadisticasService;

    private static final String INDICADOR_COMPENSACION = "Compensación";
    private static final String INDICADOR_EMPLEADO = "Empleado";

    // ==================== VISTA NACIONAL ====================

    @Override
    @Cacheable(value = CacheConfig.COMPENSACION_NACIONAL, key = "#periodo.toCodigo()")
    public ResumenSemanal obtenerResumenNacional(Periodo periodo) {
        log.info("Obteniendo resumen nacional para periodo: {}", periodo);

        List<DatoCompensacion> datosActual = repository.findByPeriodo(periodo.toCodigo());
        List<DatoCompensacion> datosAnterior = repository.findByPeriodo(periodo.anterior().toCodigo());

        BigDecimal totalActual = sumarCompensacion(datosActual);
        BigDecimal totalAnterior = sumarCompensacion(datosAnterior);
        int empleadosActual = contarEmpleados(datosActual);
        int empleadosAnterior = contarEmpleados(datosAnterior);

        ResumenSemanal resumen = ResumenSemanal.builder()
                .periodo(periodo)
                .negocio("Nacional")
                .totalCompensacion(totalActual)
                .totalAnterior(totalAnterior)
                .cantidadEmpleados(empleadosActual)
                .empleadosAnterior(empleadosAnterior)
                .build();

        resumen.calcularDiferenciaMonto();
        resumen.calcularVariacionPorcentual();
        resumen.calcularPromedio();

        return resumen;
    }

    @Override
    @Cacheable(value = CacheConfig.COMPENSACION_POR_NEGOCIO, key = "#periodo.toCodigo()")
    public List<ResumenSemanal> obtenerResumenNacionalPorNegocio(Periodo periodo) {
        log.info("Obteniendo resumen por negocio para periodo: {}", periodo);

        List<String> negocios = repository.findDistinctNegocios();
        List<ResumenSemanal> resumenes = new ArrayList<>();

        for (String negocio : negocios) {
            List<DatoCompensacion> datosActual = repository.findByNegocioAndPeriodo(negocio, periodo.toCodigo());
            List<DatoCompensacion> datosAnterior = repository.findByNegocioAndPeriodo(negocio, periodo.anterior().toCodigo());

            BigDecimal totalActual = sumarCompensacion(datosActual);
            BigDecimal totalAnterior = sumarCompensacion(datosAnterior);
            int empleadosActual = contarEmpleados(datosActual);

            ResumenSemanal resumen = ResumenSemanal.builder()
                    .periodo(periodo)
                    .negocio(negocio.trim())
                    .totalCompensacion(totalActual)
                    .totalAnterior(totalAnterior)
                    .cantidadEmpleados(empleadosActual)
                    .build();

            resumen.calcularDiferenciaMonto();
            resumen.calcularVariacionPorcentual();
            resumen.calcularPromedio();

            resumenes.add(resumen);
        }

        return resumenes;
    }

    @Override
    @Cacheable(value = CacheConfig.ESTADISTICAS, key = "'nacional-total-' + #desde.toCodigo() + '-' + #hasta.toCodigo()")
    public EstadisticasSerie obtenerSerieNacionalTotal(Periodo desde, Periodo hasta) {
        log.info("Obteniendo serie nacional total desde {} hasta {}", desde, hasta);

        List<DatoCompensacion> datos = repository.findByPeriodoBetween(desde.toCodigo(), hasta.toCodigo());

        Map<Periodo, BigDecimal> seriePorPeriodo = datos.stream()
                .filter(d -> INDICADOR_COMPENSACION.equalsIgnoreCase(d.getIndicador().trim()))
                .collect(Collectors.groupingBy(
                        DatoCompensacion::getPeriodo,
                        Collectors.reducing(BigDecimal.ZERO, DatoCompensacion::getValor, BigDecimal::add)
                ));

        return estadisticasService.calcularEstadisticas(seriePorPeriodo, "Nacional", "Total", INDICADOR_COMPENSACION);
    }

    @Override
    @Cacheable(value = CacheConfig.ESTADISTICAS, key = "'negocio-' + #negocio + '-' + #desde.toCodigo() + '-' + #hasta.toCodigo()")
    public EstadisticasSerie obtenerSeriePorNegocio(String negocio, Periodo desde, Periodo hasta) {
        log.info("Obteniendo serie para negocio {} desde {} hasta {}", negocio, desde, hasta);

        List<DatoCompensacion> datos = repository.findByNegocioAndIndicadorAndPeriodoBetween(
                negocio, INDICADOR_COMPENSACION, desde.toCodigo(), hasta.toCodigo());

        Map<Periodo, BigDecimal> seriePorPeriodo = datos.stream()
                .collect(Collectors.groupingBy(
                        DatoCompensacion::getPeriodo,
                        Collectors.reducing(BigDecimal.ZERO, DatoCompensacion::getValor, BigDecimal::add)
                ));

        return estadisticasService.calcularEstadisticas(seriePorPeriodo, negocio, "Total", INDICADOR_COMPENSACION);
    }

    @Override
    @Cacheable(value = CacheConfig.ESTADISTICAS, key = "'negocio-puesto-' + #negocio + '-' + #puesto + '-' + #desde.toCodigo() + '-' + #hasta.toCodigo()")
    public EstadisticasSerie obtenerSeriePorNegocioYPuesto(String negocio, String puesto, Periodo desde, Periodo hasta) {
        log.info("Obteniendo serie para negocio {} puesto {} desde {} hasta {}", negocio, puesto, desde, hasta);

        List<DatoCompensacion> datos = repository.findByNegocioAndPuestoAndPeriodoBetween(
                negocio, puesto, desde.toCodigo(), hasta.toCodigo());

        // Filtrar por indicador Compensacion y agrupar por periodo
        Map<Periodo, BigDecimal> seriePorPeriodo = datos.stream()
                .filter(d -> INDICADOR_COMPENSACION.equalsIgnoreCase(d.getIndicador().trim()))
                .collect(Collectors.groupingBy(
                        DatoCompensacion::getPeriodo,
                        Collectors.reducing(BigDecimal.ZERO, DatoCompensacion::getValor, BigDecimal::add)
                ));

        return estadisticasService.calcularEstadisticas(seriePorPeriodo, negocio, puesto, INDICADOR_COMPENSACION);
    }

    @Override
    @Cacheable(value = CacheConfig.ESTADISTICAS, key = "'negocio-puesto-ind-' + #negocio + '-' + #puesto + '-' + #indicador + '-' + #desde.toCodigo() + '-' + #hasta.toCodigo()")
    public EstadisticasSerie obtenerSeriePorNegocioPuestoIndicador(
            String negocio, String puesto, String indicador, Periodo desde, Periodo hasta) {
        log.info("Obteniendo serie para negocio {} puesto {} indicador {} desde {} hasta {}",
                negocio, puesto, indicador, desde, hasta);

        List<DatoCompensacion> datos = repository.findByNegocioAndPuestoAndIndicadorAndPeriodoBetween(
                negocio, puesto, indicador, desde.toCodigo(), hasta.toCodigo());

        // Agrupar por periodo
        Map<Periodo, BigDecimal> seriePorPeriodo = datos.stream()
                .collect(Collectors.groupingBy(
                        DatoCompensacion::getPeriodo,
                        Collectors.reducing(BigDecimal.ZERO, DatoCompensacion::getValor, BigDecimal::add)
                ));

        // Para gráficas mostrar PROMEDIO por empleado (excepto indicador Empleado)
        if (!indicador.equalsIgnoreCase(INDICADOR_EMPLEADO)) {
            // Obtener empleados por periodo del mismo negocio y puesto
            List<DatoCompensacion> datosEmpleados = repository.findByNegocioAndPuestoAndIndicadorAndPeriodoBetween(
                    negocio, puesto, INDICADOR_EMPLEADO, desde.toCodigo(), hasta.toCodigo());

            Map<Periodo, Integer> empleadosPorPeriodo = datosEmpleados.stream()
                    .collect(Collectors.toMap(
                            DatoCompensacion::getPeriodo,
                            d -> d.getValor().intValue(),
                            Integer::sum
                    ));

            seriePorPeriodo = seriePorPeriodo.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> {
                                Integer empleados = empleadosPorPeriodo.get(e.getKey());
                                if (empleados != null && empleados > 0) {
                                    return e.getValue().divide(BigDecimal.valueOf(empleados), 2, RoundingMode.HALF_UP);
                                }
                                return e.getValue();
                            }
                    ));
        }

        return estadisticasService.calcularEstadisticas(seriePorPeriodo, negocio, puesto, indicador);
    }

    @Override
    public List<ResumenIndicador> obtenerResumenIndicadoresPorNegocioYPuesto(
            String negocio, String puesto, Periodo periodo) {
        log.info("Obteniendo resumen de indicadores para negocio {} puesto {} periodo {}", negocio, puesto, periodo);

        // Obtener indicadores disponibles para este negocio y puesto
        List<String> indicadores = repository.findDistinctIndicadoresByNegocioAndPuesto(negocio, puesto);

        // Obtener datos del periodo actual y anterior
        List<DatoCompensacion> datosActual = repository.findByNegocioAndPuestoAndPeriodo(
                negocio, puesto, periodo.toCodigo());
        List<DatoCompensacion> datosAnterior = repository.findByNegocioAndPuestoAndPeriodo(
                negocio, puesto, periodo.anterior().toCodigo());

        log.info("Datos encontrados para periodo actual {}: {} registros", periodo.toCodigo(), datosActual.size());
        log.info("Datos encontrados para periodo anterior {}: {} registros", periodo.anterior().toCodigo(), datosAnterior.size());

        List<ResumenIndicador> resumenes = new ArrayList<>();

        for (String indicador : indicadores) {
            BigDecimal totalActual = datosActual.stream()
                    .filter(d -> indicador.equalsIgnoreCase(d.getIndicador().trim()))
                    .map(DatoCompensacion::getValor)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalAnterior = datosAnterior.stream()
                    .filter(d -> indicador.equalsIgnoreCase(d.getIndicador().trim()))
                    .map(DatoCompensacion::getValor)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Mostrar totales sin dividir por empleados
            BigDecimal valorActual = totalActual;
            BigDecimal valorAnterior = totalAnterior;

            BigDecimal diferencia = valorActual.subtract(valorAnterior);
            BigDecimal variacion = estadisticasService.calcularVariacionPorcentual(valorActual, valorAnterior);

            resumenes.add(ResumenIndicador.builder()
                    .indicador(indicador.trim())
                    .valorActual(valorActual)
                    .valorAnterior(valorAnterior)
                    .diferencia(diferencia)
                    .variacionPorcentual(variacion)
                    .variacionPositiva(diferencia.compareTo(BigDecimal.ZERO) >= 0)
                    .build());
        }

        // Ordenar por nombre de indicador
        resumenes.sort(Comparator.comparing(ResumenIndicador::getIndicador));

        return resumenes;
    }

    // ==================== VISTA POR PUESTO ====================

    @Override
    @Cacheable(value = CacheConfig.COMPENSACION_POR_PUESTO, key = "#funcionSap + '-' + #periodo.toCodigo()")
    public ResumenSemanal obtenerResumenPorPuesto(int funcionSap, Periodo periodo) {
        log.info("Obteniendo resumen para funcion SAP {} periodo {}", funcionSap, periodo);

        List<DatoCompensacion> datosActual = repository.findByFuncionSapAndPeriodoBetween(
                funcionSap, periodo.toCodigo(), periodo.toCodigo());
        List<DatoCompensacion> datosAnterior = repository.findByFuncionSapAndPeriodoBetween(
                funcionSap, periodo.anterior().toCodigo(), periodo.anterior().toCodigo());

        String puesto = datosActual.stream()
                .findFirst()
                .map(d -> d.getPuesto().trim())
                .orElse("Desconocido");

        String negocio = datosActual.stream()
                .findFirst()
                .map(d -> d.getNegocio().trim())
                .orElse("Desconocido");

        BigDecimal totalActual = sumarCompensacion(datosActual);
        BigDecimal totalAnterior = sumarCompensacion(datosAnterior);
        int empleadosActual = contarEmpleados(datosActual);

        ResumenSemanal resumen = ResumenSemanal.builder()
                .periodo(periodo)
                .negocio(negocio)
                .puesto(puesto)
                .totalCompensacion(totalActual)
                .totalAnterior(totalAnterior)
                .cantidadEmpleados(empleadosActual)
                .build();

        resumen.calcularDiferenciaMonto();
        resumen.calcularVariacionPorcentual();
        resumen.calcularPromedio();

        return resumen;
    }

    @Override
    public EstadisticasSerie obtenerSeriePorPuestoIndicador(int funcionSap, String indicador, Periodo desde, Periodo hasta) {
        log.info("Obteniendo serie para funcion SAP {} indicador {} desde {} hasta {}", funcionSap, indicador, desde, hasta);

        List<DatoCompensacion> datos = repository.findByFuncionSapAndPeriodoBetween(
                funcionSap, desde.toCodigo(), hasta.toCodigo());

        String puesto = datos.stream()
                .findFirst()
                .map(d -> d.getPuesto().trim())
                .orElse("Desconocido");

        String negocio = datos.stream()
                .findFirst()
                .map(d -> d.getNegocio().trim())
                .orElse("Desconocido");

        // Filtrar por indicador y agrupar por periodo
        Map<Periodo, BigDecimal> seriePorPeriodo = datos.stream()
                .filter(d -> indicador.equalsIgnoreCase(d.getIndicador().trim()))
                .collect(Collectors.groupingBy(
                        DatoCompensacion::getPeriodo,
                        Collectors.reducing(BigDecimal.ZERO, DatoCompensacion::getValor, BigDecimal::add)
                ));

        // Para gráficas mostrar PROMEDIO por empleado (excepto indicador Empleado)
        if (!indicador.equalsIgnoreCase(INDICADOR_EMPLEADO)) {
            Map<Periodo, Integer> empleadosPorPeriodo = datos.stream()
                    .filter(d -> INDICADOR_EMPLEADO.equalsIgnoreCase(d.getIndicador().trim()))
                    .collect(Collectors.toMap(
                            DatoCompensacion::getPeriodo,
                            d -> d.getValor().intValue(),
                            Integer::sum
                    ));

            seriePorPeriodo = seriePorPeriodo.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> {
                                Integer empleados = empleadosPorPeriodo.get(e.getKey());
                                if (empleados != null && empleados > 0) {
                                    return e.getValue().divide(BigDecimal.valueOf(empleados), 2, RoundingMode.HALF_UP);
                                }
                                return e.getValue();
                            }
                    ));
        }

        return estadisticasService.calcularEstadisticas(seriePorPeriodo, negocio, puesto, indicador);
    }

    // ==================== MAPA DE CALOR ====================

    @Override
    @Cacheable(value = CacheConfig.MAPA_CALOR, key = "#periodo.toCodigo()")
    public List<MapaCalorItem> obtenerMapaCalor(Periodo periodo) {
        log.info("Obteniendo mapa de calor para periodo {}", periodo);

        List<DatoCompensacion> datosActual = repository.findByPeriodo(periodo.toCodigo());
        List<DatoCompensacion> datosAnterior = repository.findByPeriodo(periodo.anterior().toCodigo());

        // Agrupar por puesto
        Map<String, BigDecimal> compensacionActualPorPuesto = datosActual.stream()
                .filter(d -> INDICADOR_COMPENSACION.equalsIgnoreCase(d.getIndicador().trim()))
                .collect(Collectors.groupingBy(
                        d -> d.getPuesto().trim(),
                        Collectors.reducing(BigDecimal.ZERO, DatoCompensacion::getValor, BigDecimal::add)
                ));

        Map<String, BigDecimal> compensacionAnteriorPorPuesto = datosAnterior.stream()
                .filter(d -> INDICADOR_COMPENSACION.equalsIgnoreCase(d.getIndicador().trim()))
                .collect(Collectors.groupingBy(
                        d -> d.getPuesto().trim(),
                        Collectors.reducing(BigDecimal.ZERO, DatoCompensacion::getValor, BigDecimal::add)
                ));

        Map<String, String> negocioPorPuesto = datosActual.stream()
                .collect(Collectors.toMap(
                        d -> d.getPuesto().trim(),
                        d -> d.getNegocio().trim(),
                        (a, b) -> a
                ));

        List<MapaCalorItem> items = new ArrayList<>();

        for (String puesto : compensacionActualPorPuesto.keySet()) {
            BigDecimal valorActual = compensacionActualPorPuesto.get(puesto);
            BigDecimal valorAnterior = compensacionAnteriorPorPuesto.getOrDefault(puesto, BigDecimal.ZERO);
            BigDecimal variacion = estadisticasService.calcularVariacionPorcentual(valorActual, valorAnterior);

            items.add(MapaCalorItem.builder()
                    .negocio(negocioPorPuesto.getOrDefault(puesto, "Otro"))
                    .puesto(puesto)
                    .valorSemanaActual(valorActual)
                    .valorSemanaAnterior(valorAnterior)
                    .variacionPorcentual(variacion)
                    .periodoActual(periodo)
                    .periodoAnterior(periodo.anterior())
                    .build());
        }

        // Ordenar por negocio y variacion absoluta
        items.sort(Comparator.comparing(MapaCalorItem::getNegocio)
                .thenComparing(i -> i.getValorAbsoluto().negate())); // Mayor variacion primero

        return items;
    }

    // ==================== TABLA DE RESULTADOS ====================

    @Override
    public List<ResumenSemanal> obtenerTablaResultadosTotal(Periodo periodo) {
        log.info("Obteniendo tabla de resultados total para periodo {}", periodo);
        return obtenerTablaResultados(periodo, false);
    }

    @Override
    public List<ResumenSemanal> obtenerTablaResultadosPromedio(Periodo periodo) {
        log.info("Obteniendo tabla de resultados promedio para periodo {}", periodo);
        return obtenerTablaResultados(periodo, true);
    }

    private List<ResumenSemanal> obtenerTablaResultados(Periodo periodo, boolean calcularPromedio) {
        List<DatoCompensacion> datosActual = repository.findByPeriodo(periodo.toCodigo());
        List<DatoCompensacion> datosAnterior = repository.findByPeriodo(periodo.anterior().toCodigo());

        // Agrupar por puesto
        Map<String, List<DatoCompensacion>> porPuestoActual = datosActual.stream()
                .collect(Collectors.groupingBy(d -> d.getPuesto().trim()));

        Map<String, List<DatoCompensacion>> porPuestoAnterior = datosAnterior.stream()
                .collect(Collectors.groupingBy(d -> d.getPuesto().trim()));

        List<ResumenSemanal> resultados = new ArrayList<>();

        for (String puesto : porPuestoActual.keySet()) {
            List<DatoCompensacion> datosActualPuesto = porPuestoActual.get(puesto);
            List<DatoCompensacion> datosAnteriorPuesto = porPuestoAnterior.getOrDefault(puesto, List.of());

            String negocio = datosActualPuesto.stream()
                    .findFirst()
                    .map(d -> d.getNegocio().trim())
                    .orElse("Otro");

            BigDecimal totalActual = sumarCompensacion(datosActualPuesto);
            BigDecimal totalAnterior = sumarCompensacion(datosAnteriorPuesto);
            int empleadosActual = contarEmpleados(datosActualPuesto);
            int empleadosAnterior = contarEmpleados(datosAnteriorPuesto);

            BigDecimal valorActual = totalActual;
            BigDecimal valorAnterior = totalAnterior;

            if (calcularPromedio && empleadosActual > 0) {
                valorActual = totalActual.divide(BigDecimal.valueOf(empleadosActual), 2, RoundingMode.HALF_UP);
                if (empleadosAnterior > 0) {
                    valorAnterior = totalAnterior.divide(BigDecimal.valueOf(empleadosAnterior), 2, RoundingMode.HALF_UP);
                }
            }

            ResumenSemanal resumen = ResumenSemanal.builder()
                    .periodo(periodo)
                    .negocio(negocio)
                    .puesto(puesto)
                    .totalCompensacion(valorActual)
                    .totalAnterior(valorAnterior)
                    .promedioCompensacion(calcularPromedio ? valorActual : null)
                    .promedioAnterior(calcularPromedio ? valorAnterior : null)
                    .cantidadEmpleados(empleadosActual)
                    .build();

            resumen.calcularDiferenciaMonto();
            resumen.calcularVariacionPorcentual();

            resultados.add(resumen);
        }

        // Ordenar por negocio y puesto
        resultados.sort(Comparator.comparing(ResumenSemanal::getNegocio)
                .thenComparing(ResumenSemanal::getPuesto));

        return resultados;
    }

    // ==================== UTILITARIOS ====================

    @Override
    public List<String> obtenerNegocios() {
        return repository.findDistinctNegocios().stream()
                .map(String::trim)
                .sorted()
                .toList();
    }

    @Override
    public List<String> obtenerPuestosPorNegocio(String negocio) {
        return repository.findDistinctPuestosByNegocio(negocio).stream()
                .map(String::trim)
                .sorted()
                .toList();
    }

    @Override
    public List<String> obtenerIndicadores() {
        return repository.findDistinctIndicadores().stream()
                .map(String::trim)
                .filter(i -> !i.isEmpty())
                .sorted()
                .toList();
    }

    @Override
    public List<String> obtenerIndicadoresPorNegocio(String negocio) {
        return repository.findDistinctIndicadoresByNegocio(negocio).stream()
                .map(String::trim)
                .filter(i -> !i.isEmpty())
                .sorted()
                .toList();
    }

    @Override
    public List<String> obtenerIndicadoresPorNegocioYPuesto(String negocio, String puesto) {
        return repository.findDistinctIndicadoresByNegocioAndPuesto(negocio, puesto).stream()
                .map(String::trim)
                .filter(i -> !i.isEmpty())
                .sorted()
                .toList();
    }

    @Override
    public List<Periodo> obtenerPeriodosDisponibles() {
        return repository.findDistinctPeriodos().stream()
                .map(Periodo::fromCodigo)
                .sorted()
                .toList();
    }

    @Override
    public Periodo obtenerPeriodoActual() {
        // Obtener periodos disponibles ordenados de mas reciente a mas antiguo
        List<Integer> periodos = repository.findDistinctPeriodos();
        if (periodos.isEmpty()) {
            return Periodo.actual();
        }

        // Obtener negocios esperados
        List<String> negociosEsperados = repository.findDistinctNegocios();
        int minNegociosRequeridos = Math.max(3, negociosEsperados.size() - 1); // Al menos 3 negocios o todos menos 1

        // Buscar el periodo mas reciente con datos completos
        for (int i = periodos.size() - 1; i >= 0; i--) {
            int codigoPeriodo = periodos.get(i);
            List<DatoCompensacion> datos = repository.findByPeriodo(codigoPeriodo);

            // Contar negocios unicos con datos de compensacion
            long negociosConDatos = datos.stream()
                    .filter(d -> INDICADOR_COMPENSACION.equalsIgnoreCase(d.getIndicador().trim()))
                    .map(d -> d.getNegocio().trim())
                    .distinct()
                    .count();

            if (negociosConDatos >= minNegociosRequeridos) {
                log.info("Periodo actual seleccionado: {} con {} negocios", codigoPeriodo, negociosConDatos);
                return Periodo.fromCodigo(codigoPeriodo);
            }
        }

        // Fallback al periodo mas reciente
        return Periodo.fromCodigo(periodos.get(periodos.size() - 1));
    }

    // ==================== HELPERS ====================

    private BigDecimal sumarCompensacion(List<DatoCompensacion> datos) {
        return datos.stream()
                .filter(d -> INDICADOR_COMPENSACION.equalsIgnoreCase(d.getIndicador().trim()))
                .map(DatoCompensacion::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int contarEmpleados(List<DatoCompensacion> datos) {
        return datos.stream()
                .filter(d -> INDICADOR_EMPLEADO.equalsIgnoreCase(d.getIndicador().trim()))
                .map(DatoCompensacion::getValor)
                .filter(Objects::nonNull)
                .mapToInt(BigDecimal::intValue)
                .sum();
    }
}
