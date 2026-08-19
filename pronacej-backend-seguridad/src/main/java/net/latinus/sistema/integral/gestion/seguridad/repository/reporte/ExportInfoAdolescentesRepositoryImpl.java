package net.latinus.sistema.integral.gestion.seguridad.repository.reporte;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionDinamicaResultado;
import net.latinus.sistema.integral.gestion.seguridad.repository.reporte.seccion.SeccionExportacion;
import net.latinus.sistema.integral.gestion.seguridad.repository.reporte.seccion.SeccionExportacionDinamica;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Repository
public class ExportInfoAdolescentesRepositoryImpl implements ExportInfoAdolescentesRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(ExportInfoAdolescentesRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    /** Todas las secciones disponibles, inyectadas automáticamente por Spring. */
    private final List<SeccionExportacion> seccionesDisponibles;

    /**
     * Mapa de lookup: nemonico (normalizado) → sección.
     * Se construye una sola vez al iniciar el contexto.
     */
    private Map<String, SeccionExportacion> seccionesPorNemonico;

    public ExportInfoAdolescentesRepositoryImpl(List<SeccionExportacion> seccionesDisponibles) {
        this.seccionesDisponibles = seccionesDisponibles;
    }

    @PostConstruct
    void inicializarMapa() {
        seccionesPorNemonico = new LinkedHashMap<>();
        for (SeccionExportacion seccion : seccionesDisponibles) {
            String clave = normalizar(seccion.getNemonico());
            seccionesPorNemonico.put(clave, seccion);
        }
        log.debug("Secciones de exportación registradas: {}", seccionesPorNemonico.keySet());
    }

    // -------------------------------------------------------------------------
    // Método principal
    // -------------------------------------------------------------------------

    @Override
    public ExportacionDinamicaResultado obtenerAdolescentesParaExportar(
            List<String> numerosIdentificacion,
            List<String> nemonicosSecciones
    ) {
        // Columnas y cabeceras base (siempre presentes)
        List<String> headers = new ArrayList<>(List.of("nombre", "centro", "estado"));
        List<String> columnasSelect = new ArrayList<>(List.of(
                "UPPER(TRIM(CONCAT_WS(' ', TRIM(a.nombres), TRIM(a.apellido_paterno), TRIM(a.apellido_materno)))) AS nombre",
                "jer_centro.nombre AS centro",
                "cat_est.nombre AS estado"
        ));

        // JOINs base (siempre presentes)
        StringBuilder joins = new StringBuilder()
                .append(" LEFT JOIN seg_jerarquia jer_centro ON jer_centro.id_jerarquia = a.id_centro ")
                .append(" LEFT JOIN par_catalogo cat_est ON cat_est.id_catalogo = a.id_estado ");

        // WHERE dinámico por secciones, manteniendo orden de entrada
        List<String> whereCondiciones = new ArrayList<>();

        // Agregar secciones dinámicas en el orden en que llegaron
        List<String> seccionesActivas = normalizarLista(nemonicosSecciones);
        for (String nemonico : seccionesActivas) {
            SeccionExportacion seccion = seccionesPorNemonico.get(nemonico);
            if (seccion == null) {
                log.warn("Sección de exportación desconocida ignorada: '{}'", nemonico);
                continue;
            }

            if (seccion instanceof SeccionExportacionDinamica seccionDinamica) {
                EstructuraPivotDinamico estructuraPivot = construirEstructuraPivot(seccionDinamica);

                headers.addAll(estructuraPivot.headers);
                columnasSelect.addAll(estructuraPivot.columnasSelect);
                joins.append(estructuraPivot.joinSql).append(" ");

                agregarCondiciones(whereCondiciones, seccionDinamica.getWhereCondicionesDinamicas(seccionDinamica.getAliasPivot()));
                continue;
            }

            headers.addAll(seccion.getHeaders());
            columnasSelect.addAll(seccion.getColumnasSelect());
            if (seccion.getJoinSql() != null) {
                joins.append(seccion.getJoinSql()).append(" ");
            }
            agregarCondiciones(whereCondiciones, seccion.getWhereCondiciones());
        }

        // Construcción de la consulta
        StringBuilder sql = new StringBuilder()
                .append("SELECT ").append(String.join(", ", columnasSelect))
                .append(" FROM ia_ficha_identificacion a ")
                .append(joins)
                .append("WHERE a.numero_identificacion IN (");

        for (int i = 0; i < numerosIdentificacion.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(":ni").append(i);
        }
        sql.append(")");
        for (String condicion : whereCondiciones) {
            sql.append(" AND (").append(condicion).append(")");
        }
        sql.append(" ORDER BY a.id_ficha_identificacion DESC");

        // Ejecución
        Query query = entityManager.createNativeQuery(sql.toString());
        aplicarParametrosIn(query, numerosIdentificacion);

        return new ExportacionDinamicaResultado(headers, mapearFilas(query.getResultList()));
    }

    // -------------------------------------------------------------------------
    // Métodos auxiliares
    // -------------------------------------------------------------------------

    /** Asigna los parámetros del IN (:ni0, :ni1, …) a la query. */
    private void aplicarParametrosIn(Query query, List<String> valores) {
        for (int i = 0; i < valores.size(); i++) {
            query.setParameter("ni" + i, valores.get(i));
        }
    }

    private void agregarCondiciones(List<String> whereCondiciones, List<String> condiciones) {
        if (condiciones == null) {
            return;
        }
        for (String condicion : condiciones) {
            if (condicion != null && !condicion.isBlank()) {
                whereCondiciones.add(condicion);
            }
        }
    }

    private EstructuraPivotDinamico construirEstructuraPivot(SeccionExportacionDinamica seccionDinamica) {
        List<String> preguntas = obtenerPreguntasDinamicas(seccionDinamica);
        List<ColumnaDinamica> columnasDinamicas = construirColumnasDinamicas(preguntas);

        List<String> headers = new ArrayList<>(seccionDinamica.getHeadersBaseDinamicas());
        List<String> columnasSelect = new ArrayList<>(seccionDinamica.getColumnasBaseDinamicas(seccionDinamica.getAliasPivot()));

        for (ColumnaDinamica columnaDinamica : columnasDinamicas) {
            headers.add(columnaDinamica.header);
            columnasSelect.add(seccionDinamica.getAliasPivot() + "." + columnaDinamica.alias + " as " + columnaDinamica.alias);
        }

        String joinSql = construirJoinPivot(seccionDinamica, columnasDinamicas);
        return new EstructuraPivotDinamico(headers, columnasSelect, joinSql);
    }

    @SuppressWarnings("unchecked")
    private List<String> obtenerPreguntasDinamicas(SeccionExportacionDinamica seccionDinamica) {
        String sql = """
                select distinct src.%s
                from (
                    %s
                ) src
                where src.%s is not null and btrim(src.%s) <> ''
                order by src.%s
                """.formatted(
                SeccionExportacionDinamica.COLUMNA_PREGUNTA,
                seccionDinamica.getSqlFuentePivot(),
                SeccionExportacionDinamica.COLUMNA_PREGUNTA,
                SeccionExportacionDinamica.COLUMNA_PREGUNTA,
                SeccionExportacionDinamica.COLUMNA_PREGUNTA
        );

        Query query = entityManager.createNativeQuery(sql);
        List<Object> resultados = query.getResultList();

        List<String> preguntas = new ArrayList<>(resultados.size());
        for (Object valor : resultados) {
            if (valor != null) {
                preguntas.add(valor.toString());
            }
        }
        return preguntas;
    }

    private List<ColumnaDinamica> construirColumnasDinamicas(List<String> preguntas) {
        List<ColumnaDinamica> columnas = new ArrayList<>(preguntas.size());
        Map<String, Integer> aliasOcurrencias = new HashMap<>();

        for (String pregunta : preguntas) {
            String baseAlias = sanitizarAliasColumna(pregunta);
            int ocurrencias = aliasOcurrencias.getOrDefault(baseAlias, 0);
            String alias = ocurrencias == 0 ? baseAlias : baseAlias + "_" + ocurrencias;
            aliasOcurrencias.put(baseAlias, ocurrencias + 1);
            columnas.add(new ColumnaDinamica(pregunta, alias));
        }

        return columnas;
    }

    private String construirJoinPivot(SeccionExportacionDinamica seccionDinamica, List<ColumnaDinamica> columnasDinamicas) {
        List<String> columnasSubconsulta = new ArrayList<>();
        columnasSubconsulta.add("src." + SeccionExportacionDinamica.COLUMNA_ID_GRUPO + " as " + SeccionExportacionDinamica.COLUMNA_ID_GRUPO);
        columnasSubconsulta.add("src." + SeccionExportacionDinamica.COLUMNA_NOMBRE_GRUPO + " as " + SeccionExportacionDinamica.COLUMNA_NOMBRE_GRUPO);

        for (ColumnaDinamica columnaDinamica : columnasDinamicas) {
            String preguntaEscapada = columnaDinamica.header.replace("'", "''");
            columnasSubconsulta.add(
                    "MAX(CASE WHEN src." + SeccionExportacionDinamica.COLUMNA_PREGUNTA + " = '" + preguntaEscapada +
                            "' THEN src." + SeccionExportacionDinamica.COLUMNA_RESPUESTA + " END) as " + columnaDinamica.alias
            );
        }

        return """
                left join lateral (
                    select %s
                    from (
                        %s
                    ) src
                    where src.%s = a.id_ficha_identificacion
                    group by src.%s, src.%s
                ) %s on true
                """.formatted(
                String.join(", ", columnasSubconsulta),
                seccionDinamica.getSqlFuentePivot(),
                SeccionExportacionDinamica.COLUMNA_ID_FICHA,
                SeccionExportacionDinamica.COLUMNA_ID_GRUPO,
                SeccionExportacionDinamica.COLUMNA_NOMBRE_GRUPO,
                seccionDinamica.getAliasPivot()
        );
    }

    private String sanitizarAliasColumna(String texto) {
        String alias = FuncionesAyuda.crearNemonico("", texto).toLowerCase(Locale.ROOT);
        if (alias.isBlank()) {
            alias = "pregunta";
        }
        if (Character.isDigit(alias.charAt(0))) {
            alias = "pregunta_" + alias;
        }
        return alias;
    }

    /** Convierte el resultado raw de JPA en una lista de filas tipadas. */
    private List<List<Object>> mapearFilas(List<?> resultados) {
        if (resultados == null || resultados.isEmpty()) {
            return List.of();
        }

        List<List<Object>> filas = new ArrayList<>(resultados.size());
        for (Object resultado : resultados) {
            if (resultado instanceof Object[] filaArray) {
                List<Object> fila = new ArrayList<>(filaArray.length);
                for (Object valor : filaArray) {
                    fila.add(valor);
                }
                filas.add(fila);
            } else {
                List<Object> fila = new ArrayList<>(1);
                fila.add(resultado);
                filas.add(fila);
            }
        }
        return filas;
    }

    /**
     * Normaliza una lista de nemónicos: elimina nulos, aplica trim y
     * convierte a mayúsculas, manteniendo el orden de entrada y
     * descartando duplicados con LinkedHashMap.
     */
    private List<String> normalizarLista(List<String> nemonicosSecciones) {
        if (nemonicosSecciones == null) return List.of();
        List<String> resultado = new ArrayList<>();
        for (String nemonico : nemonicosSecciones) {
            String normalizado = normalizar(nemonico);
            if (!normalizado.isEmpty() && !resultado.contains(normalizado)) {
                resultado.add(normalizado);
            }
        }
        return resultado;
    }

    /** Normaliza un único nemónico: trim + mayúsculas. Retorna "" si es nulo. */
    private String normalizar(String nemonico) {
        if (nemonico == null) return "";
        return nemonico.trim().toUpperCase(Locale.ROOT);
    }

    private static final class ColumnaDinamica {
        private final String header;
        private final String alias;

        private ColumnaDinamica(String header, String alias) {
            this.header = header;
            this.alias = alias;
        }
    }

    private static final class EstructuraPivotDinamico {
        private final List<String> headers;
        private final List<String> columnasSelect;
        private final String joinSql;

        private EstructuraPivotDinamico(List<String> headers, List<String> columnasSelect, String joinSql) {
            this.headers = headers;
            this.columnasSelect = columnasSelect;
            this.joinSql = joinSql;
        }
    }
}

