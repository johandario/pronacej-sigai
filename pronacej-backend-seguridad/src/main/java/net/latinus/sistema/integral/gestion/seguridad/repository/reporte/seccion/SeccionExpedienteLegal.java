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
public class SeccionExpedienteLegal implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_EXPEDIENTE_MATRIZ;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "fecha_creacion",
                "numero_registro",
                "expediente_judicial",
                "numero_oficio",
                "fecha_oficio",
                "observaciones",
                "fecha_resolucion",
                "numero_resolucion",
                "tipo_registro",
                "situacion",
                "situacion",
                "fecha_inicio_medida",
                "fecha_fin_medida",
                "corte_justicia",
                "instancia",
                "especialidad",
                "organo",
                "monto_reparacion_civil"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "to_char(iem.fecha_creacion, 'DD/MM/yyyy') as fecha_creacion",
                "iem.num_expediente as numero_registro",
                "iem.num_expediente_judicial as expediente_judicial",
                "iem.num_oficio as numero_oficio",
                "to_char(iem.fecha_oficio, 'DD/MM/yyyy') as fecha_oficio",
                "iem.observacion as observaciones",
                "to_char(iemd.fecha_resolucion, 'DD/MM/yyyy') as fecha_resolucion",
                "iemd.num_resolucion as numero_resolucion",
                "cat_tipo_disp.nombre as tipo_registro",
                "cat_sit_jur.nombre as situacion",
                "cat_var.nombre as situacion",
                "to_char(iemd.fecha_inicio_medida, 'DD/MM/yyyy') as fecha_inicio_medida",
                "to_char(iemd.fecha_fin_medida, 'DD/MM/yyyy') as fecha_fin_medida",
                "cat_corte_just.nombre as corte_justicia",
                "cat_inst.nombre as instancia",
                "cat_esp.nombre as especialidad",
                "iemd.organo_jurisdiccional as organo",
                "iemd.monto_reparacion as monto_reparacion_civil"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_expediente_matriz iem on iem.id_ficha_identificacion = a.id_ficha_identificacion and iem.removido = false
                left join ia_expediente_matriz_detalle iemd on iemd.id_expediente = iem.id_expediente and iemd.removido = false
                LEFT JOIN par_catalogo cat_tipo_disp ON cat_tipo_disp.id_catalogo = iemd.id_catalogo_tipo_registro
                LEFT JOIN par_catalogo cat_sit_jur ON cat_sit_jur.id_catalogo = iemd.id_catalogo_situacion_juridica
                LEFT JOIN par_catalogo cat_var ON cat_var.id_catalogo = iemd.id_catalogo_tipo_variacion
                LEFT JOIN par_catalogo cat_corte_just ON cat_corte_just.id_catalogo = iemd.id_catalogo_corte_justicia
                LEFT JOIN par_catalogo cat_inst ON cat_inst.id_catalogo = iemd.id_catalogo_instancia
                LEFT JOIN par_catalogo cat_esp ON cat_esp.id_catalogo = iemd.id_catalogo_especialidad
                """;
    }
}

