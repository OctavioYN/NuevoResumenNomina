package com.elektra.resumennomina.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Enum que representa los negocios de Grupo Elektra.
 * Cada negocio tiene sus puestos asociados.
 */
@Getter
@RequiredArgsConstructor
public enum Negocio {

    ELEKTRA("Elektra", "elektra", List.of(
            "AdP EKT Maduro",
            "AdP EKT Entrenamiento",
            "AC Movilidad",
            "Líder Comercial"
    )),

    CREDITO("Crédito", "credito", List.of(
            "AdP SF Maduro",
            "AdP SF Entrenamiento",
            "ASF",
            "Líder Financiero EKT",
            "DAZ"
    )),

    CAJEROS("Cajeros", "cajeros", List.of(
            "Cajero Supervisor",
            "Supervisor Máquinas",
            "Senior",
            "Universal",
            "Tiempo Parcial",
            "Líder de Operaciones"
    )),

    GERENTES("Gerentes", "gerentes", List.of(
            "Gerente EKT",
            "DAZ"
    )),

    PRESTA_PRENDA("Presta Prenda", "presta-prenda", List.of(
            "Líder de Tienda",
            "Valuador C",
            "Valuador PP1",
            "Plus PP",
            "Vendedor",
            "Gerente",
            "Líder de Préstamos Maduro",
            "Entrenamiento",
            "Promotor de Préstamos",
            "Promotor de Préstamos CAM"
    )),

    CDT("CDT", "cdt", List.of(
            "Cajero Supervisor",
            "AdC Entrenamiento",
            "AdC Autoservicio",
            "Movilidad",
            "Gerente Autoservicio"
    )),

    SUCURSAL_VIRTUAL("Sucursal Virtual", "sucursal-virtual", List.of(
            "AC Maduro",
            "Entrenamiento",
            "Líder"
    )),

    COBRANZA("Cobranza", "cobranza", List.of(
            "GCR",
            "LCR",
            "GCV",
            "LCV",
            "GCA",
            "GPI",
            "LPI"
    ));

    private final String nombre;
    private final String slug;
    private final List<String> puestos;

    public static Optional<Negocio> fromNombre(String nombre) {
        return Arrays.stream(values())
                .filter(n -> n.getNombre().equalsIgnoreCase(nombre.trim()))
                .findFirst();
    }

    public static Optional<Negocio> fromSlug(String slug) {
        return Arrays.stream(values())
                .filter(n -> n.getSlug().equalsIgnoreCase(slug.trim()))
                .findFirst();
    }

    public boolean contienePuesto(String puesto) {
        return puestos.stream()
                .anyMatch(p -> puesto.toLowerCase().contains(p.toLowerCase()));
    }
}
