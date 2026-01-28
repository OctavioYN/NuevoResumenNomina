package com.elektra.resumennomina.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una fila del archivo DatosInteligencia
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosInteligenciaRow {

    // Campos PKI (Primary Key Identifiers)
    private Integer pkiPuesto;
    private Integer pkiSucursal;
    private Integer pkiEmpleado;
    private Integer pkiCDGenerico;
    private Integer pkiPais;
    private String pkiPeriodo;
    private Integer pkiGrupoNegocio;
    private Integer pkiCanal;
    private Integer pkiConceptoDetalle;

    // Campos FN (Fields Numeric)
    private Double fnValor;
    private Double fnDetalle1;
    private Double fnDetalle2;

    // Campo PK
    private Integer pkcDetalle3;

    // Campos FC (Fields Character/String)
    private String fcDetalle4;  // Negocio
    private String fcDetalle5;  // Puesto
    private String fcDetalle6;  // Indicador

    private Double fnDetalle7;
}
