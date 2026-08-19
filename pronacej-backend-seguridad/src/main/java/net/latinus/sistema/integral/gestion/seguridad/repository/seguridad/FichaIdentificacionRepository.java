package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIdentificacionResumenDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FichaIdentificacionRepository extends JpaRepository<FichaIdentificacion, Long> {

    List<FichaIdentificacion> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido);
    
    FichaIdentificacion findByIdFichaIdentificacion(Long idFichaIdentificacion);
    
    FichaIdentificacion findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<FichaIdentificacion> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);

    List<FichaIdentificacion> findByTokenIdentificadorNotAndRemovidoAndNumeroIdentificacion(String tokenIdentificador,
                                                                                            Boolean removido, String numeroIdentificacion);

    @Query("SELECT f FROM FichaIdentificacion f WHERE f.tokenIdentificador != :tokenIdentificador AND f.removido = :removido AND f.numeroIdentificacion IS NOT NULL AND TRIM(f.numeroIdentificacion) <> ''")
    List<FichaIdentificacion> findByTokenIdentificadorNotAndRemovidoAndNumeroIdentificacion(@Param("tokenIdentificador") String tokenIdentificador,
                                                                                            @Param("removido") Boolean removido);


    List<FichaIdentificacion> findByNumeroIdentificacionAndRemovido(String numeroIdentificacion,
                                                                                            Boolean removido);

    List<FichaIdentificacion> findByNumeroIdentificacionAndRemovidoOrderByIdFichaIdentificacionDesc(String numeroIdentificacion,
                                                                                                    Boolean removido);

    @Query("SELECT f FROM FichaIdentificacion f WHERE f.numeroIdentificacion = :numeroIdentificacion AND f.removido = :removido AND f.numeroIdentificacion IS NOT NULL AND TRIM(f.numeroIdentificacion) <> '' ORDER BY f.idFichaIdentificacion DESC")
    List<FichaIdentificacion> findByNumeroIdentificacionNotNullAndRemovidoOrderByIdFichaIdentificacionDesc(@Param("numeroIdentificacion") String numeroIdentificacion,
                                                                                                    @Param("removido") Boolean removido);


    Page<FichaIdentificacion> findByEmpresaIdEmpresaAndRemovidoAndCentroIngreso_TokenIdentificador(Long idEmpresa, Boolean removido, String tokenIdentificador ,Pageable pageable);

    Page<FichaIdentificacion> findByEmpresaIdEmpresaAndRemovidoAndNombresContainingIgnoreCase(Long idEmpresa, Boolean removido, String nombres ,Pageable pageable);

    Page<FichaIdentificacion> findByEmpresaIdEmpresaAndRemovidoAndCentroIngreso_TokenIdentificadorAndNombresContainingIgnoreCase(Long idEmpresa, Boolean removido, String nombres ,String tokenIdentificador ,Pageable pageable);

    List<FichaIdentificacion> findByEmpresaIdEmpresaAndRemovidoAndCentroIngreso_TokenIdentificador(Long idEmpresa, Boolean removido, String tokenIdentificador);

    Page<FichaIdentificacion> findByEmpresaIdEmpresaAndRemovidoAndCentroIngreso_JerarquiaPadre_Nemonico(
            Long idEmpresa, boolean removido, String nemonicoPadre, Pageable pageable
    );

    @Query("SELECT EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM f.fechaNacimiento) AS edad, COUNT(f) " +
            "FROM FichaIdentificacion f " +
            "WHERE f.fechaNacimiento IS NOT NULL AND f.removido = false " +
            "GROUP BY edad " +
            "ORDER BY edad ASC")
    List<Object[]> obtenerEstadisticasPorEdad();

    @Query("SELECT EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM f.fechaNacimiento) AS edad, COUNT(f) " +
            "FROM FichaIdentificacion f " +
            "LEFT JOIN f.tipoSexo s " +
            "LEFT JOIN f.centroIngreso ci " +
            "WHERE f.fechaNacimiento IS NOT NULL " +
            "AND f.removido = false " +
            "AND (:nemonicoTipoSexo IS NULL OR s.nemonico = :nemonicoTipoSexo) " +
            "AND (:tokenIdentificadorCentro IS NULL OR ci.tokenIdentificador = :tokenIdentificadorCentro) " +
            "AND (:nemonicoCentro IS NULL OR (ci.jerarquiaPadre IS NOT NULL AND ci.jerarquiaPadre.nemonico = :nemonicoCentro)) " +
            "GROUP BY edad " +
            "ORDER BY edad ASC")
    List<Object[]> obtenerEstadisticasPorEdad(@Param("nemonicoTipoSexo") String nemonicoTipoSexo,
                                              @Param("tokenIdentificadorCentro") String tokenIdentificadorCentro,
                                              @Param("nemonicoCentro") String nemonicoCentro);


    @Query("SELECT f FROM FichaIdentificacion f " +
            "LEFT JOIN f.paisNacimiento p " +
            "LEFT JOIN f.tipoOcupacion t " +
            "LEFT JOIN f.estadoCivil e " +
            "LEFT JOIN f.estado es " +
            "LEFT JOIN f.tipoEntrada te " +
            "LEFT JOIN f.tipoIdentificacion ti " +
            "WHERE f.empresa.idEmpresa = :empresaId " +
            "AND f.removido = false " +
            "AND (:tokenCentro IS NULL OR f.centroIngreso.tokenIdentificador = :tokenCentro) " +
            "AND (:filter IS NULL OR :filter = '' OR " +
            "LOWER(f.nombres) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(f.apellidoPaterno) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(f.apellidoMaterno) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(f.numeroIdentificacion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(p.gentilicio) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR (te IS NOT NULL AND LOWER(te.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))) " +
            "OR (ti IS NOT NULL AND LOWER(ti.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))) " +
            ") " +
            "AND (" +
            "  (:validarEstado = true AND f.estado IS NOT NULL AND es.nemonico = 'ESTADO_ADOLESCENTE_LIBRE')" +
            "  OR (:validarEstado = false AND (f.estado IS NULL OR es.nemonico <> 'ESTADO_ADOLESCENTE_LIBRE'))" +
            ")" +
            "AND (:validarPostEgreso = false AND (f.postEgreso IS NULL OR f.postEgreso = false) " +
            "     OR :validarPostEgreso = true AND f.postEgreso = true)")
    Page<FichaIdentificacion> buscarPorFiltroTokenCentro(
            @Param("empresaId") Long empresaId,
            @Param("tokenCentro") String tokenCentro,
            @Param("filter") String filter,
            @Param("validarEstado") boolean validarEstado,
            @Param("validarPostEgreso") boolean validarPostEgreso,
            Pageable pageable);



    @Query("SELECT f FROM FichaIdentificacion f " +
            "LEFT JOIN f.paisNacimiento p " +
            "LEFT JOIN f.tipoOcupacion t " +
            "LEFT JOIN f.estadoCivil e " +
            "LEFT JOIN f.centroIngreso ci " +
            "LEFT JOIN ci.jerarquiaPadre jp " +
            "LEFT JOIN f.estado es " +
            "LEFT JOIN f.tipoEntrada te " +
            "LEFT JOIN f.tipoIdentificacion ti " +
            "WHERE f.empresa.idEmpresa = :empresaId " +
            "AND f.removido = false " +
            "AND (:nemonicoPadre IS NULL OR jp.nemonico = :nemonicoPadre) " + // Filtro por el nemónico del padre
            "AND (:filter IS NULL OR :filter = '' OR " +
            "LOWER(f.nombres) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(f.apellidoPaterno) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(f.apellidoMaterno) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(f.numeroIdentificacion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(p.gentilicio) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR (te IS NOT NULL AND LOWER(te.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))) " +
            "OR (ti IS NOT NULL AND LOWER(ti.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))) " +
            ") " +
            "AND (" +
            "  (:validarEstado = true AND f.estado IS NOT NULL AND es.nemonico = 'ESTADO_ADOLESCENTE_LIBRE')" +
            "  OR (:validarEstado = false AND (f.estado IS NULL OR es.nemonico <> 'ESTADO_ADOLESCENTE_LIBRE'))" +
            ")"+
            "AND (:validarPostEgreso = false AND (f.postEgreso IS NULL OR f.postEgreso = false) " +
            "     OR :validarPostEgreso = true AND f.postEgreso = true)")

    Page<FichaIdentificacion> buscarPorFiltroCentroPadre(
            @Param("empresaId") Long empresaId,
            @Param("nemonicoPadre") String nemonicoPadre,
            @Param("filter") String filter,
            @Param("validarEstado") boolean validarEstado,
            @Param("validarPostEgreso") boolean validarPostEgreso,
            Pageable pageable);

    @Query("SELECT f FROM FichaIdentificacion f " +
            "WHERE f.numeroIdentificacion = :numeroIdentificacion " +
            "AND f.removido = false " +
            "AND (:tokenCentro IS NULL OR f.centroIngreso.tokenIdentificador = :tokenCentro) " +
            "ORDER BY f.idFichaIdentificacion DESC")
    List<FichaIdentificacion> findByNumeroIdentificacionAndCentroIngresoAndRemovidoOrderByIdFichaIdentificacionDesc(
            @Param("numeroIdentificacion") String numeroIdentificacion,
            @Param("tokenCentro") String tokenCentro);

    @Query("SELECT c.nombre, COUNT(f) " +
            "FROM Catalogo c " +
            "LEFT JOIN FichaIdentificacion f ON f.estado = c " +
            "AND (:tokenCentro IS NULL OR f.centroIngreso.tokenIdentificador = :tokenCentro) " +
            "WHERE c.removido = false " +
            "AND c.catalogoPadre IS NOT NULL " +
            "AND c.catalogoPadre.nemonico = 'ESTADO_ADOLESCENTE' " +
            "GROUP BY c.nombre " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> countFichaIdentificacionEstados(
            @Param("tokenCentro") String tokenCentro
    );

    @Query("SELECT c.nombre, COUNT(f) " +
            "FROM Catalogo c " +
            "LEFT JOIN FichaIdentificacion f ON f.tipoSexo = c " +
            "AND (:tokenCentro IS NULL OR f.centroIngreso.tokenIdentificador = :tokenCentro) " +
            "WHERE c.removido = false " +
            "AND c.catalogoPadre IS NOT NULL " +
            "AND c.catalogoPadre.nemonico = 'TIPO_SEXO' " +
            "GROUP BY c.nombre " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> countFichaIdentificacionSexo(
            @Param("tokenCentro") String tokenCentro
    );

    @Query("""
        select
            UPPER(TRIM(
                   CONCAT(
                       COALESCE(TRIM(fi.apellidoPaterno), ''),
                       CASE
                           WHEN fi.apellidoPaterno IS NOT NULL AND TRIM(fi.apellidoPaterno) <> '' THEN ' '
                           ELSE ''
                       END,
                       COALESCE(TRIM(fi.apellidoMaterno), ''),
                       CASE
                           WHEN fi.apellidoMaterno IS NOT NULL AND TRIM(fi.apellidoMaterno) <> '' THEN ' '
                           ELSE ''
                       END,
                       COALESCE(TRIM(fi.nombres), '')
                   )
            )) as nombreCompleto,
            fi.numeroIdentificacion,
            c.nombre as centro,
            UPPER(es.nombre) as estado,
            fi.tokenIdentificador
        from FichaIdentificacion fi
        join fi.centroIngreso c
        join fi.estado es
        join fi.empresa em
        where
            em.tokenIdentificador = :tokenEmpresa
            and
            fi.removido = :removido
            and (
                :valorBusqueda is null
                or trim(:valorBusqueda) = ''
                or UPPER(TRIM(
                    CONCAT(
                        COALESCE(TRIM(fi.apellidoPaterno), ''),
                        ' ',
                        COALESCE(TRIM(fi.apellidoMaterno), ''),
                        ' ',
                        COALESCE(TRIM(fi.nombres), '')
                    )
                )) like CONCAT('%', UPPER(TRIM(:valorBusqueda)), '%')
                or UPPER(TRIM(fi.numeroIdentificacion)) like CONCAT('%', UPPER(TRIM(:valorBusqueda)), '%')
                or UPPER(TRIM(c.nombre)) like CONCAT('%', UPPER(TRIM(:valorBusqueda)), '%')
                or UPPER(TRIM(es.nombre)) like CONCAT('%', UPPER(TRIM(:valorBusqueda)), '%')
            )
        order by nombreCompleto
    """)
    Page<FichaIdentificacionResumenDTO> obtenerFichasResumenPorTokenEmpresaYRemovido(String tokenEmpresa, Boolean removido, String valorBusqueda, Pageable pageable);
}
