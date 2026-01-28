package com.elektra.resumennomina.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum que representa los tipos de indicadores de compensacion.
 */
@Getter
@RequiredArgsConstructor
public enum Indicador {

    COMPENSACION("Compensación", "compensacion", 1001),
    EMPLEADO("Empleado", "empleado", 1011),
    USO_TARJETAS("Uso y Disposición de Tarjetas", "uso-tarjetas", 1002),
    VENTA_CREDITO("Venta con Préstamos", "venta-credito", 1003),
    VENTA_CONTADO("Venta a Contado", "venta-contado", 1004),
    CAPITAL_ATRASO("Capital en Atraso de 6 a 7", "capital-atraso", 1005),
    CARTERA_VIGENTE("Cartera Vigente", "cartera-vigente", 1006),
    VENTA_JOYERIA("Venta de Joyería", "venta-joyeria", 1007),
    PASE_CARTERA("Pase de Cartera de 13 a 14", "pase-cartera", 1008);

    private final String nombre;
    private final String slug;
    private final int codigo;

    public static Optional<Indicador> fromNombre(String nombre) {
        return Arrays.stream(values())
                .filter(i -> nombre.toLowerCase().contains(i.getNombre().toLowerCase()))
                .findFirst();
    }

    public static Optional<Indicador> fromCodigo(int codigo) {
        return Arrays.stream(values())
                .filter(i -> i.getCodigo() == codigo)
                .findFirst();
    }
}
