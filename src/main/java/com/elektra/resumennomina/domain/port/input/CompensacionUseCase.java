package com.elektra.resumennomina.domain.port.input;

import com.elektra.resumennomina.domain.model.*;

import java.util.List;

/**
 * Puerto de entrada que define los casos de uso de compensacion.
 * Representa las operaciones que el sistema expone al exterior.
 */
public interface CompensacionUseCase {

    // ==================== VISTA NACIONAL ====================

    /**
     * Obtiene el resumen general nacional para la semana actual vs anterior
     */
    ResumenSemanal obtenerResumenNacional(Periodo periodo);

    /**
     * Obtiene el resumen nacional por negocio
     */
    List<ResumenSemanal> obtenerResumenNacionalPorNegocio(Periodo periodo);

    /**
     * Obtiene la serie historica de compensacion total nacional
     */
    EstadisticasSerie obtenerSerieNacionalTotal(Periodo desde, Periodo hasta);

    /**
     * Obtiene la serie historica de compensacion por negocio
     */
    EstadisticasSerie obtenerSeriePorNegocio(String negocio, Periodo desde, Periodo hasta);

    /**
     * Obtiene la serie historica de compensacion por negocio y puesto
     */
    EstadisticasSerie obtenerSeriePorNegocioYPuesto(String negocio, String puesto, Periodo desde, Periodo hasta);

    /**
     * Obtiene la serie historica por negocio, puesto e indicador
     */
    EstadisticasSerie obtenerSeriePorNegocioPuestoIndicador(
            String negocio, String puesto, String indicador, Periodo desde, Periodo hasta);

    /**
     * Obtiene el resumen de todos los indicadores para un negocio y puesto
     */
    List<ResumenIndicador> obtenerResumenIndicadoresPorNegocioYPuesto(
            String negocio, String puesto, Periodo periodo);

    // ==================== VISTA POR PUESTO ====================

    /**
     * Obtiene el resumen de un puesto especifico
     */
    ResumenSemanal obtenerResumenPorPuesto(int funcionSap, Periodo periodo);

    /**
     * Obtiene la serie historica de un indicador para un puesto
     */
    EstadisticasSerie obtenerSeriePorPuestoIndicador(
            int funcionSap, String indicador, Periodo desde, Periodo hasta);

    // ==================== MAPA DE CALOR ====================

    /**
     * Obtiene los datos para el mapa de calor de variaciones
     */
    List<MapaCalorItem> obtenerMapaCalor(Periodo periodo);

    // ==================== TABLA DE RESULTADOS ====================

    /**
     * Obtiene la tabla de resultados con totales por puesto
     */
    List<ResumenSemanal> obtenerTablaResultadosTotal(Periodo periodo);

    /**
     * Obtiene la tabla de resultados con promedios por puesto
     */
    List<ResumenSemanal> obtenerTablaResultadosPromedio(Periodo periodo);

    // ==================== UTILITARIOS ====================

    /**
     * Obtiene la lista de negocios disponibles
     */
    List<String> obtenerNegocios();

    /**
     * Obtiene la lista de puestos por negocio
     */
    List<String> obtenerPuestosPorNegocio(String negocio);

    /**
     * Obtiene la lista de indicadores disponibles
     */
    List<String> obtenerIndicadores();

    /**
     * Obtiene la lista de indicadores por negocio
     */
    List<String> obtenerIndicadoresPorNegocio(String negocio);

    /**
     * Obtiene la lista de indicadores por negocio y puesto
     */
    List<String> obtenerIndicadoresPorNegocioYPuesto(String negocio, String puesto);

    /**
     * Obtiene los periodos disponibles
     */
    List<Periodo> obtenerPeriodosDisponibles();

    /**
     * Obtiene el periodo mas reciente
     */
    Periodo obtenerPeriodoActual();
}
