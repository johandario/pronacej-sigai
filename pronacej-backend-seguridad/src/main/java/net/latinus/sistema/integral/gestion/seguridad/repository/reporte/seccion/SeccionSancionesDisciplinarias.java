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
public class SeccionSancionesDisciplinarias implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_SANCIONES_DISCIPLINARIAS;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "fecha_registro",
                "num_resolucion_administrativa",
                "fecha_inicio",
                "fecha_fin_sancion",
                "tipificacion_falta",
                "programa",
                "ambiente",
                "motivo_sancion",
                "falta_cometida",
                "sancion_impuesta",
                "observaciones"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "to_char(isd.fecha_registro, 'DD/MM/yyyy HH24:MI:SS') as fecha_registro",
                "isd.nro_resolucion as num_resolucion_administrativa",
                "to_char(isd.fecha_inicio, 'DD/MM/yyyy') as fecha_inicio",
                "to_char(isd.fecha_fin, 'DD/MM/yyyy') as fecha_fin_sancion",
                "isd_cat_tipif.nombre as tipificacion_falta",
                "isd_sj_programa.nombre as programa",
                "isd_sj_ambiente.nombre as ambiente",
                "isd.motivo as motivo_sancion",
                "isd.falta as falta_cometida",
                "isd.sancion as sancion_impuesta",
                "isd.observacion as observaciones"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_sancion_disciplinaria isd on isd.id_ficha_identificacion = a.id_ficha_identificacion
                left join seg_jerarquia isd_sj_programa on isd_sj_programa.id_jerarquia = isd.id_programa
                left join seg_jerarquia isd_sj_ambiente on isd_sj_ambiente.id_jerarquia = isd.id_ambiente
                left join par_catalogo isd_cat_tipif on isd_cat_tipif.id_catalogo = isd.tipificacion_falta
                """;
    }
}

