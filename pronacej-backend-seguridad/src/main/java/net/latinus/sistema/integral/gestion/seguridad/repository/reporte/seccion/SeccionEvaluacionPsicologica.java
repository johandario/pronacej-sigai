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
public class SeccionEvaluacionPsicologica implements SeccionExportacionDinamica {
    static final String NEMONICO_EVALUACION_PSICOLOGICA = "ENCUESTA_EVALUACIÓN_PSICOLÓGICA_CJDR";

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_EVALUACIONES_PSICOLOGICAS;
    }

    @Override
    public String getAliasPivot() {
        return "eval_psic_pivot";
    }

    @Override
    public List<String> getHeadersBaseDinamicas() {
        return List.of(
                "identificador",
                "nombre"
        );
    }

    @Override
    public List<String> getColumnasBaseDinamicas(String aliasPivot) {
        return List.of(
                aliasPivot + ".id_grupo as identificador",
                aliasPivot + ".nombre_grupo as nombre"
        );
    }

    @Override
    public String getSqlFuentePivot() {
        return """
                select
                    eval_psic_encab.id_ficha_identificacion as id_ficha_identificacion,
                    eval_psic_encab.id_encabezado as id_grupo,
                    eval_psic_encab.nombre as nombre_grupo,
                    eval_ep.texto as pregunta,
                    eval_ec.contestacion as respuesta
                from enc_encabezado eval_psic_encab
                inner join enc_encuesta eva_psic_encuest on eva_psic_encuest.id_encuesta = eval_psic_encab.id_encuesta
                left join enc_contestacion eval_ec on eval_ec.id_encabezado = eval_psic_encab.id_encabezado
                left join enc_pregunta eval_ep on eval_ep.id_pregunta = eval_ec.id_pregunta
                left join par_catalogo eval_psic_cat_tipo on eval_psic_cat_tipo.id_catalogo = eva_psic_encuest.id_catalogo
                where eval_psic_encab.removido = false
                  and eva_psic_encuest.removido = false
                  and eval_psic_cat_tipo.nemonico = '%s'
                """.formatted(NEMONICO_EVALUACION_PSICOLOGICA);
    }


}

