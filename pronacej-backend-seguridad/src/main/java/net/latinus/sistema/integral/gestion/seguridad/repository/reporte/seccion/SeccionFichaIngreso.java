package net.latinus.sistema.integral.gestion.seguridad.repository.reporte.seccion;

import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sección «Ficha de Ingreso» de la exportación dinámica de adolescentes.
 * <p>
 * Aporta las observaciones del último registro de salida asociado
 * a cada adolescente mediante un {@code LEFT JOIN LATERAL}.
 * </p>
 */
@Component
public class SeccionFichaIngreso implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_INGRESO;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "fecha_hora_ingreso",
                "centro_ingreso",
                "obs_ingreso",
                "seguro_salud",
                "lesiones",
                "moretones",
                "cicatrices",
                "tatuajes",
                "piercing",
                "otros",
                "forma_cabeza",
                "forma_nariz",
                "forma_labios",
                "forma_cuerpo",
                "forma_ojos"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "to_char(ifi.fecha_ingreso, 'DD/MM/yyyy HH24:MM') as fecha_hora_ingreso",
                "jer_centro_ing.nombre as centro_ingreso",
                "ifi.observaciones as obs_ingreso",
                "cat_seg_sal.nombre as seguro_salud",
                """                        
                case
                    when ifi.lesiones is true then ifi.especificar_zona_lesiones
                    else null
                end as lesiones
                """,
                """
                    case
                    when ifi.moretones is true then ifi.especificar_zona_moretones
                    else null
                end as moretones
                """,
                """
                case
                    when ifi.cicatrices is true then ifi.especificar_zona_cicatrices
                    else null
                end as cicatrices
                """,
                """
                    case
                    when ifi.tatuajes is true then ifi.especificar_zona_tatuajes
                    else null
                end as tatuajes
                """,
                """
                case
                    when ifi.piercing is true then ifi.especificar_zona_piercing
                    else null
                end as piercing
                """,
                """
                    case
                    when ifi.otros is true then ifi.especificar_zona_otros
                    else null
                end as otros
                """,
                "cat_form_cabeza.nombre as forma_cabeza",
                "cat_form_nariz.nombre as forma_nariz",
                "cat_form_labios.nombre as forma_labios",
                "cat_form_cuerpo.nombre as forma_cuerpo",
                "cat_form_ojos.nombre as forma_ojos"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_ficha_ingreso ifi on ifi.id_ficha_identificacion = a.id_ficha_identificacion and ifi.removido = false
                left join seg_jerarquia jer_centro_ing on jer_centro_ing.id_jerarquia = ifi.id_centro
                left join par_catalogo cat_seg_sal on cat_seg_sal.id_catalogo = ifi.seguro_salud
                left join par_catalogo cat_form_cabeza on cat_form_cabeza.id_catalogo = ifi.forma_cabeza
                left join par_catalogo cat_form_nariz on cat_form_nariz.id_catalogo = ifi.forma_nariz
                left join par_catalogo cat_form_labios on cat_form_labios.id_catalogo = ifi.forma_labios
                left join par_catalogo cat_form_cuerpo on cat_form_cuerpo.id_catalogo = ifi.forma_cuerpo
                left join par_catalogo cat_form_ojos on cat_form_ojos.id_catalogo = ifi.anomalia_ojos
                """;
    }
}

