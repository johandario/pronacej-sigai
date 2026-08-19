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
public class SeccionFlujos implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_FLUJOS;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "fecha_creacion",
                "numero_documento",
                "motivo_salida",
                "observaciones",
                "fecha_hora_salida"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "to_char(reg_sal.fecha_creacion, 'DD/MM/yyyy HH24:MI') as fecha_creacion",
                "reg_sal.nro_documento as numero_documento",
                "reg_sal_mot_sal.nombre as motivo_salida",
                "reg_sal.observaciones as observaciones",
                "to_char(reg_sal.fecha_hora_salida, 'DD/MM/yyyy HH24:MI') as fecha_hora_salida"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join salida_registro reg_sal on reg_sal.id_ficha_identificacion = a.id_ficha_identificacion and reg_sal.removido = false
                left join gest_evento_fuga gef on gef.id_fuga = reg_sal.id_fuga
                --left join par_catalogo reg_sal_cat_estado_fuga on reg_sal_cat_estado_fuga.id_catalogo = gef.id_catalogo_estado_evento
                left join tras_traslado tt on tt.id_traslado = reg_sal.id_traslado
                --left join par_catalogo reg_sal_cat_estado_tras on reg_sal_cat_estado_tras.id_catalogo = gef.id_catalogo_estado_evento
                left join par_catalogo reg_sal_cat_estado on reg_sal_cat_estado.id_catalogo = gef.id_catalogo_estado_evento and reg_sal_cat_estado.nemonico = 'ESTADO_SALIDA_INACTIVO'
                left join par_catalogo reg_sal_mot_sal on reg_sal_mot_sal.id_catalogo = reg_sal.id_catalogo_motivo_salida
                """;
    }

    /*@Override
    public List<String> getWhereCondiciones() {
        return List.of(
                "(reg_sal_cat_estado_fuga.nemonico = 'ESTADO_SALIDA_INACTIVO' or reg_sal_cat_estado_tras.nemonico = 'ESTADO_SALIDA_INACTIVO')"
        );
    }*/
}

