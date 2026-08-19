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
public class SeccionEvaluacionSocialAutorizacionVisitantes implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_EVALUACION_SOCIAL_AUTORIZACION_VISITANTES;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "tipo_registro",
                "cometimiento_infraccion",
                "numero_oficio_sancion",
                "persona_relacionada",
                "tipo_autorizacion",
                "fecha_inicio",
                "fecha_fin",
                "observaciones",
                "causales_restriccion"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "aut_vis.tipo_registro as tipo_registro",
                "aut_vis.cometimiento_infraccion as cometimiento_infraccion",
                "aut_vis.numero_oficio_sancion as numero_oficio_sancion",
                "aut_vis.persona_relacionada as persona_relacionada",
                "aut_vis.tipo_autorizacion as tipo_autorizacion",
                "aut_vis.fecha_inicio as fecha_inicio",
                "aut_vis.fecha_fin as fecha_fin",
                "aut_vis.observaciones as observaciones",
                "aut_vis.causales_restriccion as causales_restriccion"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                LEFT JOIN LATERAL (
                  -- BLOQUE 1: suspensión de visitas
                  SELECT
                    'SUSPENSION VISITA' AS tipo_registro,
                    av_cat_causal.nombre AS cometimiento_infraccion,
                    isv.oficio_de_sancion AS numero_oficio_sancion,
                    NULL AS persona_relacionada,
                    NULL AS tipo_autorizacion,
                    to_char(isv.fecha_inicio, 'DD/MM/yyyy') AS fecha_inicio,
                    to_char(isv.fecha_fin, 'DD/MM/yyyy') AS fecha_fin,
                    isv.observaciones AS observaciones,
                    NULL AS causales_restriccion
                  FROM ia_suspension_visitas isv
                  LEFT JOIN ia_cometimiento_infraccion ici
                    ON ici.id_suspension_visitas = isv.id_suspension_visitas
                    AND ici.seleccionado = true
                  LEFT JOIN par_catalogo av_cat_causal
                    ON av_cat_causal.id_catalogo = ici.id_causal_suspension
                  WHERE isv.token_identificador_ficha_principal = a.token_identificador
                  UNION ALL
                  -- BLOQUE 2: informe de visitas
                  SELECT
                    'AUTORIZACION VISITA' AS tipo_registro,
                    NULL AS cometimiento_infraccion,
                    NULL AS numero_oficio_sancion,
                    UPPER(TRIM(CONCAT_WS(' ', TRIM(av_spr.nombres), TRIM(av_spr.primer_apellido), TRIM(av_spr.segundo_apellido)))) AS persona_relacionada,
                    av_cat_tipo_aut.nombre AS tipo_autorizacion,
                    to_char(iiv.fecha_inicio, 'DD/MM/yyyy') AS fecha_inicio,
                    to_char(iiv.fecha_fin, 'DD/MM/yyyy') AS fecha_fin,
                    iiv.observaciones AS observaciones,
                    iiv.causales_restriccion
                  FROM ia_informe_visitas iiv
                  LEFT JOIN seg_personas_relacionadas av_spr
                    ON av_spr.id_personas_relacionadas = iiv.id_persona_relacionada
                  LEFT JOIN par_catalogo av_cat_tipo_aut
                    ON av_cat_tipo_aut.id_catalogo = iiv.id_tipo_autorizacion
                  WHERE iiv.token_identificador_ficha_principal = a.token_identificador
                ) aut_vis ON TRUE
                """;
    }
}

