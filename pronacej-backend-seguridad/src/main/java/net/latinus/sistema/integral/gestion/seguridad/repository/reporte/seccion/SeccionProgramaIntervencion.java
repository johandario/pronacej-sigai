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
public class SeccionProgramaIntervencion implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_PROGRAMA_INTERVENCION_INTENSIVA;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "motivo",
                "criterios_seleccion",
                "analisis_psicologico",
                "analisis_social",
                "analisis_conductual",
                "analisis_familiar",
                "propuesta_actividad_formativa",
                "importancia_participacion_adolescente",
                "objetivos_conseguir",
                "duracion",
                "conclusiones",
                "recomendaciones"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "iits.motivo as motivo",
                "iits.criterios_seleccion as criterios_seleccion",
                "iits.analisis_psicologico as analisis_psicologico",
                "iits.analisis_social as analisis_social",
                "iits.analisis_conductual as analisis_conductual",
                "iits.analisis_familiar as analisis_familiar",
                "iits.propuesta_actividad_formativa as propuesta_actividad_formativa",
                "iits.importancia_participacion_adolescente as importancia_participacion_adolescente",
                "iits.objetivos_conseguir as objetivos_conseguir",
                "LPAD(FLOOR(iits.duracion)::text, 2, '0') || ':' || LPAD(ROUND((iits.duracion - FLOOR(iits.duracion)) * 60)::text, 2, '0') as duracion",
                "iits.conclusiones as conclusiones",
                "iits.recomendaciones as recomendaciones"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_informe_tecnico_sustentatorio iits on iits.id_ficha_identificacion = a.id_ficha_identificacion
                """;
    }
}

