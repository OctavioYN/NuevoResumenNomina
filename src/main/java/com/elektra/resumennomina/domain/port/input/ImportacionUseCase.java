package com.elektra.resumennomina.domain.port.input;

import com.elektra.resumennomina.domain.model.DatoCompensacion;

import java.io.InputStream;
import java.util.List;

/**
 * Puerto de entrada para la importacion de datos desde CSV.
 */
public interface ImportacionUseCase {

    /**
     * Resultado de una operacion de importacion
     */
    record ResultadoImportacion(
            int registrosProcesados,
            int registrosExitosos,
            int registrosFallidos,
            List<String> errores
    ) {}

    /**
     * Importa datos desde un archivo CSV
     */
    ResultadoImportacion importarDesdeCSV(InputStream inputStream, String nombreArchivo);

    /**
     * Valida un archivo CSV sin importar
     */
    ResultadoImportacion validarCSV(InputStream inputStream);

    /**
     * Importa una lista de datos de compensacion
     */
    ResultadoImportacion importarDatos(List<DatoCompensacion> datos);

    /**
     * Elimina todos los datos y reimporta
     */
    ResultadoImportacion reimportarTodo(InputStream inputStream);
}
