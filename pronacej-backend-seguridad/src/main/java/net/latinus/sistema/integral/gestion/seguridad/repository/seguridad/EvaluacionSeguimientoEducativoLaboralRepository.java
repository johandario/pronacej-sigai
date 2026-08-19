package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionSeguimientoEducativoLaboral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluacionSeguimientoEducativoLaboralRepository extends JpaRepository<EvaluacionSeguimientoEducativoLaboral, Long> {
    
    List<EvaluacionSeguimientoEducativoLaboral> findByRemovido(boolean removido);
    
    EvaluacionSeguimientoEducativoLaboral findByIdEvaluacionSeguimiento(Long idEvaluacionSeguimiento);
    
    EvaluacionSeguimientoEducativoLaboral findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<EvaluacionSeguimientoEducativoLaboral> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<EvaluacionSeguimientoEducativoLaboral> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(String tokenIdentificadorFichaIdentificacion, Long idEmpresa, Boolean removido, Pageable pageable);

    @Query("SELECT e FROM EvaluacionSeguimientoEducativoLaboral e " +
            "LEFT JOIN e.institucionEducativaLaboral i " +
            "LEFT JOIN e.usuarioSistemaCrea u " +
            "WHERE e.removido = false " +
            "AND e.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND (" +
            ":filter IS NULL OR :filter = '' OR (" +
            "LOWER(i.nombreOrganizacion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(e.tipoEntidad) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR CAST(e.fechaInicio AS string) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(u.nombres) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :filter, '%'))" +
            "))")
    Page<EvaluacionSeguimientoEducativoLaboral> buscarPorFiltro(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("filter") String filter,
            Pageable pageable
    );



}