package com.elektra.resumennomina.application.service;

import com.elektra.resumennomina.domain.model.DatoCompensacion;
import com.elektra.resumennomina.domain.port.input.ImportacionUseCase;
import com.elektra.resumennomina.domain.port.output.DatoCompensacionRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para importar datos desde archivos CSV.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportacionService implements ImportacionUseCase {

    private final DatoCompensacionRepository repository;

    private static final int BATCH_SIZE = 1000;

    // Indices de columnas del CSV
    private static final int COL_PKI_PUESTO = 0;
    private static final int COL_PKI_SUCURSAL = 1;
    private static final int COL_PKI_EMPLEADO = 2;
    private static final int COL_PKI_CD_GENERICO = 3;
    private static final int COL_PKI_PAIS = 4;
    private static final int COL_PKI_PERIODO = 5;
    private static final int COL_PKI_GRUPO_NEGOCIO = 6;
    private static final int COL_PKI_CANAL = 7;
    private static final int COL_PKI_CONCEPTO_DETALLE = 8;
    private static final int COL_FN_VALOR = 9;
    private static final int COL_FN_DETALLE1 = 10;
    private static final int COL_FN_DETALLE2 = 11;
    private static final int COL_PKC_DETALLE3 = 12;
    private static final int COL_FC_DETALLE4 = 13; // Negocio
    private static final int COL_FC_DETALLE5 = 14; // Puesto
    private static final int COL_FC_DETALLE6 = 15; // Indicador
    private static final int COL_FN_DETALLE7 = 16;

    @Override
    @Transactional
    public ResultadoImportacion importarDesdeCSV(InputStream inputStream, String nombreArchivo) {
        log.info("Iniciando importacion desde archivo: {}", nombreArchivo);

        List<DatoCompensacion> batch = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        int registrosProcesados = 0;
        int registrosExitosos = 0;
        int registrosFallidos = 0;

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .withSkipLines(1) // Saltar header
                .build()) {

            String[] linea;
            while ((linea = reader.readNext()) != null) {
                registrosProcesados++;

                try {
                    DatoCompensacion dato = parsearLinea(linea, registrosProcesados);
                    if (dato != null) {
                        batch.add(dato);

                        if (batch.size() >= BATCH_SIZE) {
                            repository.saveAll(batch);
                            registrosExitosos += batch.size();
                            batch.clear();
                            log.debug("Procesados {} registros...", registrosExitosos);
                        }
                    } else {
                        registrosFallidos++;
                    }
                } catch (Exception e) {
                    registrosFallidos++;
                    if (errores.size() < 100) { // Limitar cantidad de errores guardados
                        errores.add(String.format("Linea %d: %s", registrosProcesados, e.getMessage()));
                    }
                }
            }

            // Guardar ultimo batch
            if (!batch.isEmpty()) {
                repository.saveAll(batch);
                registrosExitosos += batch.size();
            }

        } catch (IOException | CsvValidationException e) {
            log.error("Error al leer archivo CSV: {}", e.getMessage());
            errores.add("Error al leer archivo: " + e.getMessage());
        }

        log.info("Importacion completada. Procesados: {}, Exitosos: {}, Fallidos: {}",
                registrosProcesados, registrosExitosos, registrosFallidos);

        return new ResultadoImportacion(registrosProcesados, registrosExitosos, registrosFallidos, errores);
    }

    @Override
    public ResultadoImportacion validarCSV(InputStream inputStream) {
        log.info("Validando archivo CSV...");

        List<String> errores = new ArrayList<>();
        int registrosProcesados = 0;
        int registrosValidos = 0;
        int registrosInvalidos = 0;

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .withSkipLines(1)
                .build()) {

            String[] linea;
            while ((linea = reader.readNext()) != null) {
                registrosProcesados++;

                try {
                    DatoCompensacion dato = parsearLinea(linea, registrosProcesados);
                    if (dato != null) {
                        registrosValidos++;
                    } else {
                        registrosInvalidos++;
                    }
                } catch (Exception e) {
                    registrosInvalidos++;
                    if (errores.size() < 100) {
                        errores.add(String.format("Linea %d: %s", registrosProcesados, e.getMessage()));
                    }
                }
            }

        } catch (IOException | CsvValidationException e) {
            errores.add("Error al leer archivo: " + e.getMessage());
        }

        return new ResultadoImportacion(registrosProcesados, registrosValidos, registrosInvalidos, errores);
    }

    @Override
    @Transactional
    public ResultadoImportacion importarDatos(List<DatoCompensacion> datos) {
        log.info("Importando {} registros...", datos.size());

        try {
            List<DatoCompensacion> guardados = repository.saveAll(datos);
            return new ResultadoImportacion(datos.size(), guardados.size(), 0, List.of());
        } catch (Exception e) {
            log.error("Error al importar datos: {}", e.getMessage());
            return new ResultadoImportacion(datos.size(), 0, datos.size(), List.of(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResultadoImportacion reimportarTodo(InputStream inputStream) {
        log.warn("Reimportando todos los datos - eliminando existentes...");
        repository.deleteAll();
        return importarDesdeCSV(inputStream, "reimportacion");
    }

    /**
     * Parsea una linea del CSV a un objeto DatoCompensacion
     */
    private DatoCompensacion parsearLinea(String[] campos, int numeroLinea) {
        if (campos.length < 17) {
            throw new IllegalArgumentException("Linea incompleta, se esperaban 17 campos");
        }

        try {
            return DatoCompensacion.builder()
                    .funcionSap(parseInteger(campos[COL_PKI_PUESTO]))
                    .codigoGenerico(parseInteger(campos[COL_PKI_CD_GENERICO]))
                    .codigoPais(parseInteger(campos[COL_PKI_PAIS]))
                    .codigoPeriodo(parseInteger(campos[COL_PKI_PERIODO]))
                    .grupoNegocio(parseInteger(campos[COL_PKI_GRUPO_NEGOCIO]))
                    .canal(parseInteger(campos[COL_PKI_CANAL]))
                    .conceptoDetalle(parseInteger(campos[COL_PKI_CONCEPTO_DETALLE]))
                    .valor(parseBigDecimal(campos[COL_FN_VALOR]))
                    .detalle1(parseBigDecimal(campos[COL_FN_DETALLE1]))
                    .detalle2(parseBigDecimal(campos[COL_FN_DETALLE2]))
                    .detalle3(campos[COL_PKC_DETALLE3])
                    .negocio(campos[COL_FC_DETALLE4] != null ? campos[COL_FC_DETALLE4].trim() : null)
                    .puesto(campos[COL_FC_DETALLE5] != null ? campos[COL_FC_DETALLE5].trim() : null)
                    .indicador(campos[COL_FC_DETALLE6] != null ? campos[COL_FC_DETALLE6].trim() : null)
                    .detalle7(parseBigDecimal(campos[COL_FN_DETALLE7]))
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parseando campos: " + e.getMessage());
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return Integer.parseInt(value.trim());
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(value.trim());
    }
}
