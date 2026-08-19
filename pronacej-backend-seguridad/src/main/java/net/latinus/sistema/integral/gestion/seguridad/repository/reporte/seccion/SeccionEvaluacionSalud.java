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
public class SeccionEvaluacionSalud implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_HISTORIA_CLINICA;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "estado_salud",
                "tipo_sangre",
                "tiene_alergia_medicamentos",
                "detalle_alergia_medicamentos",
                "tiene_alergia_alimentos",
                "detalle_alergia_alimentos",
                "tiene_cirugias",
                "detalle_cirugias",
                "tiene_fracturas",
                "detalle_fracturas",
                "inicio_relaciones_sexuales",
                "inicio_consumo_drogas",
                "relacion_genero",
                "uso_preservativo",
                "droga_inicio",
                "habitos_nocivos",
                "consumo_alcohol",
                "edad_alcohol",
                "consumo_tabaco",
                "edad_tabaco",
                "aspecto_general_fisico",
                "piel_faneras",
                "regional_cabeza",
                "regional_ojos",
                "regional_oidos",
                "regional_nariz",
                "regional_boca",
                "regional_orofaringe",
                "regional_corazon",
                "regional_pulmones",
                "regional_abdomen",
                "regional_urinario",
                "regional_ppl",
                "regional_pru",
                "regional_impresion_diagnostico"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "afm.estado_salud as estado_salud",
                "eval_cat_tipo_sangre.nombre as tipo_sangre",
                "case when afm.alergia_medicamentos is true then 'Sí' else 'No' end as tiene_alergia_medicamentos",
                "afm.medicamentos_alergicos as detalle_alergia_medicamentos",
                "case when afm.alergia_alimentos is true then 'Sí' else 'No' end as tiene_alergia_alimentos",
                "afm.detalle_alergias_alimentos as detalle_alergia_alimentos",
                "case when afm.cirugia_quirurgica  is true then 'Sí' else 'No' end as tiene_cirugias",
                "afm.detalle_cirugias as detalle_cirugias",
                "case when afm.fracturas is true then 'Sí' else 'No' end as tiene_fracturas",
                "afm.detalle_fracturas as detalle_fracturas",
                "afm.irs as inicio_relaciones_sexuales",
                "afm.icd as inicio_consumo_drogas",
                "eval_cat_relacion_genero.nombre as relacion_genero",
                "case when afm.uso_de_preservativo is true then 'Sí' else 'No' end as uso_preservativo",
                "afm.droga_inicio as droga_inicio",
                "case when afm.habitos_nocivos is true then 'Sí' else 'No' end as habitos_nocivos",
                "case when afm.toma_alcohol is true then 'Sí' else 'No' end as consumo_alcohol",
                "afm.edad_alcohol as edad_alcohol",
                "case when afm.tabaco is true then 'Sí' else 'No' end as consumo_tabaco",
                "afm.edad_tabaco as edad_tabaco",
                "afm.aspecto_general_fisico as aspecto_general_fisico",
                "afm.piel_faneras as piel_faneras",
                "afm.cabeza_detalle as regional_cabeza",
                "afm.ojos_detalle as regional_ojos",
                "afm.oido_detalle as regional_oidos",
                "afm.nariz_detalle as regional_nariz",
                "afm.boca_detalle as regional_boca",
                "afm.orofaringe_detalle as regional_orofaringe",
                "afm.corazon_detalle as regional_corazon",
                "afm.pulmones_detalle as regional_pulmones",
                "afm.abdomen_detalle as regional_abdomen",
                "afm.urinario_detalle as regional_urinario",
                "afm.ppl_detalle as regional_ppl",
                "afm.pru_detalle as regional_pru",
                "afm.impresion_diagnostico as regional_impresion_diagnostico"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ai_ficha_medica afm on afm.id_ficha_identificacion = a.id_ficha_identificacion and afm.removido = false
                left join par_catalogo eval_cat_tipo_sangre on eval_cat_tipo_sangre.id_catalogo = afm.id_catalogo_tipo_sangre
                left join par_catalogo eval_cat_relacion_genero on eval_cat_relacion_genero.id_catalogo = afm.tipo_genero
                """;
    }
}

