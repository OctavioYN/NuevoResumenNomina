package com.elektra.resumennomina.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad principal del dominio que representa un dato de compensacion.
 * Corresponde a un registro del CSV de inteligencia de datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatoCompensacion {

    private String id;

    // Identificadores
    private Integer funcionSap;      // PkiPuesto -> Funcion SAP (10059, 10115, etc.)
    private Integer codigoGenerico;  // PkiCDGenerico
    private Integer codigoPais;      // PkiPais
    private Integer codigoPeriodo;   // PkiPeriodo (YYYYWW)
    private Integer grupoNegocio;    // PkiGrupoNegocio
    private Integer canal;           // PkiCanal
    private Integer conceptoDetalle; // PkiConceptoDetalle

    // Valores
    private BigDecimal valor;        // FnValor
    private BigDecimal detalle1;     // FnDetalle1
    private BigDecimal detalle2;     // FnDetalle2
    private String detalle3;         // PkcDetalle3
    private BigDecimal detalle7;     // FnDetalle7

    // Descriptores
    private String negocio;          // FcDetalle4 (Credito, Elektra, etc.)
    private String puesto;           // FcDetalle5 (AdP SF Entrenamiento, etc.)
    private String indicador;        // FcDetalle6 (Compensacion, Empleado, etc.)

    // Campos calculados/derivados
    private Periodo periodo;

    /**
     * Obtiene el periodo como objeto Periodo
     */
    public Periodo getPeriodo() {
        if (periodo == null && codigoPeriodo != null) {
            periodo = Periodo.fromCodigo(codigoPeriodo);
        }
        return periodo;
    }

    /**
     * Obtiene el enum Negocio correspondiente
     */
    public Negocio getNegocioEnum() {
        return Negocio.fromNombre(negocio).orElse(null);
    }

    /**
     * Obtiene el enum Indicador correspondiente
     */
    public Indicador getIndicadorEnum() {
        return Indicador.fromNombre(indicador).orElse(null);
    }
}
