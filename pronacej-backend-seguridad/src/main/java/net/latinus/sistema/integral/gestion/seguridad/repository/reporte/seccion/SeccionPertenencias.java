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
public class SeccionPertenencias implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_ENTREGA_RETIRO_DE_PERTENENCIAS;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "retirados",
                "entregados",
                "retirados salida"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "pert.ing_retirados as retirados",
                "pert.ing_entregados as entregados",
                "pert.sal_retirados as retirados_salida"
        );
    }

    @Override
    public String getJoinSql() {
        return """
                LEFT JOIN ia_pertenencia ip ON ip.id_ficha_identificacion = a.id_ficha_identificacion and ip.removido = false
                LEFT JOIN LATERAL (
                     select
                       STRING_AGG(
                         CONCAT('(', d.cantidad, ') ', d.nombre, ' - ', d.cat_tipo, ' - ', d.cat_estado),
                         ', '
                       ) FILTER (WHERE d.tipo = 'RETIRADOS') AS ing_retirados,
                       STRING_AGG(
                         CONCAT('(', d.cantidad, ') ', d.nombre, ' - ', d.cat_tipo, ' - ', d.cat_estado),
                         ', '
                       ) FILTER (WHERE d.tipo = 'ENTREGADOS') AS ing_entregados,
                       STRING_AGG(
                         CONCAT('(', d.cantidad, ') ', d.nombre, ' - ', d.cat_tipo, ' - ', d.cat_estado),
                         ', '
                       ) FILTER (WHERE d.tipo = 'RETIRADOS_SALIDA') AS sal_retirados
                     from (
                         select
                           ipd.id_pertenencia_ingreso as id_pertenencia,
                           'RETIRADOS' as tipo,
                           ipd.cantidad,
                           ipd.nombre,
                           pc.nombre as cat_tipo,
                                    pc1.nombre as cat_estado
                         from ia_pertenencia_detalle ipd
                         left join par_catalogo pc on pc.id_catalogo = id_catalogo_tipo
                                left join par_catalogo pc1 on pc1.id_catalogo = id_catalogo_estado
                         where ipd.id_pertenencia_ingreso is not null and ipd.removido = false
                         UNION ALL
                         select
                           ipd.id_pertenencia_egreso,
                           'ENTREGADOS',
                           ipd.cantidad,
                           ipd.nombre,
                           pc.nombre as cat_tipo,
                                    pc1.nombre as cat_estado
                         from ia_pertenencia_detalle ipd
                         left join par_catalogo pc on pc.id_catalogo = id_catalogo_tipo
                                left join par_catalogo pc1 on pc1.id_catalogo = id_catalogo_estado
                         where ipd.id_pertenencia_egreso is not null and ipd.removido = false
                         UNION ALL
                         select
                           ipd.id_pertenencia_salida_ingreso,
                           'RETIRADOS_SALIDA',
                           ipd.cantidad,
                           ipd.nombre,
                           pc.nombre as cat_tipo,
                                    pc1.nombre as cat_estado
                         from ia_pertenencia_detalle ipd
                         left join par_catalogo pc on pc.id_catalogo = id_catalogo_tipo
                                left join par_catalogo pc1 on pc1.id_catalogo = id_catalogo_estado
                         where ipd.id_pertenencia_salida_ingreso is not null and ipd.removido = false
                     ) d
                     where d.id_pertenencia = ip.id_pertenencia
                ) pert ON true
                """;
    }
}

