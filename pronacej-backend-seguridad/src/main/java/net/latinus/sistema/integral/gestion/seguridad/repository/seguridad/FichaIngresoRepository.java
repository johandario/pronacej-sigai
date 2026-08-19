package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIngreso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FichaIngresoRepository extends JpaRepository<FichaIngreso, Long> {

    List<FichaIngreso> findByRemovido(boolean removido);
    
    FichaIngreso findByIdFichaIngreso(Long idFichaIngreso);
        
    FichaIngreso findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<FichaIngreso> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<FichaIngreso> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(String tokenIdentificadorFichaIdentificacion, Long idEmpresa, Boolean removido, Pageable pageable);

    Long countByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion,Boolean removido);

    @Query("SELECT fin FROM FichaIngreso fin " +
            "JOIN fin.fichaIdentificacion fid " +
            "WHERE fid.tokenIdentificador = :tokenIdentificador " +
            "AND fin.removido = false " +
            "ORDER BY fin.fechaCreacion DESC " +
            "LIMIT 1")
    FichaIngreso obtenerUltimaFichaIngresoValidaPorTokenFichaIdentificacion(@Param("tokenIdentificador") String tokenIdentificadorFichaIdentificacion);

    List<FichaIngreso> findByFichaIdentificacionTokenIdentificadorAndRemovidoAndActivo(String tokenIdentificadorFichaIdentificacion,Boolean removido, Boolean activo);

    @Query("SELECT fi FROM FichaIngreso fi " +
            "LEFT JOIN fi.centro c " +
            "LEFT JOIN fi.seguroSalud s " +
            "WHERE fi.empresa.idEmpresa = :empresaId " +
            "AND fi.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND fi.removido = false " +
            "AND (:filter IS NULL OR :filter = '' OR " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :filter, '%')))")
    Page<FichaIngreso> buscarPorCentroOSeguro(
            @Param("empresaId") Long empresaId,
            @Param("filter") String filter,
            @Param("tokenIdentificador") String tokenIdentificador,
            Pageable pageable);

}
