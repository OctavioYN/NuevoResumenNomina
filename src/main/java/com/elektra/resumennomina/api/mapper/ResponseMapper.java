package com.elektra.resumennomina.api.mapper;

import com.elektra.resumennomina.api.dto.*;
import com.elektra.resumennomina.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper para convertir modelos de dominio a DTOs de respuesta.
 */
@Component
public class ResponseMapper {

    public ResumenNacionalResponse toResumenNacionalResponse(
            Periodo periodo,
            ResumenSemanal resumenGeneral,
            List<ResumenSemanal> resumenPorNegocio) {

        List<ResumenNacionalResponse.ResumenNegocioDto> negocios = resumenPorNegocio.stream()
                .map(r -> ResumenNacionalResponse.ResumenNegocioDto.builder()
                        .negocio(r.getNegocio())
                        .slug(toSlug(r.getNegocio()))
                        .semanaActual(r.getTotalCompensacion())
                        .semanaAnterior(r.getTotalAnterior())
                        .diferencia(r.getDiferenciaMonto())
                        .variacionPorcentual(r.getVariacionPorcentual())
                        .empleados(r.getCantidadEmpleados())
                        .build())
                .toList();

        return ResumenNacionalResponse.builder()
                .periodoActual(periodo.toDisplayString())
                .periodoAnterior(periodo.anterior().toDisplayString())
                .totalSemanaActual(resumenGeneral.getTotalCompensacion())
                .totalSemanaAnterior(resumenGeneral.getTotalAnterior())
                .diferencia(resumenGeneral.getDiferenciaMonto())
                .variacionPorcentual(resumenGeneral.getVariacionPorcentual())
                .negocios(negocios)
                .build();
    }

    public SerieTemporalResponse toSerieTemporalResponse(EstadisticasSerie estadisticas) {
        List<SerieTemporalResponse.PuntoSerieDto> puntos = estadisticas.getPuntos() != null ?
                estadisticas.getPuntos().stream()
                        .map(p -> SerieTemporalResponse.PuntoSerieDto.builder()
                                .semana(p.getPeriodo().toDisplayString())
                                .valor(p.getValor())
                                .variacionVsSemanaAnterior(p.getVariacionVsSemanaAnterior())
                                .fueraDe1Sigma(p.estaFuera1Sigma(
                                        estadisticas.getLimiteSuperior1Sigma(),
                                        estadisticas.getLimiteInferior1Sigma()))
                                .fueraDe15Sigma(p.estaFuera15Sigma(
                                        estadisticas.getLimiteSuperior15Sigma(),
                                        estadisticas.getLimiteInferior15Sigma()))
                                .build())
                        .toList() : List.of();

        return SerieTemporalResponse.builder()
                .negocio(estadisticas.getNegocio())
                .puesto(estadisticas.getPuesto())
                .indicador(estadisticas.getIndicador())
                .media(estadisticas.getMedia())
                .desviacionEstandar(estadisticas.getDesviacionEstandar())
                .minimo(estadisticas.getMinimo())
                .maximo(estadisticas.getMaximo())
                .cantidadPuntos(estadisticas.getCantidadPuntos())
                .lineasConfianza(SerieTemporalResponse.LineasConfianza.builder()
                        .limiteSuperior1Sigma(estadisticas.getLimiteSuperior1Sigma())
                        .limiteInferior1Sigma(estadisticas.getLimiteInferior1Sigma())
                        .limiteSuperior15Sigma(estadisticas.getLimiteSuperior15Sigma())
                        .limiteInferior15Sigma(estadisticas.getLimiteInferior15Sigma())
                        .build())
                .serie(puntos)
                .build();
    }

    public MapaCalorResponse toMapaCalorResponse(Periodo periodo, List<MapaCalorItem> items) {
        List<MapaCalorResponse.ItemMapaCalor> itemsDto = items.stream()
                .map(i -> MapaCalorResponse.ItemMapaCalor.builder()
                        .negocio(i.getNegocio())
                        .puesto(i.getPuesto())
                        .variacionPorcentual(i.getVariacionPorcentual())
                        .valorAbsoluto(i.getValorAbsoluto())
                        .esPositivo(i.esPositiva())
                        .intensidadColor(i.getIntensidadColor())
                        .valorSemanaActual(i.getValorSemanaActual())
                        .valorSemanaAnterior(i.getValorSemanaAnterior())
                        .build())
                .toList();

        Map<String, List<MapaCalorResponse.ItemMapaCalor>> porNegocio = itemsDto.stream()
                .collect(Collectors.groupingBy(MapaCalorResponse.ItemMapaCalor::getNegocio));

        return MapaCalorResponse.builder()
                .periodoActual(periodo.toDisplayString())
                .periodoAnterior(periodo.anterior().toDisplayString())
                .items(itemsDto)
                .itemsPorNegocio(porNegocio)
                .build();
    }

    public TablaResultadosResponse toTablaResultadosResponse(
            Periodo periodo,
            List<ResumenSemanal> datos,
            TablaResultadosResponse.TipoTabla tipo) {

        List<TablaResultadosResponse.FilaResultado> filas = datos.stream()
                .map(r -> TablaResultadosResponse.FilaResultado.builder()
                        .puesto(r.getPuesto())
                        .negocio(r.getNegocio())
                        .semanaActual(r.getTotalCompensacion())
                        .semanaAnterior(r.getTotalAnterior())
                        .variacionMonto(r.getDiferenciaMonto())
                        .variacionPorcentual(r.getVariacionPorcentual())
                        .variacionPositiva(r.getVariacionPorcentual() != null &&
                                r.getVariacionPorcentual().doubleValue() >= 0)
                        .build())
                .toList();

        Map<String, List<TablaResultadosResponse.FilaResultado>> porNegocio = filas.stream()
                .collect(Collectors.groupingBy(TablaResultadosResponse.FilaResultado::getNegocio));

        return TablaResultadosResponse.builder()
                .periodoActual(periodo.toDisplayString())
                .periodoAnterior(periodo.anterior().toDisplayString())
                .tipo(tipo)
                .filas(filas)
                .filasPorNegocio(porNegocio)
                .build();
    }

    private String toSlug(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .trim()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[ñ]", "n")
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
