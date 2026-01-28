package com.elektra.resumennomina.domain.port.output;

import com.elektra.resumennomina.domain.model.DatoCompensacion;
import com.elektra.resumennomina.domain.model.Periodo;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para el repositorio de datos de compensacion.
 * Define el contrato que debe implementar cualquier adaptador de persistencia.
 */
public interface DatoCompensacionRepository {

    /**
     * Guarda un dato de compensacion
     */
    DatoCompensacion save(DatoCompensacion dato);

    /**
     * Guarda multiples datos de compensacion
     */
    List<DatoCompensacion> saveAll(List<DatoCompensacion> datos);

    /**
     * Busca por ID
     */
    Optional<DatoCompensacion> findById(String id);

    /**
     * Busca todos los datos de un periodo especifico
     */
    List<DatoCompensacion> findByPeriodo(int codigoPeriodo);

    /**
     * Busca datos por rango de periodos
     */
    List<DatoCompensacion> findByPeriodoBetween(int periodoInicio, int periodoFin);

    /**
     * Busca datos por negocio
     */
    List<DatoCompensacion> findByNegocio(String negocio);

    /**
     * Busca datos por negocio y periodo
     */
    List<DatoCompensacion> findByNegocioAndPeriodo(String negocio, int codigoPeriodo);

    /**
     * Busca datos por puesto
     */
    List<DatoCompensacion> findByPuesto(String puesto);

    /**
     * Busca datos por puesto y rango de periodos
     */
    List<DatoCompensacion> findByPuestoAndPeriodoBetween(String puesto, int periodoInicio, int periodoFin);

    /**
     * Busca datos por funcion SAP
     */
    List<DatoCompensacion> findByFuncionSap(int funcionSap);

    /**
     * Busca datos por funcion SAP y rango de periodos
     */
    List<DatoCompensacion> findByFuncionSapAndPeriodoBetween(int funcionSap, int periodoInicio, int periodoFin);

    /**
     * Busca datos por negocio, indicador y rango de periodos
     */
    List<DatoCompensacion> findByNegocioAndIndicadorAndPeriodoBetween(
            String negocio, String indicador, int periodoInicio, int periodoFin);

    /**
     * Busca datos por negocio, puesto y rango de periodos
     */
    List<DatoCompensacion> findByNegocioAndPuestoAndPeriodoBetween(
            String negocio, String puesto, int periodoInicio, int periodoFin);

    /**
     * Busca datos por negocio, puesto y periodo exacto
     */
    List<DatoCompensacion> findByNegocioAndPuestoAndPeriodo(
            String negocio, String puesto, int codigoPeriodo);

    /**
     * Busca datos por negocio, puesto, indicador y rango de periodos
     */
    List<DatoCompensacion> findByNegocioAndPuestoAndIndicadorAndPeriodoBetween(
            String negocio, String puesto, String indicador, int periodoInicio, int periodoFin);

    /**
     * Obtiene la lista de negocios unicos
     */
    List<String> findDistinctNegocios();

    /**
     * Obtiene la lista de puestos unicos por negocio
     */
    List<String> findDistinctPuestosByNegocio(String negocio);

    /**
     * Obtiene la lista de periodos disponibles
     */
    List<Integer> findDistinctPeriodos();

    /**
     * Obtiene la lista de indicadores unicos
     */
    List<String> findDistinctIndicadores();

    /**
     * Obtiene la lista de indicadores unicos por negocio
     */
    List<String> findDistinctIndicadoresByNegocio(String negocio);

    /**
     * Obtiene la lista de indicadores unicos por negocio y puesto
     */
    List<String> findDistinctIndicadoresByNegocioAndPuesto(String negocio, String puesto);

    /**
     * Obtiene el ultimo periodo disponible
     */
    Optional<Integer> findMaxPeriodo();

    /**
     * Elimina todos los datos
     */
    void deleteAll();

    /**
     * Cuenta el total de registros
     */
    long count();
}
