package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.SituacionRiesgoSocial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SituacionRiesgoSocialRepository extends JpaRepository<SituacionRiesgoSocial, Long> {
    
    SituacionRiesgoSocial findByIdSituacionRiesgoSocial(Long idSituacionRiesgoSocial);
        
    SituacionRiesgoSocial findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<SituacionRiesgoSocial> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<SituacionRiesgoSocial> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
        String tokenIdentificadorFichaIdentificacion, 
        Long idEmpresa, 
        Boolean removido, 
        Pageable pageable
    );
    
    /**
     * Búsqueda con filtrado de texto para situaciones de riesgo social
     * El filtro se aplica a varios campos de texto relevantes
     */
    @Query("SELECT s FROM SituacionRiesgoSocial s " +
           "JOIN s.usuarioSistemaCrea u " +
           "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND s.empresa.idEmpresa = :idEmpresa " +
           "AND s.removido = false " +
           "AND (" +
           "  :filtro = '' OR " +
           "  LOWER(CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, ''))) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.anteDeliFami, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.primManiInfrAdol, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.estadoSaludGeneral, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.problemasLegales, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.observaciones, '')) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
           ")")
    Page<SituacionRiesgoSocial> buscarPorFiltro(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("filtro") String filtro,
        Pageable pageable
    );

    /**
     * Búsqueda ordenada por usuarioRegistro ASC
     */
    @Query("SELECT s FROM SituacionRiesgoSocial s " +
           "JOIN s.usuarioSistemaCrea u " +
           "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND s.empresa.idEmpresa = :idEmpresa " +
           "AND s.removido = false " +
           "ORDER BY " +
           "CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, '')) ASC")
    Page<SituacionRiesgoSocial> buscarOrdenadoPorUsuarioRegistroAsc(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        Pageable pageable
    );

    /**
     * Búsqueda ordenada por usuarioRegistro DESC
     */
    @Query("SELECT s FROM SituacionRiesgoSocial s " +
           "JOIN s.usuarioSistemaCrea u " +
           "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND s.empresa.idEmpresa = :idEmpresa " +
           "AND s.removido = false " +
           "ORDER BY " +
           "CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, '')) DESC")
    Page<SituacionRiesgoSocial> buscarOrdenadoPorUsuarioRegistroDesc(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        Pageable pageable
    );

    /**
     * Búsqueda con filtro y ordenamiento por usuarioRegistro ASC
     */
    @Query("SELECT s FROM SituacionRiesgoSocial s " +
           "JOIN s.usuarioSistemaCrea u " +
           "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND s.empresa.idEmpresa = :idEmpresa " +
           "AND s.removido = false " +
           "AND (" +
           "  :filtro = '' OR " +
           "  LOWER(CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, ''))) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.anteDeliFami, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.primManiInfrAdol, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.estadoSaludGeneral, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.problemasLegales, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.observaciones, '')) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
           ") " +
           "ORDER BY " +
           "CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, '')) ASC")
    Page<SituacionRiesgoSocial> buscarConFiltroOrdenadoPorUsuarioRegistroAsc(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("filtro") String filtro,
        Pageable pageable
    );

    /**
     * Búsqueda con filtro y ordenamiento por usuarioRegistro DESC
     */
    @Query("SELECT s FROM SituacionRiesgoSocial s " +
           "JOIN s.usuarioSistemaCrea u " +
           "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND s.empresa.idEmpresa = :idEmpresa " +
           "AND s.removido = false " +
           "AND (" +
           "  :filtro = '' OR " +
           "  LOWER(CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, ''))) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.anteDeliFami, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.primManiInfrAdol, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.estadoSaludGeneral, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.problemasLegales, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(s.observaciones, '')) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
           ") " +
           "ORDER BY " +
           "CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, '')) DESC")
    Page<SituacionRiesgoSocial> buscarConFiltroOrdenadoPorUsuarioRegistroDesc(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("filtro") String filtro,
        Pageable pageable
    );
}