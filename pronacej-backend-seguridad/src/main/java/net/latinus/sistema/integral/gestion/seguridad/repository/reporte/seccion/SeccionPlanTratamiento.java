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
public class SeccionPlanTratamiento implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_PLAN_TRATAMIENTO;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "instrumentos_tecnicas_utilizadas",
                "factores_riesgo_no_criminogenos",
                "valoracion_riesgo",
                "hipotesis_explicativa",
                "intensidad_intervencion_tratamiento",
                "dimension",
                "factor_riesgo",
                "factor_protector",
                "objetivo",
                "actividad_programa",
                "equipo_responsable",
                "tiempo_estimado"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "ipti.inst_tecnicas as instrumentos_tecnicas_utilizadas",
                "ipti.fact_riesgo_no_crimin as factores_riesgo_no_criminogenos",
                "ipti.val_riesgo as valoracion_riesgo",
                "ipti.hipot_explicativa as hipotesis_explicativa",
                "ipti.intensidad_interv_trat as intensidad_intervencion_tratamiento",
                "ipti_cat_dimension.nombre as dimension",
                "iptie.factor_riesgo as factor_riesgo",
                "iptie.factor_protector as factor_protector",
                "plan_interv.objetivo as objetivo",
                "plan_interv.actividad_programa as actividad_programa",
                "plan_interv.equipo_responsable as equipo_responsable",
                "plan_interv.tiempo_estimado as tiempo_estimado"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_plan_tratamiento_ind ipti on ipti.id_ficha_identificacion = a.id_ficha_identificacion
                 left join ia_plan_tratamiento_ind_especif iptie on iptie.id_plan_tratamiento_espec_factores = ipti.id_plan_tratamiento\s
                 left join par_catalogo ipti_cat_dimension on ipti_cat_dimension.id_catalogo = iptie.id_catalogo_dimension\s
                 LEFT JOIN LATERAL (
                     select          
                       'OBJETIVOS' as tipo,
                       ipt.objetivo,
                       ipt.actividad_programa,
                       ipt.equipo_responsable,
                       ipt.tiempo_estimado
                     from ia_plan_tratamiento_ind_interv ipt           
                     where ipt.id_plan_trat_ind_interv = ipti.id_plan_tratamiento
                     union all
                     select          
                       'NO CRIMINOGENO' as tipo,
                       ipt.objetivo,
                       ipt.actividad_programa,
                       ipt.equipo_responsable,
                       ipt.tiempo_estimado
                     from ia_plan_tratamiento_ind_interv ipt               
                     where ipt.id_plan_tratamiento_ind_no_criminogeno = ipti.id_plan_tratamiento
                     union all
                     select          
                       'DIFERENCIADA' as tipo,
                       ipt.objetivo,
                       ipt.actividad_programa,
                       ipt.equipo_responsable,
                       ipt.tiempo_estimado
                     from ia_plan_tratamiento_ind_interv ipt               
                     where ipt.id_plan_tratamiento_ind_diferenciada = ipti.id_plan_tratamiento
                 ) plan_interv ON true
                """;
    }
}

