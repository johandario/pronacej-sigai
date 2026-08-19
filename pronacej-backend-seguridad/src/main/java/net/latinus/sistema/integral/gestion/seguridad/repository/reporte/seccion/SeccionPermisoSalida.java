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
public class SeccionPermisoSalida implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_PERMISO_SALIDA;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "fecha_hora_inicio_actividad",
                "fecha_hora_fin_actividad",
                "usuario_registra_salida",
                "centro_salida",
                "motivo_salida",
                "lugar_salida",
                "frecuencia_salida",
                "observaciones"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "to_char(ips.fecha_hora_salida, 'DD/MM/yyyy HH24:MI:SS') as fecha_hora_inicio_actividad",
                "to_char(ips.fecha_hora_regreso, 'DD/MM/yyyy HH24:MI:SS') as fecha_hora_fin_actividad",
                "ips.usuario_salida as usuario_registra_salida",
                "ips_sj.nombre as centro_salida",
                "ips_cat_tipo_salida.nombre as motivo_salida",
                "ips.tipo_salida_lugar as lugar_salida",
                "ips_cat_frec.nombre as frecuencia_salida",
                "ips.observaciones as observaciones"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join inf_permiso_salida ips on ips.id_ficha_identificacion = a.id_ficha_identificacion
                left join seg_jerarquia ips_sj on ips_sj.id_jerarquia = ips.id_centro 
                left join par_catalogo ips_cat_tipo_salida on ips_cat_tipo_salida.id_catalogo = ips.id_catalogo_tipo_salida 
                left join par_catalogo ips_cat_frec on ips_cat_frec.id_catalogo = ips.id_catalogo_frencuencia_salida  
                """;
    }
}

