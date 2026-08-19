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
public class SeccionFichaPsicosocialSituacionEconomica implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_FICHA_PSICOSOCIAL_SITUACION_ECONOMICA;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "fecha_creacion",
                "zona_vivienda",
                "subzona_vivienda",
                "material_pared_vivienda",
                "material_piso_vivienda",
                "material_techo_vivienda",
                "tipo_abastecimiento_agua",
                "tipo_vivienda",
                "tipo_alumbrado",
                "combustible_cocinar",
                "tipo_desague",
                "numero_ambientes",
                "tenencia_vivienda",
                "otros_servicios",
                "numero_ocupantes",
                "numero_habitaciones",
                "numero_dormitorios",
                "grupo_amical",
                "factor_riesgo_medio",
                "area_academico_laboral",
                "area_social_recreacional",
                "area_familiar_pareja",
                "area_personal"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "to_char(ies.fecha_creacion, 'DD/MM/yyyy HH24:MM') as fecha_creacion",
                "cat_zona_viv.nombre as zona_vivienda",
                "cat_subzona_viv.nombre as subzona_vivienda",
                "cat_mat_pared_viv.nombre as material_pared_vivienda",
                "cat_mat_piso_viv.nombre as material_piso_vivienda",
                "cat_mat_techo_viv.nombre as material_techo_vivienda",
                "cat_tipo_abast_agua.nombre as tipo_abastecimiento_agua",
                "cat_tipo_viv.nombre as tipo_vivienda",
                "cat_tipo_alumb.nombre as tipo_alumbrado",
                "cat_comb_cocin.nombre as combustible_cocinar",
                "cat_tipo_desag.nombre as tipo_desague",
                "ies.numero_ambientes as numero_ambientes",
                "cat_tenen_viv.nombre as tenencia_vivienda",
                "cat_otros_serv.nombre as otros_servicios",
                "ies.numero_ocupantes as numero_ocupantes",
                "ies.numero_habitaciones as numero_habitaciones",
                "ies.numero_dormitorios as numero_dormitorios",
                "ies.grupo_amical as grupo_amical",
                "ies.factor_riesgo_medio as factor_riesgo_medio",
                "ies.area_academico_laboral as area_academico_laboral",
                "ies.area_social_recreacional as area_social_recreacional",
                "ies.area_familiar_pareja as area_familiar_pareja",
                "ies.area_personal as area_personal"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_evaluacion_social ies on ies.id_ficha_identificacion = a.id_ficha_identificacion and ies.removido = false
                left join par_catalogo cat_zona_viv on cat_zona_viv.id_catalogo = ies.zona_vivienda
                left join par_catalogo cat_subzona_viv on cat_subzona_viv.id_catalogo = ies.sub_zona
                left join par_catalogo cat_mat_pared_viv on cat_mat_pared_viv.id_catalogo = ies.material_pared_vivienda
                left join par_catalogo cat_mat_piso_viv on cat_mat_piso_viv.id_catalogo = ies.material_piso_vivienda 
                left join par_catalogo cat_mat_techo_viv on cat_mat_techo_viv.id_catalogo = ies.material_techo_vivienda
                left join par_catalogo cat_tipo_abast_agua on cat_tipo_abast_agua.id_catalogo = ies.abastecimiento_agua_vivienda 
                left join par_catalogo cat_tipo_viv on cat_tipo_viv.id_catalogo = ies.tipo_vivienda 
                left join par_catalogo cat_tipo_alumb on cat_tipo_alumb.id_catalogo = ies.tipo_alumbrado_vivienda
                left join par_catalogo cat_comb_cocin on cat_comb_cocin.id_catalogo = ies.combustible_cocinar_vivienda
                left join par_catalogo cat_tipo_desag on cat_tipo_desag.id_catalogo = ies.tipo_desague_vivienda
                left join par_catalogo cat_tenen_viv on cat_tenen_viv.id_catalogo = ies.tenencia
                left join par_catalogo cat_otros_serv on cat_otros_serv.id_catalogo = ies.otros_servicios
                """;
    }
}

