package com.elektra.resumennomina.infrastructure.persistence.mapper;

import com.elektra.resumennomina.domain.model.DatoCompensacion;
import com.elektra.resumennomina.infrastructure.persistence.document.DatoCompensacionDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper para convertir entre DatoCompensacion (dominio) y DatoCompensacionDocument (persistencia).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DatoCompensacionMapper {

    @Mapping(target = "periodo", ignore = true)
    DatoCompensacion toDomain(DatoCompensacionDocument document);

    DatoCompensacionDocument toDocument(DatoCompensacion domain);

    List<DatoCompensacion> toDomainList(List<DatoCompensacionDocument> documents);

    List<DatoCompensacionDocument> toDocumentList(List<DatoCompensacion> domains);
}
