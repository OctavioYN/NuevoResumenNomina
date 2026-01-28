package com.elektra.resumennomina.application.service;

import com.elektra.resumennomina.api.dto.DatosInteligenciaRow;
import com.elektra.resumennomina.api.dto.ResultadoCarga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para administrar la carga de datos de inteligencia
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCargaService {

    private static final String COLLECTION_NAME = "datos_compensacion";

    private final MongoTemplate mongoTemplate;
    private final CsvProcessorService csvProcessor;

    /**
     * Carga datos reemplazando todos los existentes
     */
    public ResultadoCarga cargarDatosReemplazar(MultipartFile file, String usuario) {
        log.info("Iniciando carga COMPLETA por usuario: {}", usuario);

        long inicio = System.currentTimeMillis();
        ResultadoCarga resultado = ResultadoCarga.builder()
                .fechaCarga(LocalDateTime.now())
                .usuarioCarga(usuario)
                .archivoOriginal(file.getOriginalFilename())
                .errores(new ArrayList<>())
                .build();

        try {
            List<DatosInteligenciaRow> datos = procesarArchivo(file);
            resultado.setTotalRegistros(datos.size());

            if (datos.isEmpty()) {
                resultado.setSuccess(false);
                resultado.setMensaje("No se encontraron registros válidos en el archivo");
                return resultado;
            }

            // Eliminar todos los datos existentes
            log.warn("Eliminando registros existentes de {}...", COLLECTION_NAME);
            long eliminados = mongoTemplate.getCollection(COLLECTION_NAME).countDocuments();
            mongoTemplate.dropCollection(COLLECTION_NAME);
            log.info("Eliminados {} registros", eliminados);

            // Convertir e insertar
            List<Document> documents = datos.stream()
                    .map(this::convertirADocument)
                    .collect(Collectors.toList());

            mongoTemplate.getCollection(COLLECTION_NAME).insertMany(documents);

            resultado.setRegistrosExitosos(documents.size());
            resultado.setSuccess(true);
            resultado.setMensaje("Carga completa: " + documents.size() + " registros insertados");

            long fin = System.currentTimeMillis();
            resultado.setTiempoProcesamientoMs(fin - inicio);

            log.info("Carga completada en {} ms", resultado.getTiempoProcesamientoMs());

        } catch (Exception e) {
            log.error("Error durante la carga: {}", e.getMessage(), e);
            resultado.setSuccess(false);
            resultado.setMensaje("Error durante la carga: " + e.getMessage());
            resultado.getErrores().add(e.getMessage());
        }

        return resultado;
    }

    /**
     * Carga datos de forma incremental (agregar/actualizar)
     */
    public ResultadoCarga cargarDatosIncremental(MultipartFile file, String usuario) {
        log.info("Iniciando carga INCREMENTAL por usuario: {}", usuario);

        long inicio = System.currentTimeMillis();
        ResultadoCarga resultado = ResultadoCarga.builder()
                .fechaCarga(LocalDateTime.now())
                .usuarioCarga(usuario)
                .archivoOriginal(file.getOriginalFilename())
                .errores(new ArrayList<>())
                .build();

        try {
            List<DatosInteligenciaRow> datos = procesarArchivo(file);
            resultado.setTotalRegistros(datos.size());

            if (datos.isEmpty()) {
                resultado.setSuccess(false);
                resultado.setMensaje("No se encontraron registros válidos");
                return resultado;
            }

            int exitosos = 0;
            int errores = 0;

            for (DatosInteligenciaRow dato : datos) {
                try {
                    Document doc = convertirADocument(dato);

                    // Filtro único por: periodo + puesto + concepto + negocio
                    Document filtro = new Document()
                            .append("codigoPeriodo", Integer.parseInt(dato.getPkiPeriodo()))
                            .append("funcionSap", dato.getPkiPuesto())
                            .append("grupoNegocio", dato.getPkiGrupoNegocio())
                            .append("conceptoDetalle", dato.getPkiConceptoDetalle())
                            .append("negocio", dato.getFcDetalle4().trim())
                            .append("puesto", dato.getFcDetalle5().trim())
                            .append("indicador", dato.getFcDetalle6().trim());

                    mongoTemplate.getCollection(COLLECTION_NAME)
                            .replaceOne(filtro, doc,
                                    new com.mongodb.client.model.ReplaceOptions().upsert(true));

                    exitosos++;
                } catch (Exception e) {
                    errores++;
                    resultado.getErrores().add("Error insertando registro: " + e.getMessage());
                }
            }

            resultado.setRegistrosExitosos(exitosos);
            resultado.setRegistrosConError(errores);
            resultado.setSuccess(errores == 0);
            resultado.setMensaje(String.format("Carga incremental: %d exitosos, %d errores", exitosos, errores));

            long fin = System.currentTimeMillis();
            resultado.setTiempoProcesamientoMs(fin - inicio);

        } catch (Exception e) {
            log.error("Error durante la carga incremental: {}", e.getMessage(), e);
            resultado.setSuccess(false);
            resultado.setMensaje("Error: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Carga datos solo para un periodo específico (reemplaza solo ese periodo)
     */
    public ResultadoCarga cargarDatosPorPeriodo(MultipartFile file, String usuario, Integer periodo) {
        log.info("Iniciando carga por PERIODO {} por usuario: {}", periodo, usuario);

        long inicio = System.currentTimeMillis();
        ResultadoCarga resultado = ResultadoCarga.builder()
                .fechaCarga(LocalDateTime.now())
                .usuarioCarga(usuario)
                .archivoOriginal(file.getOriginalFilename())
                .errores(new ArrayList<>())
                .build();

        try {
            List<DatosInteligenciaRow> datos = procesarArchivo(file);

            // Filtrar solo los datos del periodo especificado
            List<DatosInteligenciaRow> datosPeriodo = datos.stream()
                    .filter(d -> periodo.toString().equals(d.getPkiPeriodo()))
                    .collect(Collectors.toList());

            resultado.setTotalRegistros(datosPeriodo.size());

            if (datosPeriodo.isEmpty()) {
                resultado.setSuccess(false);
                resultado.setMensaje("No se encontraron registros para el periodo " + periodo);
                return resultado;
            }

            // Eliminar datos existentes del periodo
            Query query = new Query(Criteria.where("codigoPeriodo").is(periodo));
            long eliminados = mongoTemplate.remove(query, COLLECTION_NAME).getDeletedCount();
            log.info("Eliminados {} registros del periodo {}", eliminados, periodo);

            // Insertar nuevos datos
            List<Document> documents = datosPeriodo.stream()
                    .map(this::convertirADocument)
                    .collect(Collectors.toList());

            mongoTemplate.getCollection(COLLECTION_NAME).insertMany(documents);

            resultado.setRegistrosExitosos(documents.size());
            resultado.setSuccess(true);
            resultado.setMensaje(String.format("Periodo %d: %d registros reemplazados", periodo, documents.size()));

            long fin = System.currentTimeMillis();
            resultado.setTiempoProcesamientoMs(fin - inicio);

        } catch (Exception e) {
            log.error("Error durante la carga por periodo: {}", e.getMessage(), e);
            resultado.setSuccess(false);
            resultado.setMensaje("Error: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Elimina todos los datos de un periodo
     */
    public ResultadoCarga eliminarPorPeriodo(Integer periodo, String usuario) {
        log.info("Eliminando datos del periodo: {} por usuario: {}", periodo, usuario);

        ResultadoCarga resultado = ResultadoCarga.builder()
                .fechaCarga(LocalDateTime.now())
                .usuarioCarga(usuario)
                .errores(new ArrayList<>())
                .build();

        try {
            Query query = new Query(Criteria.where("codigoPeriodo").is(periodo));
            long eliminados = mongoTemplate.remove(query, COLLECTION_NAME).getDeletedCount();

            resultado.setSuccess(true);
            resultado.setTotalRegistros((int) eliminados);
            resultado.setMensaje("Eliminados " + eliminados + " registros del periodo " + periodo);

        } catch (Exception e) {
            resultado.setSuccess(false);
            resultado.setMensaje("Error: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Obtiene estadísticas de los datos cargados, agrupadas por año
     */
    public Document obtenerEstadisticas() {
        long totalRegistros = mongoTemplate.getCollection(COLLECTION_NAME).countDocuments();

        // Obtener periodos únicos
        List<Integer> periodos = mongoTemplate.getCollection(COLLECTION_NAME)
                .distinct("codigoPeriodo", Integer.class)
                .into(new ArrayList<>());

        // Obtener negocios únicos
        List<String> negocios = mongoTemplate.getCollection(COLLECTION_NAME)
                .distinct("negocio", String.class)
                .into(new ArrayList<>());

        // Agrupar estadísticas por año
        Map<Integer, List<Integer>> periodosPorAnio = periodos.stream()
                .collect(Collectors.groupingBy(p -> p / 100)); // YYYYWW / 100 = YYYY

        List<Document> estadisticasPorAnio = new ArrayList<>();

        for (Map.Entry<Integer, List<Integer>> entry : periodosPorAnio.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList())) {

            int anio = entry.getKey();
            List<Integer> periodosDelAnio = entry.getValue();

            // Contar registros del año
            long registrosAnio = 0;
            for (Integer periodo : periodosDelAnio) {
                registrosAnio += mongoTemplate.getCollection(COLLECTION_NAME)
                        .countDocuments(new Document("codigoPeriodo", periodo));
            }

            // Obtener negocios del año
            List<String> negociosAnio = new ArrayList<>();
            for (Integer periodo : periodosDelAnio) {
                List<String> negociosPeriodo = mongoTemplate.getCollection(COLLECTION_NAME)
                        .distinct("negocio", new Document("codigoPeriodo", periodo), String.class)
                        .into(new ArrayList<>());
                for (String neg : negociosPeriodo) {
                    if (!negociosAnio.contains(neg)) {
                        negociosAnio.add(neg);
                    }
                }
            }
            Collections.sort(negociosAnio);

            estadisticasPorAnio.add(new Document()
                    .append("anio", anio)
                    .append("totalRegistros", registrosAnio)
                    .append("totalPeriodos", periodosDelAnio.size())
                    .append("periodos", periodosDelAnio.stream().sorted().collect(Collectors.toList()))
                    .append("totalNegocios", negociosAnio.size())
                    .append("negocios", negociosAnio));
        }

        return new Document()
                .append("estadisticasPorAnio", estadisticasPorAnio)
                .append("totales", new Document()
                        .append("totalRegistros", totalRegistros)
                        .append("totalPeriodos", periodos.size())
                        .append("totalNegocios", negocios.size())
                        .append("negocios", negocios.stream().sorted().collect(Collectors.toList())));
    }

    private List<DatosInteligenciaRow> procesarArchivo(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("Nombre de archivo no válido");
        }

        if (filename.endsWith(".csv")) {
            return csvProcessor.procesarCsv(file);
        } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return csvProcessor.procesarExcel(file);
        } else {
            throw new IllegalArgumentException("Formato de archivo no soportado. Use CSV o Excel (.xlsx)");
        }
    }

    private Document convertirADocument(DatosInteligenciaRow dato) {
        return new Document()
                .append("funcionSap", dato.getPkiPuesto())
                .append("codigoGenerico", dato.getPkiCDGenerico())
                .append("codigoPais", dato.getPkiPais())
                .append("codigoPeriodo", Integer.parseInt(dato.getPkiPeriodo()))
                .append("grupoNegocio", dato.getPkiGrupoNegocio())
                .append("canal", dato.getPkiCanal())
                .append("conceptoDetalle", dato.getPkiConceptoDetalle())
                .append("valor", dato.getFnValor())
                .append("detalle1", dato.getFnDetalle1())
                .append("detalle2", dato.getFnDetalle2())
                .append("detalle3", dato.getPkcDetalle3() != null ? dato.getPkcDetalle3().toString() : "0")
                .append("detalle7", dato.getFnDetalle7())
                .append("negocio", dato.getFcDetalle4().trim())
                .append("puesto", dato.getFcDetalle5().trim())
                .append("indicador", dato.getFcDetalle6().trim());
    }
}
