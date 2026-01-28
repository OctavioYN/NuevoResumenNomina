package com.elektra.resumennomina.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Value Object que representa un periodo (año + semana).
 * Formato: YYYYWW (ej: 202507 = semana 7 de 2025)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Periodo implements Comparable<Periodo> {

    private int anio;
    private int semana;

    /**
     * Crea un Periodo desde el formato YYYYWW
     */
    public static Periodo fromCodigo(int codigo) {
        int anio = codigo / 100;
        int semana = codigo % 100;
        return new Periodo(anio, semana);
    }

    /**
     * Crea un Periodo desde una fecha
     */
    public static Periodo fromDate(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int anio = date.getYear();
        int semana = date.get(weekFields.weekOfWeekBasedYear());
        return new Periodo(anio, semana);
    }

    /**
     * Obtiene el periodo actual
     */
    public static Periodo actual() {
        return fromDate(LocalDate.now());
    }

    /**
     * Convierte a codigo YYYYWW
     */
    public int toCodigo() {
        return anio * 100 + semana;
    }

    /**
     * Obtiene el periodo anterior
     */
    public Periodo anterior() {
        if (semana == 1) {
            return new Periodo(anio - 1, 52);
        }
        return new Periodo(anio, semana - 1);
    }

    /**
     * Obtiene el periodo siguiente
     */
    public Periodo siguiente() {
        if (semana >= 52) {
            return new Periodo(anio + 1, 1);
        }
        return new Periodo(anio, semana + 1);
    }

    /**
     * Formatea como string legible: "2025-07"
     */
    public String toDisplayString() {
        return String.format("%d-%02d", anio, semana);
    }

    @Override
    public int compareTo(Periodo other) {
        return Integer.compare(this.toCodigo(), other.toCodigo());
    }

    @Override
    public String toString() {
        return toDisplayString();
    }
}
