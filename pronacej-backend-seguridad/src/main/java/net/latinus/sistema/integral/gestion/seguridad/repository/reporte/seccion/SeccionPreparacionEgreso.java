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
public class SeccionPreparacionEgreso implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_PREPARACION_EGRESO;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "identificador",
                "fecha_sesion",
                "tipo",
                "nombre_responsable",
                "observaciones"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "srefz.id_reforzamiento as identificador",
                "to_char(srefz_ssr.fecha_sesion, 'DD/MM/yyyy') as fecha_sesion",
                "srefz_cat_tipo.nombre as tipo",
                "srefz_ssr.nombre_responsable as nombre_responsable",
                "srefz_ssr.observaciones as observaciones"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join sal_reforzamiento srefz on srefz.id_ficha_identificacion = a.id_ficha_identificacion and srefz.removido = false
                left join sal_sesion_reforzamiento srefz_ssr on srefz_ssr.id_reforzamiento = srefz.id_reforzamiento and srefz_ssr.removido = false
                left join par_catalogo srefz_cat_tipo on srefz_cat_tipo.id_catalogo = srefz_ssr.tipo_sesion
                """;
    }
}

