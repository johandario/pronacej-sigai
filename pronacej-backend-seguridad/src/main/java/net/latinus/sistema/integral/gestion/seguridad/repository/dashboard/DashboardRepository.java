package net.latinus.sistema.integral.gestion.seguridad.repository.dashboard;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio dedicado al módulo de Dashboard.
 * <p>
 * Centraliza todas las consultas de estadísticas agregadas de {@link FichaIdentificacion}
 * filtradas por empresa (obligatorio) y opcionalmente por centro (Jerarquía).
 * </p>
 * <p>
 * <b>Cómo agregar un nuevo criterio:</b>
 * <ol>
 *   <li>Añadir aquí un nuevo método {@code countPorXxx} con su @Query.</li>
 *   <li>Añadir el campo {@code List<EstadisticaItemDTO> porXxx} en {@code DashboardEstadisticasDTO}.</li>
 *   <li>Llamarlo en {@code DashboardServiceImpl.obtenerEstadisticas} usando {@code mapearResultados()}.</li>
 * </ol>
 * </p>
 */
@Repository
public interface DashboardRepository extends JpaRepository<FichaIdentificacion, Long> {

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICA POR DELITO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cuenta adolescentes activos agrupados por delito
     * Object[0] = String (delito), Object[1] = Long (cantidad).
     */
    @Query("SELECT " +
            "  emd.delitoEspecifico.nombre AS delito, " +
            "  COUNT(f) " +
            "FROM ExpedienteMatrizDelito emd " +
            "JOIN emd.expedienteMatrizDetalle.expedienteMatriz.fichaIdentificacion f " +
            "WHERE f.empresa.idEmpresa = :empresaId " +
            "AND f.removido = false " +
            "AND f.fechaNacimiento IS NOT NULL " +
            "AND (:tokenCentro IS NULL OR f.centroIngreso.tokenIdentificador = :tokenCentro) " +
            "GROUP BY delito " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> countPorDelito(@Param("empresaId") Long empresaId,
                                @Param("tokenCentro") String tokenCentro);

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICA POR EDAD
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cuenta adolescentes activos agrupados por edad calculada.
     * Object[0] = Integer (edad), Object[1] = Long (cantidad).
     */
    @Query("SELECT " +
            "  EXTRACT(YEAR FROM FUNCTION('AGE', NOW(), f.fechaNacimiento)) AS edad, " +
            "  COUNT(f) " +
            "FROM FichaIdentificacion f " +
            "WHERE f.empresa.idEmpresa = :empresaId " +
            "AND f.removido = false " +
            "AND f.fechaNacimiento IS NOT NULL " +
            "AND (:tokenCentro IS NULL OR f.centroIngreso.tokenIdentificador = :tokenCentro) " +
            "GROUP BY edad " +
            "ORDER BY 1 DESC")
    List<Object[]> countPorEdad(@Param("empresaId") Long empresaId,
                                @Param("tokenCentro") String tokenCentro);

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICA POR SEXO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cuenta adolescentes activos agrupados por tipo de sexo (catálogo).
     * Object[0] = String (nombre de sexo o 'Sin información'), Object[1] = Long (cantidad).
     */
    @Query("SELECT " +
            "  COALESCE(ts.nombre, 'Sin información'), " +
            "  COUNT(f) " +
            "FROM FichaIdentificacion f " +
            "LEFT JOIN f.tipoSexo ts " +
            "WHERE f.empresa.idEmpresa = :empresaId " +
            "AND f.removido = false " +
            "AND (:tokenCentro IS NULL OR f.centroIngreso.tokenIdentificador = :tokenCentro) " +
            "GROUP BY ts.nombre " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> countPorSexo(@Param("empresaId") Long empresaId,
                                @Param("tokenCentro") String tokenCentro);

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICA POR NACIONALIDAD
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cuenta adolescentes activos agrupados por nacionalidad (gentilicio del país de nacimiento).
     * Si el gentilicio está vacío o nulo, usa el nombre del país; si no tiene datos, 'Sin información'.
     * Object[0] = String (gentilicio/nombre/Sin información), Object[1] = Long (cantidad).
     */
    @Query("SELECT " +
            "  CASE " +
            "    WHEN f.paisNacimiento IS NULL THEN 'Sin información' " +
            "    WHEN f.paisNacimiento.gentilicio IS NOT NULL AND TRIM(f.paisNacimiento.gentilicio) <> '' THEN f.paisNacimiento.gentilicio " +
            "    ELSE COALESCE(f.paisNacimiento.nombre, 'Sin información') " +
            "  END AS nacionalidad, " +
            "  COUNT(f) " +
            "FROM FichaIdentificacion f " +
            "WHERE f.empresa.idEmpresa = :empresaId " +
            "AND f.removido = false " +
            "AND (:tokenCentro IS NULL OR f.centroIngreso.tokenIdentificador = :tokenCentro) " +
            "GROUP BY " +
            "  CASE " +
            "    WHEN f.paisNacimiento IS NULL THEN 'Sin información' " +
            "    WHEN f.paisNacimiento.gentilicio IS NOT NULL AND TRIM(f.paisNacimiento.gentilicio) <> '' THEN f.paisNacimiento.gentilicio " +
            "    ELSE COALESCE(f.paisNacimiento.nombre, 'Sin información') " +
            "  END " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> countPorNacionalidad(@Param("empresaId") Long empresaId,
                                        @Param("tokenCentro") String tokenCentro);

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICA POR DEPARTAMENTO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cuenta adolescentes activos agrupados por departamento.
     * <p>
     * La lógica recorre la jerarquía del catálogo {@code ubigeoDireccion}:
     * el departamento puede ser el propio catálogo, su padre o su abuelo,
     * identificado por {@code nemonico = 'DEPARTAMENTO'}.
     * Si ningún nivel alcanza 'DEPARTAMENTO', se agrupa como 'Sin información'.
     * </p>
     * Object[0] = String (nombre del departamento), Object[1] = Long (cantidad).
     */
    @Query(value = """
                SELECT
                    CASE
                        WHEN cat_base.nemonico = 'DISTRITO' THEN loc_padre2.nombre
                        WHEN cat_base.nemonico = 'PROVINCIA' THEN loc_padre1.nombre
                        WHEN cat_base.nemonico = 'DEPARTAMENTO' THEN loc_base.nombre
                        ELSE 'Sin información'
                    END AS departamento,
                    COUNT(f.id_ficha_identificacion) AS cantidad
                FROM ia_ficha_identificacion f
                LEFT JOIN seg_jerarquia centro ON centro.id_jerarquia = f.id_centro
                LEFT JOIN par_localidades loc_base ON loc_base.codigo_ubigeo = f.codigo_ubigeo_direccion
                LEFT JOIN par_localidades loc_padre1 ON loc_padre1.id_localidad = loc_base.id_localidad_padre
                LEFT JOIN par_localidades loc_padre2 ON loc_padre2.id_localidad = loc_padre1.id_localidad_padre
                LEFT JOIN par_catalogo cat_base ON cat_base.id_catalogo = loc_base.id_tipo
                WHERE f.id_empresa = :empresaId
                  AND f.removido   = false
                  AND (:tokenCentro IS NULL OR centro.token_identificador = :tokenCentro)
                GROUP BY 1
                ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countPorDepartamento(@Param("empresaId") Long empresaId,
                                        @Param("tokenCentro") String tokenCentro);

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICA POR DÍAS DE INTERNACIÓN
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cuenta adolescentes activos agrupados por días de internación.
     */
    @Query(value = """
                SELECT
                    EXTRACT(day from iemd.fecha_fin_medida - iemd.fecha_inicio_medida) as dias,
                    COUNT(f.id_ficha_identificacion) AS cantidad
                FROM ia_ficha_identificacion f
                LEFT JOIN seg_jerarquia centro ON centro.id_jerarquia = f.id_centro
                join ia_expediente_matriz iem on iem.id_ficha_identificacion = f.id_ficha_identificacion
                join lateral (
                    select d.* from ia_expediente_matriz_detalle d
                    where d.id_expediente = iem.id_expediente
                    order by d.id_expediente_detalle desc
                    limit 1
                ) iemd on true
                WHERE f.id_empresa = :empresaId
                  AND f.removido   = false
                  AND (:tokenCentro IS NULL OR centro.token_identificador = :tokenCentro)
                GROUP BY 1
                ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countPorDiasInternacion(@Param("empresaId") Long empresaId,
                                        @Param("tokenCentro") String tokenCentro);

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICA POR TIPO ENFERMEDAD
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cuenta adolescentes activos agrupados por tipo de enfermedad.
     */
    @Query(value = """
                SELECT
                    case
                        when ifme.id_clasificacion_enfermedad is not null then pce.nombre
                        when ifme.id_tipo_enfermedad is not null and ifme.id_clasificacion_enfermedad is null then pc.nombre
                        else null
                    end as nombre_enfermedad,
                    COUNT(f.id_ficha_identificacion) AS cantidad
                FROM ia_ficha_identificacion f
                LEFT JOIN seg_jerarquia centro ON centro.id_jerarquia = f.id_centro
                join ai_ficha_medica afm on afm.id_ficha_identificacion = f.id_ficha_identificacion
                join ia_ficha_medica_enfermedad ifme on ifme.id_ficha_medica = afm.id_ficha_medica
                left join par_clasificacion_enfermedad pce on pce.id_clasificacion_enfermedad = ifme.id_clasificacion_enfermedad
                left join par_catalogo pc on pc.id_catalogo = ifme.id_tipo_enfermedad
                WHERE f.id_empresa = :empresaId
                  AND f.removido   = false
                  AND (:tokenCentro IS NULL OR centro.token_identificador = :tokenCentro)
                GROUP BY 1
                ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countPorTipoEnfermedad(@Param("empresaId") Long empresaId,
                                           @Param("tokenCentro") String tokenCentro);

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICA POR GRADO DE INSTRUCCIÓN
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cuenta adolescentes activos agrupados por tipo de enfermedad.
     */
    @Query(value = """
                SELECT
                    concat_ws(
                        ' - ',
                            cat_mod_est.nombre,
                            case
                               when f.nivel_eba is not null then cat_nivel_eba.nombre
                               when f.nivel_ebr is not null then cat_nivel_ebr.nombre
                               when f.nivel_superior is not null then cat_nivel_super.nombre
                            else null
                    end
                    ) as grado_instruccion,
                    COUNT(f.id_ficha_identificacion) AS cantidad
                FROM ia_ficha_identificacion f
                LEFT JOIN seg_jerarquia centro ON centro.id_jerarquia = f.id_centro
                left join par_catalogo cat_mod_est on cat_mod_est.nemonico = f.modalidad_estudio
                left join par_catalogo cat_nivel_eba on cat_nivel_eba.id_catalogo = f.nivel_eba
                left join par_catalogo cat_nivel_ebr on cat_nivel_ebr.id_catalogo = f.nivel_ebr
                left join par_catalogo cat_nivel_super on cat_nivel_super.id_catalogo = f.nivel_superior
                WHERE f.id_empresa = :empresaId
                    AND f.removido = false
                    AND f.modalidad_estudio is not null
                    AND (:tokenCentro IS NULL OR centro.token_identificador = :tokenCentro)
                GROUP BY 1
                ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countPorGradoInstruccion(@Param("empresaId") Long empresaId,
                                          @Param("tokenCentro") String tokenCentro);

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICA POR NÚMERO DE HIJOS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cuenta adolescentes activos agrupados por número de hijos.
     */
    @Query("SELECT " +
            "  f.numeroHijos, " +
            "  COUNT(f) " +
            "FROM FichaIdentificacion f " +
            "WHERE f.empresa.idEmpresa = :empresaId " +
            "AND f.numeroHijos is not null " +
            "AND f.removido = false " +
            "AND (:tokenCentro IS NULL OR f.centroIngreso.tokenIdentificador = :tokenCentro) " +
            "GROUP BY f.numeroHijos " +
            "ORDER BY 1 DESC")
    List<Object[]> countPorNumeroHijos(@Param("empresaId") Long empresaId,
                                @Param("tokenCentro") String tokenCentro);

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICA POR NÚMERO DE CENTROS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cuenta adolescentes activos agrupados por número de centros.
     */
    @Query(value = """        
                select
                    sjp.nemonico as tipo_centro,
                    count(sjh.id_jerarquia) as total
                from seg_jerarquia sjh
                join seg_jerarquia sjp on sjp.id_jerarquia = sjh.id_jerarquia_padre
                where sjp.nemonico in ('SOA','CJDR','UAPISE')
                    and sjh.removido = false
                    and sjp.removido = false
                    and sjp.id_empresa = :empresaId
                group by 1
                order by 1
            """, nativeQuery = true)
    List<Object[]> countPorNumeroCentros(@Param("empresaId") Long empresaId);
}


