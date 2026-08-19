package net.latinus.sistema.integral.gestion.seguridad.repository.reporte.seccion;

import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sección «Ficha Psicosocial» de la exportación dinámica de adolescentes.
 * <p>
 * Aporta información consolidada de:
 * - Composición familiar (par_datos_familiares)
 * - Situación educativa/laboral/ocio (ia_situacion_educativa_laboral_ocio)
 * - Áreas de situación educativa/laboral (ia_areas_situacion_educativa_laboral_ocio)
 * - Situación de riesgo social (ia_situacion_riesgo_social)
 * </p>
 */
@Component
public class SeccionPostEgreso implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_POST_EGRESO;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "fecha_registro",
                "usuario_responsable",
                "modalidad_entrevista",
                "observaciones",
                "actividades"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "to_char(ica.fecha_registro, 'DD/MM/yyyy HH24:MI') as fecha_registro",
                "ica.usuario_responsable as usuario_responsable",
                "ica.modalidad_entrevista as modalidad_entrevista",
                "ica.observaciones as observaciones",
                "ica.actividades as actividades"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join inf_contacto_adolescente ica on ica.id_ficha_identificacion = a.id_ficha_identificacion
                """;
    }
}

