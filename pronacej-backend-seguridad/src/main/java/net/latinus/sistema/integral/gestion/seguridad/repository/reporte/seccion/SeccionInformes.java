package net.latinus.sistema.integral.gestion.seguridad.repository.reporte.seccion;

import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sección «Informes» de la exportación dinámica de adolescentes.
 * <p>
 * Aporta información consolidada de los informes registrados por adolescente:
 * - Cabecera del informe      (inf_informe / inf_plantilla_informe)
 * - Campos y valores          (inf_campo / inf_valor)
 * </p>
 * <p>
 * La columna dinámica es {@code ic.etiqueta} (campo del informe) y su valor
 * correspondiente {@code iv.valor}.
 * </p>
 */
@Component
public class SeccionInformes implements SeccionExportacionDinamica {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_INFORMES;
    }

    @Override
    public String getAliasPivot() {
        return "informes_pivot";
    }

    @Override
    public List<String> getHeadersBaseDinamicas() {
        return List.of(
                "identificador",
                "tipo_informe"
        );
    }

    @Override
    public List<String> getColumnasBaseDinamicas(String aliasPivot) {
        return List.of(
                aliasPivot + ".id_grupo as identificador",
                aliasPivot + ".nombre_grupo as tipo_informe"
        );
    }

    @Override
    public String getSqlFuentePivot() {
        return """
                select
                    ii.id_ficha_identificacion as id_ficha_identificacion,
                    ii.id_informe              as id_grupo,
                    ipi.nombre                 as nombre_grupo,
                    ic.etiqueta                as pregunta,
                    iv.valor                   as respuesta
                from inf_informe ii
                left join inf_plantilla_informe ipi on ipi.id_plantilla_informe = ii.id_plantilla_informe
                left join inf_valor iv              on iv.id_informe = ii.id_informe
                left join inf_campo ic              on ic.id_campo = iv.id_campo
                """;
    }


}

