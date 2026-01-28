package com.elektra.resumennomina.application.service;

import com.elektra.resumennomina.domain.model.Periodo;
import com.elektra.resumennomina.domain.model.ResumenSemanal;
import com.elektra.resumennomina.domain.port.input.CompensacionUseCase;
import com.elektra.resumennomina.domain.port.input.ExportacionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Servicio para exportar datos a diferentes formatos (CSV, Excel, PDF).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportacionService implements ExportacionUseCase {

    private final CompensacionUseCase compensacionUseCase;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private final NumberFormat percentFormat = NumberFormat.getPercentInstance(new Locale("es", "MX"));

    @Override
    public byte[] exportarTablaResultados(List<ResumenSemanal> datos, FormatoExportacion formato) {
        return switch (formato) {
            case CSV -> exportarACSV(datos);
            case EXCEL -> exportarAExcel(datos);
            case PDF -> exportarAPDF(datos);
        };
    }

    @Override
    public byte[] exportarTablaResultadosTotal(Periodo periodo, FormatoExportacion formato) {
        List<ResumenSemanal> datos = compensacionUseCase.obtenerTablaResultadosTotal(periodo);
        return exportarTablaResultados(datos, formato);
    }

    @Override
    public byte[] exportarTablaResultadosPromedio(Periodo periodo, FormatoExportacion formato) {
        List<ResumenSemanal> datos = compensacionUseCase.obtenerTablaResultadosPromedio(periodo);
        return exportarTablaResultados(datos, formato);
    }

    @Override
    public String getContentType(FormatoExportacion formato) {
        return switch (formato) {
            case CSV -> "text/csv";
            case EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case PDF -> "application/pdf";
        };
    }

    @Override
    public String getFileExtension(FormatoExportacion formato) {
        return switch (formato) {
            case CSV -> ".csv";
            case EXCEL -> ".xlsx";
            case PDF -> ".pdf";
        };
    }

    // ==================== CSV ====================

    private byte[] exportarACSV(List<ResumenSemanal> datos) {
        log.info("Exportando {} registros a CSV", datos.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            // BOM para Excel
            writer.write('\ufeff');

            // Header
            writer.println("Puesto,Negocio,Semana Actual,Semana Anterior,Variación $,Variación %");

            // Datos
            for (ResumenSemanal resumen : datos) {
                writer.printf("\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f%%%n",
                        escapeCSV(resumen.getPuesto()),
                        escapeCSV(resumen.getNegocio()),
                        resumen.getTotalCompensacion(),
                        resumen.getTotalAnterior(),
                        resumen.getDiferenciaMonto(),
                        resumen.getVariacionPorcentual());
            }
        }

        return baos.toByteArray();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    // ==================== EXCEL ====================

    private byte[] exportarAExcel(List<ResumenSemanal> datos) {
        log.info("Exportando {} registros a Excel", datos.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Resumen de Nómina");

            // Estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle percentStylePositive = createPercentStyle(workbook, IndexedColors.GREEN);
            CellStyle percentStyleNegative = createPercentStyle(workbook, IndexedColors.RED);

            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Puesto", "Negocio", "Semana Actual", "Semana Anterior", "Variación $", "Variación %"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 1;
            String currentNegocio = "";

            for (ResumenSemanal resumen : datos) {
                // Agregar fila separadora cuando cambia el negocio
                if (!currentNegocio.equals(resumen.getNegocio())) {
                    if (rowNum > 1) {
                        rowNum++; // Fila vacia
                    }
                    Row negocioRow = sheet.createRow(rowNum++);
                    Cell negocioCell = negocioRow.createCell(0);
                    negocioCell.setCellValue(resumen.getNegocio());
                    negocioCell.setCellStyle(headerStyle);
                    currentNegocio = resumen.getNegocio();
                }

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(resumen.getPuesto());
                row.createCell(1).setCellValue(resumen.getNegocio());

                Cell cellActual = row.createCell(2);
                cellActual.setCellValue(resumen.getTotalCompensacion().doubleValue());
                cellActual.setCellStyle(currencyStyle);

                Cell cellAnterior = row.createCell(3);
                cellAnterior.setCellValue(resumen.getTotalAnterior().doubleValue());
                cellAnterior.setCellStyle(currencyStyle);

                Cell cellDiferencia = row.createCell(4);
                cellDiferencia.setCellValue(resumen.getDiferenciaMonto().doubleValue());
                cellDiferencia.setCellStyle(currencyStyle);

                Cell cellVariacion = row.createCell(5);
                cellVariacion.setCellValue(resumen.getVariacionPorcentual().doubleValue() / 100);
                cellVariacion.setCellStyle(resumen.getVariacionPorcentual().doubleValue() >= 0 ?
                        percentStylePositive : percentStyleNegative);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error al generar Excel: {}", e.getMessage());
            throw new RuntimeException("Error al generar archivo Excel", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }

    private CellStyle createPercentStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        Font font = workbook.createFont();
        font.setColor(color.getIndex());
        style.setFont(font);
        return style;
    }

    // ==================== PDF ====================

    private byte[] exportarAPDF(List<ResumenSemanal> datos) {
        log.info("Exportando {} registros a PDF", datos.size());

        // Implementacion simplificada - se puede mejorar con iText
        // Por ahora retornamos un PDF basico con la informacion

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Crear documento PDF con iText
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc =
                    new com.itextpdf.kernel.pdf.PdfDocument(
                            new com.itextpdf.kernel.pdf.PdfWriter(baos));

            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);

            // Titulo
            document.add(new com.itextpdf.layout.element.Paragraph("Resumen de Nómina")
                    .setFontSize(18)
                    .setBold());

            document.add(new com.itextpdf.layout.element.Paragraph("Grupo Elektra - Inteligencia de Datos")
                    .setFontSize(12));

            // Tabla
            float[] columnWidths = {2, 1.5f, 1.5f, 1.5f, 1.5f, 1};
            com.itextpdf.layout.element.Table table =
                    new com.itextpdf.layout.element.Table(columnWidths);

            // Headers
            table.addHeaderCell("Puesto");
            table.addHeaderCell("Negocio");
            table.addHeaderCell("Semana Actual");
            table.addHeaderCell("Semana Anterior");
            table.addHeaderCell("Variación $");
            table.addHeaderCell("Variación %");

            // Datos
            for (ResumenSemanal resumen : datos) {
                table.addCell(resumen.getPuesto() != null ? resumen.getPuesto() : "");
                table.addCell(resumen.getNegocio() != null ? resumen.getNegocio() : "");
                table.addCell(currencyFormat.format(resumen.getTotalCompensacion()));
                table.addCell(currencyFormat.format(resumen.getTotalAnterior()));
                table.addCell(currencyFormat.format(resumen.getDiferenciaMonto()));
                table.addCell(String.format("%.2f%%", resumen.getVariacionPorcentual()));
            }

            document.add(table);
            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF: {}", e.getMessage());
            throw new RuntimeException("Error al generar archivo PDF", e);
        }
    }
}
