package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoEducativoLaboralOtros;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeguimientoEducativoLaboralOtrosRepository extends JpaRepository<SeguimientoEducativoLaboralOtros, Long> {
    
    List<SeguimientoEducativoLaboralOtros> findByRemovido(boolean removido);
    
    SeguimientoEducativoLaboralOtros findByIdSeguimientoEducativoLaboral(Long idSeguimientoEducativoLaboral);
    
    SeguimientoEducativoLaboralOtros findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<SeguimientoEducativoLaboralOtros> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<SeguimientoEducativoLaboralOtros> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
        String tokenIdentificadorFichaIdentificacion, 
        Long idEmpresa, 
        Boolean removido, 
        Pageable pageable);
    
    List<SeguimientoEducativoLaboralOtros> findByFichaIdentificacionTokenIdentificadorAndRemovido(
        String tokenIdentificadorFichaIdentificacion, 
        Boolean removido);
    
    /**
     * Búsqueda con filtrado de texto para seguimientos educativos/laborales/otros
     * El filtro se aplica a varios campos de texto relevantes
     */
    @Query("SELECT s FROM SeguimientoEducativoLaboralOtros s " +
           "LEFT JOIN s.tipoSeguimiento t " +
           "LEFT JOIN s.programa p " +
           "LEFT JOIN s.ambiente a " +
           "LEFT JOIN s.usuarioSistemaCrea u " +
           "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND s.empresa.idEmpresa = :idEmpresa " +
           "AND s.removido = false " +
           "AND (" +
           "  :filtro = '' OR " +
           "  LOWER(COALESCE(s.institucionVisitada, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.personaEntrevistada, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.direccion, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.medioVerificacion, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.resultadoSeguimiento, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.sugerenciasRecomendaciones, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(t.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(p.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(a.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, ''))) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
           ")")
    Page<SeguimientoEducativoLaboralOtros> buscarPorFiltro(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("filtro") String filtro,
        Pageable pageable
    );
}
