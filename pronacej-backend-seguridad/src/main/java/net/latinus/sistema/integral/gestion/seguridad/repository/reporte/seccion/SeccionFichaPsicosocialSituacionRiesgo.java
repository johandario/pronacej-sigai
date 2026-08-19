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
public class SeccionFichaPsicosocialSituacionRiesgo implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_FICHA_PSICOSOCIAL_SITUACION_RIESGO_SOCIAL;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "antec_delictivos_familiares",
                "primeras_menifestaciones_infractoras",
                "evasion_hogar",
                "estado_salud_general",
                "problemas_legales",
                "observaciones"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "srs.ante_deli_fami as antec_delictivos_familiares",
                "srs.prim_mani_infr_adol as primeras_menifestaciones_infractoras",
                "case when srs.evasion_hogar is true then 'Sí' else 'No' end as evasion_hogar",
                "srs.estado_salud_general as estado_salud_general",
                "srs.problemas_legales as problemas_legales",
                "srs.observaciones as observaciones"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_situacion_riesgo_social srs on srs.id_ficha_identificacion = a.id_ficha_identificacion and srs.removido = false
                """;
    }
}

