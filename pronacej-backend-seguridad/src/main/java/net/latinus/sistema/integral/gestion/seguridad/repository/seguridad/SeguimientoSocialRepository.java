package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoSocial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeguimientoSocialRepository extends JpaRepository<SeguimientoSocial, Long> {
   
   List<SeguimientoSocial> findByRemovido(boolean removido);
   
   SeguimientoSocial findByIdSeguimientoSocial(Long idSeguimientoSocial);
   
   SeguimientoSocial findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
   
   Page<SeguimientoSocial> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
   
   Page<SeguimientoSocial> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
       String tokenIdentificadorFichaIdentificacion, 
       Long idEmpresa, 
       Boolean removido, 
       Pageable pageable);
   
   /**
    * Búsqueda con filtrado de texto para seguimientos sociales
    * El filtro se aplica a varios campos de texto relevantes
    */
   @Query("SELECT s FROM SeguimientoSocial s " +
          "LEFT JOIN s.tipoActividadSocial t " +
          "LEFT JOIN s.programa p " +
          "LEFT JOIN s.ambiente a " +
          "LEFT JOIN s.usuarioSistemaCrea u " +
          "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
          "AND s.empresa.idEmpresa = :idEmpresa " +
          "AND s.removido = false " +
          "AND (" +
          "  :filtro = '' OR " +
          "  LOWER(COALESCE(s.descripcionSocial, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(s.accionesAdoptadas, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(s.comentarios, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(t.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(p.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(a.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, ''))) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
          ")")
   Page<SeguimientoSocial> buscarPorFiltro(
       @Param("tokenIdentificador") String tokenIdentificador,
       @Param("idEmpresa") Long idEmpresa,
       @Param("filtro") String filtro,
       Pageable pageable
   );

   /**
    * Búsqueda ordenada por nombreCompletoUsuarioCreacion ASC
    */
   @Query("SELECT s FROM SeguimientoSocial s " +
          "LEFT JOIN s.usuarioSistemaCrea u " +
          "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
          "AND s.empresa.idEmpresa = :idEmpresa " +
          "AND s.removido = false " +
          "ORDER BY " +
          "CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, '')) ASC")
   Page<SeguimientoSocial> buscarOrdenadoPorUsuarioCreacionAsc(
       @Param("tokenIdentificador") String tokenIdentificador,
       @Param("idEmpresa") Long idEmpresa,
       Pageable pageable
   );

   /**
    * Búsqueda ordenada por nombreCompletoUsuarioCreacion DESC
    */
   @Query("SELECT s FROM SeguimientoSocial s " +
          "LEFT JOIN s.usuarioSistemaCrea u " +
          "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
          "AND s.empresa.idEmpresa = :idEmpresa " +
          "AND s.removido = false " +
          "ORDER BY " +
          "CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, '')) DESC")
   Page<SeguimientoSocial> buscarOrdenadoPorUsuarioCreacionDesc(
       @Param("tokenIdentificador") String tokenIdentificador,
       @Param("idEmpresa") Long idEmpresa,
       Pageable pageable
   );

   /**
    * Búsqueda con filtro y ordenamiento por nombreCompletoUsuarioCreacion ASC
    */
   @Query("SELECT s FROM SeguimientoSocial s " +
          "LEFT JOIN s.tipoActividadSocial t " +
          "LEFT JOIN s.programa p " +
          "LEFT JOIN s.ambiente a " +
          "LEFT JOIN s.usuarioSistemaCrea u " +
          "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
          "AND s.empresa.idEmpresa = :idEmpresa " +
          "AND s.removido = false " +
          "AND (" +
          "  :filtro = '' OR " +
          "  LOWER(COALESCE(s.descripcionSocial, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(s.accionesAdoptadas, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(s.comentarios, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(t.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(p.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(a.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, ''))) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
          ") " +
          "ORDER BY " +
          "CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, '')) ASC")
   Page<SeguimientoSocial> buscarConFiltroOrdenadoPorUsuarioCreacionAsc(
       @Param("tokenIdentificador") String tokenIdentificador,
       @Param("idEmpresa") Long idEmpresa,
       @Param("filtro") String filtro,
       Pageable pageable
   );

   /**
    * Búsqueda con filtro y ordenamiento por nombreCompletoUsuarioCreacion DESC
    */
   @Query("SELECT s FROM SeguimientoSocial s " +
          "LEFT JOIN s.tipoActividadSocial t " +
          "LEFT JOIN s.programa p " +
          "LEFT JOIN s.ambiente a " +
          "LEFT JOIN s.usuarioSistemaCrea u " +
          "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
          "AND s.empresa.idEmpresa = :idEmpresa " +
          "AND s.removido = false " +
          "AND (" +
          "  :filtro = '' OR " +
          "  LOWER(COALESCE(s.descripcionSocial, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(s.accionesAdoptadas, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(s.comentarios, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(t.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(p.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(COALESCE(a.nombre, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
          "  LOWER(CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, ''))) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
          ") " +
          "ORDER BY " +
          "CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, '')) DESC")
   Page<SeguimientoSocial> buscarConFiltroOrdenadoPorUsuarioCreacionDesc(
       @Param("tokenIdentificador") String tokenIdentificador,
       @Param("idEmpresa") Long idEmpresa,
       @Param("filtro") String filtro,
       Pageable pageable
   );

   /**
    * Búsqueda ordenada por programa.nombre ASC
    */
   @Query("SELECT s FROM SeguimientoSocial s " +
          "LEFT JOIN s.programa p " +
          "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
          "AND s.empresa.idEmpresa = :idEmpresa " +
          "AND s.removido = false " +
          "ORDER BY COALESCE(p.nombre, 'ZZZZ') ASC")
   Page<SeguimientoSocial> buscarOrdenadoPorProgramaAsc(
       @Param("tokenIdentificador") String tokenIdentificador,
       @Param("idEmpresa") Long idEmpresa,
       Pageable pageable
   );

   /**
    * Búsqueda ordenada por programa.nombre DESC
    */
   @Query("SELECT s FROM SeguimientoSocial s " +
          "LEFT JOIN s.programa p " +
          "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
          "AND s.empresa.idEmpresa = :idEmpresa " +
          "AND s.removido = false " +
          "ORDER BY COALESCE(p.nombre, '') DESC")
   Page<SeguimientoSocial> buscarOrdenadoPorProgramaDesc(
       @Param("tokenIdentificador") String tokenIdentificador,
       @Param("idEmpresa") Long idEmpresa,
       Pageable pageable
   );

   /**
    * Búsqueda ordenada por ambiente.nombre ASC
    */
   @Query("SELECT s FROM SeguimientoSocial s " +
          "LEFT JOIN s.ambiente a " +
          "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
          "AND s.empresa.idEmpresa = :idEmpresa " +
          "AND s.removido = false " +
          "ORDER BY COALESCE(a.nombre, 'ZZZZ') ASC")
   Page<SeguimientoSocial> buscarOrdenadoPorAmbienteAsc(
       @Param("tokenIdentificador") String tokenIdentificador,
       @Param("idEmpresa") Long idEmpresa,
       Pageable pageable
   );

   /**
    * Búsqueda ordenada por ambiente.nombre DESC
    */
   @Query("SELECT s FROM SeguimientoSocial s " +
          "LEFT JOIN s.ambiente a " +
          "WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
          "AND s.empresa.idEmpresa = :idEmpresa " +
          "AND s.removido = false " +
          "ORDER BY COALESCE(a.nombre, '') DESC")
   Page<SeguimientoSocial> buscarOrdenadoPorAmbienteDesc(
       @Param("tokenIdentificador") String tokenIdentificador,
       @Param("idEmpresa") Long idEmpresa,
       Pageable pageable
   );
}
