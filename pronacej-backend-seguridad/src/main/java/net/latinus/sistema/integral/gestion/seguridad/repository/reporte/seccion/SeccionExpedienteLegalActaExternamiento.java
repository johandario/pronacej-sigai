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
public class SeccionExpedienteLegalActaExternamiento implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_EXPEDIENTE_MATRIZ_ACTAS_EXTERNAMIENTO;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "fecha_creacion",
                "expediente",
                "ingreso_correspondiente",
                "juzgado_institucion",
                "autorizacion",
                "tipo_documento",
                "numero_documento",
                "resolucion",
                "domicilio",
                "nombre_familiar",
                "parentesco_familiar",
                "identificacion_familiar",
                "direccion_familiar",
                "telefono_familiar",
                "observaciones_comentarios"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "to_char(iae.fecha_creacion, 'DD/MM/yyyy HH24:MM') as fecha_creacion",
                "iem1.num_expediente as expediente",
                "iae.ingreso as ingreso_correspondiente",
                "iae.institucion as juzgado_institucion",
                "iae.autorizacion as autorizacion",
                "iae_cat_tipo_doc.nombre as tipo_documento",
                "iae.numero_documento as numero_documento",
                "iae.resolucion as resolucion",
                "iae.domicilio as domicilio",
                "iae.familiares as nombre_familiar",
                "iae.parentescos as parentesco_familiar",
                "iae.identificaciones as identificacion_familiar",
                "iae.direcciones as direccion_familiar",
                "iae.telefonos as telefono_familiar",
                "iae.observaciones as observaciones_comentarios"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                left join ia_expediente_matriz iem1 on iem1.id_ficha_identificacion = a.id_ficha_identificacion and iem1.removido = false
                left join ia_acta_externamiento iae on iae.id_expediente_matriz = iem1.id_expediente and iae.removido = false
                left join par_catalogo iae_cat_tipo_doc on iae_cat_tipo_doc.id_catalogo = iae.tipo_documento
                """;
    }
}

