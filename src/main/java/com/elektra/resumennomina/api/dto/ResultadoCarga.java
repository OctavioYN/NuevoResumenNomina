package com.elektra.resumennomina.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con el resultado de una operación de carga de datos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoCarga {

    private boolean success;
    private String mensaje;
    private LocalDateTime fechaCarga;
    private String usuarioCarga;
    private String archivoOriginal;
    private Integer totalRegistros;
    private Integer registrosExitosos;
    private Integer registrosConError;
    private Long tiempoProcesamientoMs;
    private List<String> errores;
}
