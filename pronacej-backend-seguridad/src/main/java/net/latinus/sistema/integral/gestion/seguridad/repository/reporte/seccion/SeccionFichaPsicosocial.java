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
public class SeccionFichaPsicosocial implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_FICHA_PSICOSOCIAL;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "tipo_familia",
                "organizacion_familiar",
                "partida_nacimiento",
                "relacion_padres",
                "relacion_filial",
                "relacion_parental",
                "relacion_pareja",
                "religion",
                "ultimo_sacramento",
                "otro_sacramento",
                "ejercicio_autoridad",
                "entorno_familiar",
                "obs_relacion_intrafamiliar",
                "causa_ausencia_padres"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
        "cat_tipo_fam.nombre as tipo_familia",
        "cat_org_fam.nombre as organizacion_familiar",
        "case when dfa.partida_nacimiento is true then 'Sí' else 'No' end as partida_nacimiento",
        "case when dfa.relacion_intra_familiar_padres is true then 'Sí' else 'No' end as relacion_padres",
        "case when dfa.relacion_intra_familiar_filial is true then 'Sí' else 'No' end as relacion_filial",
        "case when dfa.relacion_intra_familiar_parentales is true then 'Sí' else 'No' end as relacion_parental",
        "case when dfa.relacion_intra_familiar_pareja is true then 'Sí' else 'No' end as relacion_pareja",
        "cat_religion.nombre as religion",
        "cat_sacramento.nombre as ultimo_sacramento",
        "dfa.otro_sacramento as otro_sacramento",
        "dfa.ejercicio_autoridad as ejercicio_autoridad",
        "dfa.entorno_familiar as entorno_familiar",
        "dfa.observaciones_relacion_intrafamiliar as obs_relacion_intrafamiliar",
        "dfa.causa_ausencia_padres as causa_ausencia_padres"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join par_datos_familiares dfa on dfa.id_ficha_identificacion = a.id_ficha_identificacion and dfa.removido = false
                left join par_catalogo cat_tipo_fam on cat_tipo_fam.id_catalogo = dfa.id_tipo_familia
                left join par_catalogo cat_org_fam on cat_org_fam.id_catalogo = dfa.id_organizacion_familiar
                left join par_catalogo cat_religion on cat_religion.token_identificador = dfa.religion
                left join par_catalogo cat_sacramento on cat_sacramento.id_catalogo = dfa.id_tipo_sacramento
                """;
    }
}

