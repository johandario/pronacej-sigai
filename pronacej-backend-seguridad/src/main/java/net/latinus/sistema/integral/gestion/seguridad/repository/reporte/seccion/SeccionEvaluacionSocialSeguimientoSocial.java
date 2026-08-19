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
public class SeccionEvaluacionSocialSeguimientoSocial implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_EVALUACION_SOCIAL_SEGUIMIENTO_SOCIAL;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "programa",
                "ambiente",
                "tipo_actividad_social",
                "fecha_actividad",
                "descripcion_social",
                "acciones_adoptadas",
                "comentarios"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "ss_jer_programa.nombre as programa",
                "ss_jer_ambiente.nombre as ambiente",
                "ss_cat_tipo_actividad.nombre as tipo_actividad_social",
                "to_char(iss.fecha, 'DD/MM/yyyy') as fecha_actividad",
                "iss.descripcion_social as descripcion_social",
                "iss.acciones_adoptadas as acciones_adoptadas",
                "iss.comentarios as comentarios"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_seguimiento_social iss on iss.id_ficha_identificacion = a.id_ficha_identificacion
                left join seg_jerarquia  ss_jer_programa on ss_jer_programa.id_jerarquia = iss.id_programa
                left join par_catalogo ss_jer_ambiente on ss_jer_ambiente.id_catalogo = iss.id_ambiente
                left join par_catalogo ss_cat_tipo_actividad on ss_cat_tipo_actividad.id_catalogo = iss.tipo_actividad_social
                """;
    }
}

