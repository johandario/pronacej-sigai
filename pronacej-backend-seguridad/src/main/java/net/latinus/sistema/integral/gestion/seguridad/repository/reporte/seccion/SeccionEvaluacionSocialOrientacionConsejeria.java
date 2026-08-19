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
public class SeccionEvaluacionSocialOrientacionConsejeria implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_EVALUACION_SOCIAL_ORIENTACION_CONSEJERIA_FAMILIAR;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "Persona relacionada",
                "Tipo de documento",
                "Número de documento",
                "Parentesco",
                "Fecha de orientación/consejería",
                "Descripción de la orientación/consejería",
                "Usuario que registró la orientación/consejería"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "TRIM(CONCAT_WS(' ', TRIM(spr1.nombres), TRIM(spr1.primer_apellido), TRIM(spr1.segundo_apellido))) as persona_relacionada",
                "oc_cat_tipo_doc.nombre as tipo_documento",
                "spr1.identificacion as numero_documento",
                "oc_cat_parentesco.nombre as parentesco",
                "to_char(iocf.fecha, 'DD/MM/yyyy HH24:MM') as fecha_orientacion",
                "iocf.descripcion as descripcion_orientacion",
                "TRIM(CONCAT_WS(' ', TRIM(oc_sus.nombres), TRIM(oc_sus.apellidos))) as usuario_registro_orientacion"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_evaluacion_social oc_eval_soc on oc_eval_soc.id_ficha_identificacion = a.id_ficha_identificacion
                left join seg_personas_relacionadas spr1 on spr1.id_evaluacion_social = oc_eval_soc.id_evaluacion_social
                left join ia_orientacion_consejeria_familiar iocf on iocf.id_persona_relacionada = spr1.id_personas_relacionadas
                left join par_catalogo oc_cat_tipo_doc on oc_cat_tipo_doc.id_catalogo = spr1.id_tipo_documento
                left join par_catalogo oc_cat_parentesco on oc_cat_parentesco.id_catalogo = spr1.id_parentesco
                left join seg_usuario_sistema oc_sus on oc_sus.id_usuario_sistema = iocf.id_usuario_crea
                """;
    }
}

