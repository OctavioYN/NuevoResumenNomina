package com.elektra.resumennomina.infrastructure.persistence.repository;

import com.elektra.resumennomina.infrastructure.persistence.document.DatoCompensacionDocument;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio MongoDB para DatoCompensacionDocument.
 */
@Repository
public interface DatoCompensacionMongoRepository extends MongoRepository<DatoCompensacionDocument, String> {

    // Busquedas por periodo
    List<DatoCompensacionDocument> findByCodigoPeriodo(Integer codigoPeriodo);

    List<DatoCompensacionDocument> findByCodigoPeriodoBetween(Integer periodoInicio, Integer periodoFin);

    // Busquedas por negocio
    List<DatoCompensacionDocument> findByNegocio(String negocio);

    List<DatoCompensacionDocument> findByNegocioAndCodigoPeriodo(String negocio, Integer codigoPeriodo);

    List<DatoCompensacionDocument> findByNegocioAndCodigoPeriodoBetween(
            String negocio, Integer periodoInicio, Integer periodoFin);

    // Busquedas por puesto
    List<DatoCompensacionDocument> findByPuesto(String puesto);

    List<DatoCompensacionDocument> findByPuestoAndCodigoPeriodoBetween(
            String puesto, Integer periodoInicio, Integer periodoFin);

    // Busquedas por funcion SAP
    List<DatoCompensacionDocument> findByFuncionSap(Integer funcionSap);

    List<DatoCompensacionDocument> findByFuncionSapAndCodigoPeriodoBetween(
            Integer funcionSap, Integer periodoInicio, Integer periodoFin);

    // Busquedas combinadas
    List<DatoCompensacionDocument> findByNegocioAndIndicadorAndCodigoPeriodoBetween(
            String negocio, String indicador, Integer periodoInicio, Integer periodoFin);

    // Busquedas por negocio y puesto
    @Query("{ 'negocio': ?0, 'puesto': ?1, 'codigoPeriodo': { $gte: ?2, $lte: ?3 } }")
    List<DatoCompensacionDocument> findByNegocioAndPuestoAndCodigoPeriodoBetween(
            String negocio, String puesto, Integer periodoInicio, Integer periodoFin);

    // Busqueda por negocio, puesto y periodo exacto
    @Query("{ 'negocio': ?0, 'puesto': ?1, 'codigoPeriodo': ?2 }")
    List<DatoCompensacionDocument> findByNegocioAndPuestoAndCodigoPeriodo(
            String negocio, String puesto, Integer codigoPeriodo);

    // Busquedas por negocio, puesto e indicador
    List<DatoCompensacionDocument> findByNegocioAndPuestoAndIndicadorAndCodigoPeriodoBetween(
            String negocio, String puesto, String indicador, Integer periodoInicio, Integer periodoFin);

    // Agregaciones para valores distintos
    @Aggregation(pipeline = {
            "{ $group: { _id: '$negocio' } }",
            "{ $sort: { _id: 1 } }"
    })
    List<String> findDistinctNegocios();

    @Aggregation(pipeline = {
            "{ $match: { negocio: ?0 } }",
            "{ $group: { _id: '$puesto' } }",
            "{ $sort: { _id: 1 } }"
    })
    List<String> findDistinctPuestosByNegocio(String negocio);

    @Aggregation(pipeline = {
            "{ $group: { _id: '$codigoPeriodo' } }",
            "{ $sort: { _id: 1 } }"
    })
    List<Integer> findDistinctPeriodos();

    // Indicadores distintos
    @Aggregation(pipeline = {
            "{ $group: { _id: '$indicador' } }",
            "{ $sort: { _id: 1 } }"
    })
    List<String> findDistinctIndicadores();

    @Aggregation(pipeline = {
            "{ $match: { negocio: ?0 } }",
            "{ $group: { _id: '$indicador' } }",
            "{ $sort: { _id: 1 } }"
    })
    List<String> findDistinctIndicadoresByNegocio(String negocio);

    @Aggregation(pipeline = {
            "{ $match: { negocio: ?0, puesto: ?1 } }",
            "{ $group: { _id: '$indicador' } }",
            "{ $sort: { _id: 1 } }"
    })
    List<String> findDistinctIndicadoresByNegocioAndPuesto(String negocio, String puesto);

    // Obtener el periodo maximo
    @Aggregation(pipeline = {
            "{ $sort: { codigoPeriodo: -1 } }",
            "{ $limit: 1 }"
    })
    Optional<DatoCompensacionDocument> findTopByOrderByCodigoPeriodoDesc();
}
