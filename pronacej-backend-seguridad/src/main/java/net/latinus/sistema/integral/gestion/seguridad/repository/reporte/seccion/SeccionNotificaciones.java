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
public class SeccionNotificaciones implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_NOTIFICACIONES;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "fecha_envio",
                "destinatario",
                "tipo",
                "asunto",
                "mensaje"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "to_char(nn.fecha_creacion, 'DD/MM/yyyy HH24:MI:SS') as fecha_envio",
                "nn.destinatario as destinatario",
                "nn_cat_tipo.nombre as tipo",
                "nn.asunto as asunto",
                "regexp_replace(nn.cuerpo, '<[^>]*>', '', 'g') as mensaje"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join not_notificacion nn on nn.id_ficha_identificacion = a.id_ficha_identificacion
                left join par_catalogo nn_cat_tipo on nn_cat_tipo.id_catalogo = nn.id_tipo
                """;
    }
}

