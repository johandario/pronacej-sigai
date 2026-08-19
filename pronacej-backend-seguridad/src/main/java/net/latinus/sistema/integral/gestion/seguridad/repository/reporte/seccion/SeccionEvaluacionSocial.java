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
public class SeccionEvaluacionSocial implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_EVALUACION_SOCIAL_DOMICILIARIA;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "Fecha de registro",
                "Fecha de entrevista",
                "Duración de la entrevista (hh:mm)",
                "Persona entrevistada",
                "¿Se realizó visita domiciliaria?",
                "Objetivo general",
                "Desarrollo de la visita domiciliaria",
                "Dinámica familiar disfuncional",
                "Características del entorno social y comunitario",
                "Factores protectores",
                "Conclusiones",
                "Recomendaciones"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "to_char(ied.fecha_registro, 'DD/MM/yyyy') as fecha_registro",
                "to_char(ied.fecha_entrevista , 'DD/MM/yyyy') as fecha_entrevista",
                "LPAD(FLOOR(ied.duracion_vista)::text, 2, '0') || ':' || LPAD(ROUND((ied.duracion_vista - FLOOR(ied.duracion_vista)) * 60)::text, 2, '0') as duracion_entrevista",
                "TRIM(CONCAT_WS(' ', TRIM(spr.nombres), TRIM(spr.primer_apellido), TRIM(spr.segundo_apellido))) as persona_entrevistada",
                "case when ied.visita_realizada is true then 'Sí' else 'No' end as realizo_visita",
                "ied.objetivo_general as objetivo_general",
                "ied.desarrollo_visita_domiciliaria as desarrollo_visita_domiciliaria",
                "ied.dinamica_familiar_disfuncional as dinamica_familiar_disfuncional",
                "ied.caracteristicas_entorno_socialmc as caracteristicas_entorno_social_mc",
                "ied.factores_protectores as factores_protectores",
                "ied.conclusiones as conclusiones",
                "ied.recomendaciones as recomendaciones"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_evaluacion_domiciliaria ied on ied.id_ficha_identificacion = a.id_ficha_identificacion and ied.removido = false
                left join seg_personas_relacionadas spr on spr.id_personas_relacionadas = ied.id_persona_relacionada
                """;
    }
}

