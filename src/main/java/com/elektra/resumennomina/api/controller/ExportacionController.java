package com.elektra.resumennomina.api.controller;

import com.elektra.resumennomina.domain.model.Periodo;
import com.elektra.resumennomina.domain.port.input.CompensacionUseCase;
import com.elektra.resumennomina.domain.port.input.ExportacionUseCase;
import com.elektra.resumennomina.domain.port.input.ExportacionUseCase.FormatoExportacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para exportacion de reportes.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/exportacion")
@RequiredArgsConstructor
@Tag(name = "Exportación", description = "Endpoints para exportar reportes")
public class ExportacionController {

    private final ExportacionUseCase exportacionUseCase;
    private final CompensacionUseCase compensacionUseCase;

    @GetMapping("/tabla-resultados/total")
    @Operation(summary = "Exporta tabla de resultados totales")
    public ResponseEntity<byte[]> exportarTablaResultadosTotal(
            @RequestParam(required = false) Integer periodo,
            @RequestParam(defaultValue = "EXCEL") String formato) {

        Periodo periodoObj = periodo != null ?
                Periodo.fromCodigo(periodo) :
                compensacionUseCase.obtenerPeriodoActual();

        FormatoExportacion formatoEnum = parseFormato(formato);

        byte[] contenido = exportacionUseCase.exportarTablaResultadosTotal(periodoObj, formatoEnum);

        String nombreArchivo = String.format("resumen_total_%s%s",
                periodoObj.toDisplayString().replace("-", ""),
                exportacionUseCase.getFileExtension(formatoEnum));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.parseMediaType(exportacionUseCase.getContentType(formatoEnum)))
                .body(contenido);
    }

    @GetMapping("/tabla-resultados/promedio")
    @Operation(summary = "Exporta tabla de resultados promedios")
    public ResponseEntity<byte[]> exportarTablaResultadosPromedio(
            @RequestParam(required = false) Integer periodo,
            @RequestParam(defaultValue = "EXCEL") String formato) {

        Periodo periodoObj = periodo != null ?
                Periodo.fromCodigo(periodo) :
                compensacionUseCase.obtenerPeriodoActual();

        FormatoExportacion formatoEnum = parseFormato(formato);

        byte[] contenido = exportacionUseCase.exportarTablaResultadosPromedio(periodoObj, formatoEnum);

        String nombreArchivo = String.format("resumen_promedio_%s%s",
                periodoObj.toDisplayString().replace("-", ""),
                exportacionUseCase.getFileExtension(formatoEnum));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.parseMediaType(exportacionUseCase.getContentType(formatoEnum)))
                .body(contenido);
    }

    private FormatoExportacion parseFormato(String formato) {
        try {
            return FormatoExportacion.valueOf(formato.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FormatoExportacion.EXCEL;
        }
    }
}
