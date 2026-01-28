package com.elektra.resumennomina.application.service;

import com.elektra.resumennomina.api.dto.DatosInteligenciaRow;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para procesar archivos CSV y Excel de DatosInteligencia
 */
@Slf4j
@Service
public class CsvProcessorService {

    /**
     * Procesa archivo CSV de DatosInteligencia
     */
    public List<DatosInteligenciaRow> procesarCsv(MultipartFile file) throws IOException, CsvValidationException {
        log.info("Procesando CSV: {}", file.getOriginalFilename());

        List<DatosInteligenciaRow> datos = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                .withSkipLines(1) // Saltar header
                .build()) {

            String[] linea;
            int fila = 1;

            while ((linea = reader.readNext()) != null) {
                fila++;
                try {
                    if (linea.length < 17) {
                        log.warn("Fila {}: Columnas insuficientes ({})", fila, linea.length);
                        continue;
                    }

                    DatosInteligenciaRow dato = DatosInteligenciaRow.builder()
                            .pkiPuesto(parseInteger(linea[0]))
                            .pkiSucursal(parseInteger(linea[1]))
                            .pkiEmpleado(parseInteger(linea[2]))
                            .pkiCDGenerico(parseInteger(linea[3]))
                            .pkiPais(parseInteger(linea[4]))
                            .pkiPeriodo(linea[5].trim())
                            .pkiGrupoNegocio(parseInteger(linea[6]))
                            .pkiCanal(parseInteger(linea[7]))
                            .pkiConceptoDetalle(parseInteger(linea[8]))
                            .fnValor(parseDouble(linea[9]))
                            .fnDetalle1(parseDouble(linea[10]))
                            .fnDetalle2(parseDouble(linea[11]))
                            .pkcDetalle3(parseInteger(linea[12]))
                            .fcDetalle4(linea[13].trim())  // Negocio
                            .fcDetalle5(linea[14].trim())  // Puesto
                            .fcDetalle6(linea[15].trim())  // Indicador
                            .fnDetalle7(parseDouble(linea[16]))
                            .build();

                    if (validarDato(dato, fila)) {
                        datos.add(dato);
                    }

                } catch (Exception e) {
                    log.error("Error en fila {}: {}", fila, e.getMessage());
                }
            }
        }

        log.info("Procesados {} registros del CSV", datos.size());
        return datos;
    }

    /**
     * Procesa archivo Excel de DatosInteligencia
     */
    public List<DatosInteligenciaRow> procesarExcel(MultipartFile file) throws IOException {
        log.info("Procesando Excel: {}", file.getOriginalFilename());

        List<DatosInteligenciaRow> datos = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    DatosInteligenciaRow dato = DatosInteligenciaRow.builder()
                            .pkiPuesto(getCellAsInteger(row.getCell(0)))
                            .pkiSucursal(getCellAsInteger(row.getCell(1)))
                            .pkiEmpleado(getCellAsInteger(row.getCell(2)))
                            .pkiCDGenerico(getCellAsInteger(row.getCell(3)))
                            .pkiPais(getCellAsInteger(row.getCell(4)))
                            .pkiPeriodo(getCellAsString(row.getCell(5)))
                            .pkiGrupoNegocio(getCellAsInteger(row.getCell(6)))
                            .pkiCanal(getCellAsInteger(row.getCell(7)))
                            .pkiConceptoDetalle(getCellAsInteger(row.getCell(8)))
                            .fnValor(getCellAsDouble(row.getCell(9)))
                            .fnDetalle1(getCellAsDouble(row.getCell(10)))
                            .fnDetalle2(getCellAsDouble(row.getCell(11)))
                            .pkcDetalle3(getCellAsInteger(row.getCell(12)))
                            .fcDetalle4(getCellAsString(row.getCell(13)))
                            .fcDetalle5(getCellAsString(row.getCell(14)))
                            .fcDetalle6(getCellAsString(row.getCell(15)))
                            .fnDetalle7(getCellAsDouble(row.getCell(16)))
                            .build();

                    if (validarDato(dato, i + 1)) {
                        datos.add(dato);
                    }

                } catch (Exception e) {
                    log.error("Error en fila {}: {}", i + 1, e.getMessage());
                }
            }
        }

        log.info("Procesados {} registros del Excel", datos.size());
        return datos;
    }

    private boolean validarDato(DatosInteligenciaRow dato, int fila) {
        if (dato.getPkiPeriodo() == null || dato.getPkiPeriodo().isEmpty()) {
            log.warn("Fila {}: PkiPeriodo vacío", fila);
            return false;
        }

        if (dato.getPkiPuesto() == null || dato.getPkiPuesto() <= 0) {
            log.warn("Fila {}: PkiPuesto inválido", fila);
            return false;
        }

        if (dato.getFnValor() == null) {
            log.warn("Fila {}: FnValor vacío", fila);
            return false;
        }

        // Valores por defecto
        if (dato.getPkiSucursal() == null) dato.setPkiSucursal(0);
        if (dato.getPkiEmpleado() == null) dato.setPkiEmpleado(0);
        if (dato.getPkiCDGenerico() == null) dato.setPkiCDGenerico(0);
        if (dato.getPkiPais() == null) dato.setPkiPais(1);
        if (dato.getPkiCanal() == null) dato.setPkiCanal(0);
        if (dato.getFnDetalle1() == null) dato.setFnDetalle1(0.0);
        if (dato.getFnDetalle2() == null) dato.setFnDetalle2(0.0);
        if (dato.getPkcDetalle3() == null) dato.setPkcDetalle3(0);
        if (dato.getFnDetalle7() == null) dato.setFnDetalle7(0.0);

        return true;
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellAsString(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double num = cell.getNumericCellValue();
                yield (num == Math.floor(num)) ? String.valueOf((long) num) : String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private Integer getCellAsInteger(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private Double getCellAsDouble(Cell cell) {
        if (cell == null) return 0.0;

        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield 0.0;
                }
            }
            default -> 0.0;
        };
    }
}
