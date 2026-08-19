package net.latinus.sistema.integral.gestion.seguridad.repository.reporte.seccion;

import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sección «Ficha Principal» de la exportación dinámica de adolescentes.
 * <p>
 * Aporta el número de identificación y el tipo de documento vinculado
 * al catálogo {@code par_catalogo cat_tip_doc}.
 * </p>
 */
@Component
public class SeccionFichaPrincipal implements SeccionExportacion {

    @Override
    public String getNemonico() {
        return EtiquetaNemonico.SECCION_FICHA_IDENT_FICHA_PRINCIPAL;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(
                "tipo de documento",
                "numero de identificación",
                "tipo de sexo",
                "fecha de nacimiento",
                "edad",
                "alias",
                "estado civil",
                "genero",
                "número de hijos",
                "ocupación",
                "origen étnico",
                "modalidad de estudio",
                "nivel de estudio",
                "país de nacimiento",
                "código ubi nacimiento",
                "ubigeo nacimiento",
                "dirección",
                "código ubi dirección",
                "ubigeo dirección",
                "impedimento discapacidad"
        );
    }

    @Override
    public List<String> getColumnasSelect() {
        return List.of(
                "cat_tip_doc.nombre AS tipo_documento",
                "a.numero_identificacion",
                "cat_tip_sexo.nombre as tipo_sexo",
                "to_char(a.fecha_nacimiento, 'DD/MM/yyyy') as fecha_nacimiento",
                "(EXTRACT(YEAR FROM AGE(now(), a.fecha_nacimiento))) as edad",
                "a.alias",
                "cat_est_civil.nombre as estado_civil",
                "cat_gen.nombre as genero",
                "a.numero_hijos as hijos",
                "cat_ocup.nombre as ocupacion",
                "cat_orig_et.nombre as origen_etnico",
                "cat_mod_est.nombre as modalidad_estudio",
                """
                case
                    when a.nivel_eba is not null then cat_nivel_eba.nombre
                    when a.nivel_ebr is not null then cat_nivel_ebr.nombre
                    when a.nivel_superior is not null then cat_nivel_super.nombre
                    else null
                end as nivel_estudio
                """,
                "loc_pais_nac.nombre as pais_nacimiento",
                "a.codigo_ubigeo_nacimiento as ubigeo_nacimiento_codigo",
                "loc_ubi_nac.nombre as ubigeo_nacimiento",
                "a.direccion",
                "a.codigo_ubigeo_direccion as ubigeo_direccion_codigo",
                "loc_ubi_dir.nombre as ubigeo_direccion",
                """
                case
                    when a.impedimento_discapacidad is true then 'SI'
                    else 'NO'
                end as impedimento_discapacidad
                """
        );
    }

    @Override
    public String getJoinSql() {
        return """
                    LEFT JOIN par_catalogo cat_tip_doc ON cat_tip_doc.id_catalogo = a.tipo_identificacion
                    left join par_catalogo cat_tip_sexo on cat_tip_sexo.id_catalogo = a.tipo_sexo
                    left join par_catalogo cat_est_civil on cat_est_civil.id_catalogo = a.estado_civil
                    left join par_catalogo cat_gen on cat_gen.id_catalogo = a.tipo_genero
                    left join par_catalogo cat_ocup on cat_ocup.id_catalogo = a.id_tipo_ocupacion
                    left join par_catalogo cat_orig_et on cat_orig_et.id_catalogo = a.origen_etnico
                    left join par_catalogo cat_mod_est on cat_mod_est.nemonico = a.modalidad_estudio
                    left join par_catalogo cat_nivel_eba on cat_nivel_eba.id_catalogo = a.nivel_eba
                    left join par_catalogo cat_nivel_ebr on cat_nivel_ebr.id_catalogo = a.nivel_ebr
                    left join par_catalogo cat_nivel_super on cat_nivel_super.id_catalogo = a.nivel_superior
                    left join par_localidades loc_pais_nac on loc_pais_nac.id_localidad = a.id_pais_nacimiento
                    left join par_localidades loc_ubi_nac on loc_ubi_nac.codigo_ubigeo = a.codigo_ubigeo_nacimiento
                    left join par_localidades loc_ubi_dir on loc_ubi_dir.codigo_ubigeo = a.codigo_ubigeo_direccion
                """;
    }

}

