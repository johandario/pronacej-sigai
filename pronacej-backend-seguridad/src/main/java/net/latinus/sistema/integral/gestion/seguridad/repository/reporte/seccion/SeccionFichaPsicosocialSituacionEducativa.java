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
public class SeccionFichaPsicosocialSituacionEducativa implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_FICHA_PSICOSOCIAL_SITUACION_EDUCATIVA;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "actitud_frente_estudios",
                "desarrollo_educativo",
                "intereses_vocacionales_educativos",
                "as observaciones_ambito_educativo",
                "actitud_frente_empleo",
                "capacitaciones_empleabilidad",
                "observaciones_ambito_laboral",
                "pasatiempo_hobbies",
                "talentos_habilidades",
                "participacion_prosocial",
                "uso_tiempo",
                "observaciones_ocio"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "aselo.actitud_estudios as actitud_frente_estudios",
                "aselo.desarrollo_educativo as desarrollo_educativo",
                "aselo.intereses_vocacionales as intereses_vocacionales_educativos",
                "aselo.observaciones_educativas as observaciones_ambito_educativo",
                "aselo.actitud_empleo as actitud_frente_empleo",
                "aselo.capacitaciones_empleabilidad as capacitaciones_empleabilidad",
                "aselo.observaciones_laborales as observaciones_ambito_laboral",
                "aselo.pasatiempos as pasatiempo_hobbies",
                "aselo.talentos as talentos_habilidades",
                "aselo.participacion_grupal as participacion_prosocial",
                "aselo.uso_tiempo as uso_tiempo",
                "aselo.observaciones_ocio as observaciones_ocio"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_areas_situacion_educativa_laboral_ocio aselo on aselo.id_ficha_identificacion = a.id_ficha_identificacion and aselo.removido = false
                """;
    }
}

