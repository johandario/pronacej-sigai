package net.latinus.sistema.integral.gestion.seguridad.repository.reporte.seccion;

import java.util.List;

/**
 * Contrato que representa una sección dinámica de exportación de adolescentes.
 * <p>
 * Cada sección encapsula las columnas de cabecera, las expresiones SQL del SELECT
 * y, opcionalmente, un fragmento JOIN que se añade a la consulta base.
 * </p>
 * <p>
 * Para registrar una nueva sección basta con crear una clase {@code @Component}
 * que implemente esta interfaz; el repositorio la detecta automáticamente por
 * inyección de Spring.
 * </p>
 */
public interface SeccionExportacion {

    /**
     * Nemónico que identifica a la sección.
     * Debe coincidir (en mayúsculas y sin espacios) con el valor definido
     * en {@link net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico}.
     */
    String getNemonico();

    /**
     * Etiquetas de columna que se añaden al listado de cabeceras del resultado,
     * en el mismo orden en que aparecen en {@link #getColumnasSelect()}.
     */
    List<String> getHeaders();

    /**
     * Expresiones SQL que se añaden al SELECT de la consulta base,
     * en el mismo orden en que aparecen en {@link #getHeaders()}.
     * <p>
     * La tabla principal está aliasada como {@code a} (ia_ficha_identificacion).
     * </p>
     */
    List<String> getColumnasSelect();

    /**
     * Fragmento SQL de JOIN que se añade a la consulta base, o {@code null}
     * si la sección no requiere ningún JOIN adicional.
     * <p>
     * Ejemplo: {@code "LEFT JOIN par_catalogo t ON t.id_catalogo = a.tipo_identificacion"}
     * </p>
     */
    String getJoinSql();

    /**
     * Condiciones SQL del WHERE que la sección aporta a la consulta base.
     * Cada ítem debe ser una condición completa (ej.: {@code a.removido = false}).
     */
    default List<String> getWhereCondiciones() {
        return List.of();
    }
}

