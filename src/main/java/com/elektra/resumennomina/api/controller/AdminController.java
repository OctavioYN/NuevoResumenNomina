package com.elektra.resumennomina.api.controller;

import com.elektra.resumennomina.api.dto.ResultadoCarga;
import com.elektra.resumennomina.application.service.AdminCargaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controlador para administración y carga de datos
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Endpoints de administración y carga de datos")
public class AdminController {

    private final AdminCargaService adminCargaService;

    @PostMapping(value = "/carga/reemplazar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cargar datos reemplazando todos los existentes",
            description = "Elimina todos los datos y carga los nuevos desde el archivo CSV/Excel")
    public ResponseEntity<Map<String, Object>> cargarReemplazar(
            @Parameter(description = "Archivo CSV o Excel con datos de inteligencia")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Usuario que realiza la carga")
            @RequestParam(value = "usuario", defaultValue = "sistema") String usuario) {

        log.info("Solicitud de carga completa: {} por {}", file.getOriginalFilename(), usuario);

        ResultadoCarga resultado = adminCargaService.cargarDatosReemplazar(file, usuario);

        return ResponseEntity.ok(Map.of(
                "success", resultado.isSuccess(),
                "data", resultado
        ));
    }

    @PostMapping(value = "/carga/incremental", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cargar datos de forma incremental",
            description = "Agrega o actualiza registros sin eliminar los existentes")
    public ResponseEntity<Map<String, Object>> cargarIncremental(
            @Parameter(description = "Archivo CSV o Excel con datos de inteligencia")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Usuario que realiza la carga")
            @RequestParam(value = "usuario", defaultValue = "sistema") String usuario) {

        log.info("Solicitud de carga incremental: {} por {}", file.getOriginalFilename(), usuario);

        ResultadoCarga resultado = adminCargaService.cargarDatosIncremental(file, usuario);

        return ResponseEntity.ok(Map.of(
                "success", resultado.isSuccess(),
                "data", resultado
        ));
    }

    @PostMapping(value = "/carga/periodo/{periodo}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cargar datos para un periodo específico",
            description = "Reemplaza solo los datos del periodo indicado")
    public ResponseEntity<Map<String, Object>> cargarPorPeriodo(
            @Parameter(description = "Código del periodo (YYYYWW)")
            @PathVariable Integer periodo,
            @Parameter(description = "Archivo CSV o Excel con datos de inteligencia")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Usuario que realiza la carga")
            @RequestParam(value = "usuario", defaultValue = "sistema") String usuario) {

        log.info("Solicitud de carga para periodo {}: {} por {}", periodo, file.getOriginalFilename(), usuario);

        ResultadoCarga resultado = adminCargaService.cargarDatosPorPeriodo(file, usuario, periodo);

        return ResponseEntity.ok(Map.of(
                "success", resultado.isSuccess(),
                "data", resultado
        ));
    }

    @DeleteMapping("/periodo/{periodo}")
    @Operation(summary = "Eliminar datos de un periodo",
            description = "Elimina todos los registros del periodo especificado")
    public ResponseEntity<Map<String, Object>> eliminarPeriodo(
            @Parameter(description = "Código del periodo (YYYYWW)")
            @PathVariable Integer periodo,
            @Parameter(description = "Usuario que realiza la eliminación")
            @RequestParam(value = "usuario", defaultValue = "sistema") String usuario) {

        log.info("Solicitud de eliminación del periodo {} por {}", periodo, usuario);

        ResultadoCarga resultado = adminCargaService.eliminarPorPeriodo(periodo, usuario);

        return ResponseEntity.ok(Map.of(
                "success", resultado.isSuccess(),
                "data", resultado
        ));
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas de datos",
            description = "Retorna información sobre los datos cargados")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Document estadisticas = adminCargaService.obtenerEstadisticas();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", estadisticas
        ));
    }
}
