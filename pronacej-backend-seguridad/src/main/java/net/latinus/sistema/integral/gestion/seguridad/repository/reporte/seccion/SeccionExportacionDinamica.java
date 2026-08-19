package net.latinus.sistema.integral.gestion.seguridad.repository.reporte.seccion;

import java.util.List;

/**
 * Contrato para secciones cuya estructura de columnas depende de datos en runtime.
 *
 * <p>La consulta fuente debe exponer, con esos alias exactos:
 * <ul>
 *   <li>id_ficha_identificacion</li>
 *   <li>id_grupo</li>
 *   <li>nombre_grupo</li>
 *   <li>pregunta</li>
 *   <li>respuesta</li>
 * </ul>
 * </p>
 */
public interface SeccionExportacionDinamica extends SeccionExportacion {

    String COLUMNA_ID_FICHA = "id_ficha_identificacion";
    String COLUMNA_ID_GRUPO = "id_grupo";
    String COLUMNA_NOMBRE_GRUPO = "nombre_grupo";
    String COLUMNA_PREGUNTA = "pregunta";
    String COLUMNA_RESPUESTA = "respuesta";

    /** Alias de la subconsulta pivot en el SELECT principal. */
    String getAliasPivot();

    /**
     * SQL fuente del pivot. Debe retornar las columnas estándar de este contrato.
     */
    String getSqlFuentePivot();

    /** Cabeceras fijas previas a las columnas dinámicas (ej.: identificador, nombre). */
    List<String> getHeadersBaseDinamicas();

    /**
     * Columnas fijas del SELECT principal referenciando el alias pivot entregado.
     */
    List<String> getColumnasBaseDinamicas(String aliasPivot);

    /**
     * @deprecated Este método ya no se aplica al WHERE externo de la consulta principal.
     *             El {@code LEFT JOIN LATERAL ... ON TRUE} preserva todos los registros
     *             aunque no existan datos en la sección pivot; trasladar condiciones al
     *             WHERE externo convertía el LEFT JOIN en un INNER JOIN implícito,
     *             causando pérdida de adolescentes sin datos en la sección.
     *             Las implementaciones existentes pueden eliminarse de forma segura.
     */
    @Deprecated(since = "2026-05-28", forRemoval = true)
    default List<String> getWhereCondicionesDinamicas(String aliasPivot) {
        return List.of();
    }

    @Override
    default List<String> getHeaders() {
        return List.of();
    }

    @Override
    default List<String> getColumnasSelect() {
        return List.of();
    }

    @Override
    default String getJoinSql() {
        return null;
    }
}

