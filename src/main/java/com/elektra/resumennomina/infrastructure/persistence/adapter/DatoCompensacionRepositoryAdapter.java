package com.elektra.resumennomina.infrastructure.persistence.adapter;

import com.elektra.resumennomina.domain.model.DatoCompensacion;
import com.elektra.resumennomina.domain.port.output.DatoCompensacionRepository;
import com.elektra.resumennomina.infrastructure.persistence.document.DatoCompensacionDocument;
import com.elektra.resumennomina.infrastructure.persistence.mapper.DatoCompensacionMapper;
import com.elektra.resumennomina.infrastructure.persistence.repository.DatoCompensacionMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa el puerto de salida DatoCompensacionRepository
 * usando MongoDB como persistencia.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatoCompensacionRepositoryAdapter implements DatoCompensacionRepository {

    private final DatoCompensacionMongoRepository mongoRepository;
    private final DatoCompensacionMapper mapper;

    @Override
    public DatoCompensacion save(DatoCompensacion dato) {
        DatoCompensacionDocument document = mapper.toDocument(dato);
        DatoCompensacionDocument saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public List<DatoCompensacion> saveAll(List<DatoCompensacion> datos) {
        List<DatoCompensacionDocument> documents = mapper.toDocumentList(datos);
        List<DatoCompensacionDocument> saved = mongoRepository.saveAll(documents);
        log.info("Guardados {} registros en MongoDB", saved.size());
        return mapper.toDomainList(saved);
    }

    @Override
    public Optional<DatoCompensacion> findById(String id) {
        return mongoRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<DatoCompensacion> findByPeriodo(int codigoPeriodo) {
        return mapper.toDomainList(
                mongoRepository.findByCodigoPeriodo(codigoPeriodo)
        );
    }

    @Override
    public List<DatoCompensacion> findByPeriodoBetween(int periodoInicio, int periodoFin) {
        return mapper.toDomainList(
                mongoRepository.findByCodigoPeriodoBetween(periodoInicio, periodoFin)
        );
    }

    @Override
    public List<DatoCompensacion> findByNegocio(String negocio) {
        return mapper.toDomainList(
                mongoRepository.findByNegocio(normalizeString(negocio))
        );
    }

    @Override
    public List<DatoCompensacion> findByNegocioAndPeriodo(String negocio, int codigoPeriodo) {
        return mapper.toDomainList(
                mongoRepository.findByNegocioAndCodigoPeriodo(normalizeString(negocio), codigoPeriodo)
        );
    }

    @Override
    public List<DatoCompensacion> findByPuesto(String puesto) {
        return mapper.toDomainList(
                mongoRepository.findByPuesto(normalizeString(puesto))
        );
    }

    @Override
    public List<DatoCompensacion> findByPuestoAndPeriodoBetween(String puesto, int periodoInicio, int periodoFin) {
        return mapper.toDomainList(
                mongoRepository.findByPuestoAndCodigoPeriodoBetween(
                        normalizeString(puesto), periodoInicio, periodoFin)
        );
    }

    @Override
    public List<DatoCompensacion> findByFuncionSap(int funcionSap) {
        return mapper.toDomainList(
                mongoRepository.findByFuncionSap(funcionSap)
        );
    }

    @Override
    public List<DatoCompensacion> findByFuncionSapAndPeriodoBetween(int funcionSap, int periodoInicio, int periodoFin) {
        return mapper.toDomainList(
                mongoRepository.findByFuncionSapAndCodigoPeriodoBetween(funcionSap, periodoInicio, periodoFin)
        );
    }

    @Override
    public List<DatoCompensacion> findByNegocioAndIndicadorAndPeriodoBetween(
            String negocio, String indicador, int periodoInicio, int periodoFin) {
        return mapper.toDomainList(
                mongoRepository.findByNegocioAndIndicadorAndCodigoPeriodoBetween(
                        normalizeString(negocio), normalizeString(indicador), periodoInicio, periodoFin)
        );
    }

    @Override
    public List<DatoCompensacion> findByNegocioAndPuestoAndPeriodoBetween(
            String negocio, String puesto, int periodoInicio, int periodoFin) {
        return mapper.toDomainList(
                mongoRepository.findByNegocioAndPuestoAndCodigoPeriodoBetween(
                        normalizeString(negocio), normalizeString(puesto), periodoInicio, periodoFin)
        );
    }

    @Override
    public List<DatoCompensacion> findByNegocioAndPuestoAndPeriodo(
            String negocio, String puesto, int codigoPeriodo) {
        return mapper.toDomainList(
                mongoRepository.findByNegocioAndPuestoAndCodigoPeriodo(
                        normalizeString(negocio), normalizeString(puesto), codigoPeriodo)
        );
    }

    @Override
    public List<DatoCompensacion> findByNegocioAndPuestoAndIndicadorAndPeriodoBetween(
            String negocio, String puesto, String indicador, int periodoInicio, int periodoFin) {
        return mapper.toDomainList(
                mongoRepository.findByNegocioAndPuestoAndIndicadorAndCodigoPeriodoBetween(
                        normalizeString(negocio), normalizeString(puesto), normalizeString(indicador), periodoInicio, periodoFin)
        );
    }

    @Override
    public List<String> findDistinctNegocios() {
        return mongoRepository.findDistinctNegocios();
    }

    @Override
    public List<String> findDistinctPuestosByNegocio(String negocio) {
        return mongoRepository.findDistinctPuestosByNegocio(normalizeString(negocio));
    }

    @Override
    public List<Integer> findDistinctPeriodos() {
        return mongoRepository.findDistinctPeriodos();
    }

    @Override
    public List<String> findDistinctIndicadores() {
        return mongoRepository.findDistinctIndicadores();
    }

    @Override
    public List<String> findDistinctIndicadoresByNegocio(String negocio) {
        return mongoRepository.findDistinctIndicadoresByNegocio(normalizeString(negocio));
    }

    @Override
    public List<String> findDistinctIndicadoresByNegocioAndPuesto(String negocio, String puesto) {
        return mongoRepository.findDistinctIndicadoresByNegocioAndPuesto(
                normalizeString(negocio), normalizeString(puesto));
    }

    @Override
    public Optional<Integer> findMaxPeriodo() {
        return mongoRepository.findTopByOrderByCodigoPeriodoDesc()
                .map(DatoCompensacionDocument::getCodigoPeriodo);
    }

    @Override
    public void deleteAll() {
        log.warn("Eliminando todos los registros de datos_compensacion");
        mongoRepository.deleteAll();
    }

    @Override
    public long count() {
        return mongoRepository.count();
    }

    /**
     * Normaliza strings removiendo espacios extra
     */
    private String normalizeString(String value) {
        if (value == null) return null;
        return value.trim();
    }
}
