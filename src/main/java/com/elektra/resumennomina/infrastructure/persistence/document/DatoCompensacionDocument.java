package com.elektra.resumennomina.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

/**
 * Documento MongoDB que representa un dato de compensacion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "datos_compensacion")
@CompoundIndexes({
        @CompoundIndex(name = "idx_negocio_periodo", def = "{'negocio': 1, 'codigoPeriodo': 1}"),
        @CompoundIndex(name = "idx_puesto_periodo", def = "{'puesto': 1, 'codigoPeriodo': 1}"),
        @CompoundIndex(name = "idx_funcionSap_periodo", def = "{'funcionSap': 1, 'codigoPeriodo': 1}"),
        @CompoundIndex(name = "idx_negocio_indicador_periodo", def = "{'negocio': 1, 'indicador': 1, 'codigoPeriodo': 1}"),
        @CompoundIndex(name = "idx_negocio_puesto_periodo", def = "{'negocio': 1, 'puesto': 1, 'codigoPeriodo': 1}")
})
public class DatoCompensacionDocument {

    @Id
    private String id;

    @Indexed
    @Field("funcionSap")
    private Integer funcionSap;

    @Field("codigoGenerico")
    private Integer codigoGenerico;

    @Field("codigoPais")
    private Integer codigoPais;

    @Indexed
    @Field("codigoPeriodo")
    private Integer codigoPeriodo;

    @Field("grupoNegocio")
    private Integer grupoNegocio;

    @Field("canal")
    private Integer canal;

    @Field("conceptoDetalle")
    private Integer conceptoDetalle;

    @Field("valor")
    private BigDecimal valor;

    @Field("detalle1")
    private BigDecimal detalle1;

    @Field("detalle2")
    private BigDecimal detalle2;

    @Field("detalle3")
    private String detalle3;

    @Field("detalle7")
    private BigDecimal detalle7;

    @Indexed
    @Field("negocio")
    private String negocio;

    @Indexed
    @Field("puesto")
    private String puesto;

    @Indexed
    @Field("indicador")
    private String indicador;
}
