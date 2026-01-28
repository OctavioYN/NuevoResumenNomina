package com.elektra.resumennomina.domain.port.input;

import com.elektra.resumennomina.domain.model.Periodo;
import com.elektra.resumennomina.domain.model.ResumenSemanal;

import java.util.List;

/**
 * Puerto de entrada para la exportacion de reportes.
 */
public interface ExportacionUseCase {

    /**
     * Tipos de formato de exportacion soportados
     */
    enum FormatoExportacion {
        CSV, EXCEL, PDF
    }

    /**
     * Exporta la tabla de resultados
     */
    byte[] exportarTablaResultados(List<ResumenSemanal> datos, FormatoExportacion formato);

    /**
     * Exporta la tabla de resultados totales del periodo
     */
    byte[] exportarTablaResultadosTotal(Periodo periodo, FormatoExportacion formato);

    /**
     * Exporta la tabla de resultados promedios del periodo
     */
    byte[] exportarTablaResultadosPromedio(Periodo periodo, FormatoExportacion formato);

    /**
     * Obtiene el content-type para el formato especificado
     */
    String getContentType(FormatoExportacion formato);

    /**
     * Obtiene la extension de archivo para el formato especificado
     */
    String getFileExtension(FormatoExportacion formato);
}
