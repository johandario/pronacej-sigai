package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JerarquiaRepository extends JpaRepository<Jerarquia, Long> {

    List<Jerarquia> findByNoMostrarEnFrontAndRemovido(Boolean noMostrarEnFront, Boolean removido);
    
    Jerarquia findJerarquiaByIdJerarquia(Long idJerarquia);
    
    Jerarquia findJerarquiaByTokenIdentificador(String tokenIdentificador);
    
    List<Jerarquia> findByJerarquiaPadreNemonicoAndEmpresaTokenIdentificadorAndRemovidoOrderByIdJerarquiaDesc(String nemonicoPadre, String tokenIdentificadorEmpresa, Boolean removido);

    List<Jerarquia> findByJerarquiaPadreNemonicoInAndEmpresaTokenIdentificadorAndRemovidoOrderByIdJerarquiaDesc(List<String> nemonicosPadre, String tokenIdentificadorEmpresa, Boolean removido);

    List<Jerarquia> findByJerarquiaPadreTokenIdentificadorAndRemovido(String tokenIdentificadorPadre, Boolean removido);

    Jerarquia findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<Jerarquia> findByNombreIgnoreCaseAndRemovido(String nombre, Boolean removido);

    @Query("SELECT j.nombre, COALESCE(COUNT(f), 0) " +
            "FROM Jerarquia j " +
            "LEFT JOIN FichaIdentificacion f ON f.centroIngreso = j AND j.removido = false " +
            "LEFT JOIN f.tipoSexo s " +
            "LEFT JOIN f.centroIngreso ci " +
            "WHERE j.jerarquiaPadre IS NOT NULL " +
            "AND (j.jerarquiaPadre.nemonico = 'CJDR' OR j.jerarquiaPadre.nemonico = 'SOA') " +
            "AND f.estado IS NOT NULL AND f.estado.nemonico <> 'ESTADO_ADOLESCENTE_LIBRE' " +
            "AND f.empresa.idEmpresa = :empresaId " +
            "AND f.removido = false " +
            "AND (:nemonicoTipoSexo IS NULL OR s.nemonico = :nemonicoTipoSexo) " +
            "AND (:tokenIdentificadorCentro IS NULL OR ci.tokenIdentificador = :tokenIdentificadorCentro) " +
            "AND (:nemonicoCentro IS NULL OR (ci.jerarquiaPadre IS NOT NULL AND ci.jerarquiaPadre.nemonico = :nemonicoCentro)) " +
            "GROUP BY j.nombre " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> countFichasPorCentro(@Param("empresaId") Long empresaId,
                                        @Param("nemonicoTipoSexo") String nemonicoTipoSexo,
                                        @Param("tokenIdentificadorCentro") String tokenIdentificadorCentro,
                                        @Param("nemonicoCentro") String nemonicoCentro);


    @Query("SELECT j.nombre, COALESCE(COUNT(f), 0) " +
            "FROM Jerarquia j  "+
            "LEFT JOIN FichaIdentificacion f on f.centroIngreso = j and j.removido = false " +
            " WHERE j.jerarquiaPadre IS NOT NULL AND (j.jerarquiaPadre.nemonico = 'SOA')" +
            " AND f.estado IS NOT NULL AND f.estado.nemonico <> 'ESTADO_ADOLESCENTE_LIBRE' " +
            " AND f.empresa.idEmpresa = :empresaId " +
            " AND f.removido = false " +
            "GROUP BY j.nombre " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> countFichasPorCentroSOA(@Param("empresaId") Long empresaId);

    @Query("SELECT j.nombre, COALESCE(COUNT(f), 0) " +
            "FROM Jerarquia j  "+
            "LEFT JOIN FichaIdentificacion f on f.centroIngreso = j and j.removido = false " +
            " WHERE j.jerarquiaPadre IS NOT NULL AND (j.jerarquiaPadre.nemonico = 'CJDR')" +
            " AND f.estado IS NOT NULL AND f.estado.nemonico <> 'ESTADO_ADOLESCENTE_LIBRE' " +
            " AND f.empresa.idEmpresa = :empresaId " +
            " AND f.removido = false " +
            "GROUP BY j.nombre " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> countFichasPorCentroCJDR(@Param("empresaId") Long empresaId);

    Jerarquia findJerarquiaByNemonico(String tokenIdentificador);

    List<Jerarquia> findByJerarquiaPadreIdJerarquiaAndEmpresaIdEmpresaAndRemovido(Long idJerarquiaPadre, Long idEmpresa, Boolean removido);

    @Query("SELECT j FROM Jerarquia j " +
            "WHERE j.empresa.idEmpresa = :empresaId " +
            "AND j.removido = false " +
            "AND j.jerarquiaPadre IS NOT NULL " +
            "AND j.jerarquiaPadre.nemonico IN ('SOA', 'CJDR', 'UAPISE') " +
            "ORDER BY j.nombre ASC")
    List<Jerarquia> obtenerCentrosDashboard(@Param("empresaId") Long empresaId);
}
