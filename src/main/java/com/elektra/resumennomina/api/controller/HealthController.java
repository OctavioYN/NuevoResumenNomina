package com.elektra.resumennomina.api.controller;

import com.elektra.resumennomina.api.dto.ApiResponse;
import com.elektra.resumennomina.domain.port.output.DatoCompensacionRepository;
import com.elektra.resumennomina.infrastructure.config.properties.AppProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controlador para health checks y estado de la aplicacion.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Endpoints de estado de la aplicacion")
public class HealthController {

    private final AppProperties appProperties;
    private final Environment environment;
    private final DatoCompensacionRepository repository;

    @GetMapping("/health")
    @Operation(summary = "Estado de salud de la aplicacion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> status = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "application", appProperties.getName(),
                "version", appProperties.getVersion(),
                "profile", String.join(", ", environment.getActiveProfiles())
        );

        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/info")
    @Operation(summary = "Informacion de la aplicacion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> info() {
        long totalRegistros = repository.count();

        Map<String, Object> info = Map.of(
                "application", appProperties.getName(),
                "version", appProperties.getVersion(),
                "environment", String.join(", ", environment.getActiveProfiles()),
                "totalRegistros", totalRegistros,
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
