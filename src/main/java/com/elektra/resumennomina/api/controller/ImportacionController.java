package com.elektra.resumennomina.api.controller;

import com.elektra.resumennomina.api.dto.ApiResponse;
import com.elektra.resumennomina.domain.port.input.ImportacionUseCase;
import com.elektra.resumennomina.domain.port.input.ImportacionUseCase.ResultadoImportacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controlador para importacion de datos.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/importacion")
@RequiredArgsConstructor
@Tag(name = "Importación", description = "Endpoints para importar datos desde CSV")
public class ImportacionController {

    private final ImportacionUseCase importacionUseCase;

    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importa datos desde CSV", description = "Carga un archivo CSV con datos de compensacion")
    public ResponseEntity<ApiResponse<ResultadoImportacion>> importarCSV(
            @RequestParam("archivo") MultipartFile archivo) {

        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El archivo esta vacio"));
        }

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El archivo debe ser un CSV"));
        }

        try {
            log.info("Iniciando importacion del archivo: {}", nombreArchivo);

            ResultadoImportacion resultado = importacionUseCase.importarDesdeCSV(
                    archivo.getInputStream(), nombreArchivo);

            String mensaje = String.format(
                    "Importacion completada. Procesados: %d, Exitosos: %d, Fallidos: %d",
                    resultado.registrosProcesados(),
                    resultado.registrosExitosos(),
                    resultado.registrosFallidos());

            return ResponseEntity.ok(ApiResponse.success(resultado, mensaje));

        } catch (IOException e) {
            log.error("Error al leer archivo: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al leer el archivo: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/validar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Valida un CSV sin importar", description = "Verifica la estructura del archivo sin guardar datos")
    public ResponseEntity<ApiResponse<ResultadoImportacion>> validarCSV(
            @RequestParam("archivo") MultipartFile archivo) {

        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El archivo esta vacio"));
        }

        try {
            ResultadoImportacion resultado = importacionUseCase.validarCSV(archivo.getInputStream());

            String mensaje = resultado.registrosFallidos() == 0 ?
                    "Archivo valido" :
                    String.format("Archivo con %d errores", resultado.registrosFallidos());

            return ResponseEntity.ok(ApiResponse.success(resultado, mensaje));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al leer el archivo: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/reimportar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Reimporta todos los datos", description = "Elimina datos existentes y carga nuevos")
    public ResponseEntity<ApiResponse<ResultadoImportacion>> reimportarTodo(
            @RequestParam("archivo") MultipartFile archivo) {

        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El archivo esta vacio"));
        }

        try {
            log.warn("Iniciando reimportacion completa...");

            ResultadoImportacion resultado = importacionUseCase.reimportarTodo(archivo.getInputStream());

            return ResponseEntity.ok(ApiResponse.success(resultado, "Reimportacion completada"));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }
}
