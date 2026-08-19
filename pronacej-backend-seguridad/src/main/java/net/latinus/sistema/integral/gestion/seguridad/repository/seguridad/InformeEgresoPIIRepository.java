package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.InformeEgresoPII;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InformeEgresoPIIRepository extends JpaRepository<InformeEgresoPII, Long> {
    
    List<InformeEgresoPII> findByRemovido(boolean removido);
    
    InformeEgresoPII findByIdInformeEgresoPII(Long idInformeEgresoPII);
    
    InformeEgresoPII findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<InformeEgresoPII> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<InformeEgresoPII> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
        String tokenIdentificadorFichaIdentificacion, Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<InformeEgresoPII> findByInformeSeguimientoTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
        String tokenIdentificadorInformeSeguimiento, Long idEmpresa, Boolean removido, Pageable pageable);
    
    /**
     * Busca informes de egreso con filtrado por texto
     * Método simplificado que solo maneja el filtrado, el ordenamiento se maneja en el service
     *
     * @param tokenIdentificador String token identificador de la ficha de identificación
     * @param idEmpresa Long id de la empresa
     * @param filtro String texto para filtrar
     * @param removido boolean indicador de si fue removido
     * @param pageable Pageable objeto para la paginación y ordenamiento
     * @return Page<InformeEgresoPII>
     */
    @Query("SELECT ie FROM InformeEgresoPII ie " +
       "JOIN ie.usuarioSistemaCrea usc " +
       "WHERE ie.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
       "AND ie.empresa.idEmpresa = :idEmpresa " +
       "AND ie.removido = :removido " +
       "AND (" +
       "    :filtro = '' OR " +
       "    LOWER(COALESCE(ie.motivoIngresoPII, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
       "    LOWER(COALESCE(ie.conclusiones, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
       "    LOWER(COALESCE(ie.recomendaciones, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
       "    LOWER(CONCAT(COALESCE(usc.nombres, ''), ' ', COALESCE(usc.apellidos, ''))) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
       ")")
    Page<InformeEgresoPII> buscarPorFiltro(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("filtro") String filtro,
        @Param("removido") Boolean removido,
        Pageable pageable
    );

    /**
     * Búsqueda ordenada por nombreCompletoUsuarioCreacion ASC
     */
    @Query("SELECT ie FROM InformeEgresoPII ie " +
           "JOIN ie.usuarioSistemaCrea usc " +
           "WHERE ie.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND ie.empresa.idEmpresa = :idEmpresa " +
           "AND ie.removido = :removido " +
           "ORDER BY " +
           "CONCAT(COALESCE(usc.nombres, ''), ' ', COALESCE(usc.apellidos, '')) ASC")
    Page<InformeEgresoPII> buscarOrdenadoPorUsuarioCreacionAsc(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("removido") Boolean removido,
        Pageable pageable
    );

    /**
     * Búsqueda ordenada por nombreCompletoUsuarioCreacion DESC
     */
    @Query("SELECT ie FROM InformeEgresoPII ie " +
           "JOIN ie.usuarioSistemaCrea usc " +
           "WHERE ie.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND ie.empresa.idEmpresa = :idEmpresa " +
           "AND ie.removido = :removido " +
           "ORDER BY " +
           "CONCAT(COALESCE(usc.nombres, ''), ' ', COALESCE(usc.apellidos, '')) DESC")
    Page<InformeEgresoPII> buscarOrdenadoPorUsuarioCreacionDesc(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("removido") Boolean removido,
        Pageable pageable
    );

    /**
     * Búsqueda con filtro y ordenamiento por nombreCompletoUsuarioCreacion ASC
     */
    @Query("SELECT ie FROM InformeEgresoPII ie " +
           "JOIN ie.usuarioSistemaCrea usc " +
           "WHERE ie.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND ie.empresa.idEmpresa = :idEmpresa " +
           "AND ie.removido = :removido " +
           "AND (" +
           "    :filtro = '' OR " +
           "    LOWER(COALESCE(ie.motivoIngresoPII, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "    LOWER(COALESCE(ie.conclusiones, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "    LOWER(COALESCE(ie.recomendaciones, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "    LOWER(CONCAT(COALESCE(usc.nombres, ''), ' ', COALESCE(usc.apellidos, ''))) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
           ") " +
           "ORDER BY " +
           "CONCAT(COALESCE(usc.nombres, ''), ' ', COALESCE(usc.apellidos, '')) ASC")
    Page<InformeEgresoPII> buscarConFiltroOrdenadoPorUsuarioCreacionAsc(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("filtro") String filtro,
        @Param("removido") Boolean removido,
        Pageable pageable
    );

    /**
     * Búsqueda con filtro y ordenamiento por nombreCompletoUsuarioCreacion DESC
     */
    @Query("SELECT ie FROM InformeEgresoPII ie " +
           "JOIN ie.usuarioSistemaCrea usc " +
           "WHERE ie.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND ie.empresa.idEmpresa = :idEmpresa " +
           "AND ie.removido = :removido " +
           "AND (" +
           "    :filtro = '' OR " +
           "    LOWER(COALESCE(ie.motivoIngresoPII, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "    LOWER(COALESCE(ie.conclusiones, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "    LOWER(COALESCE(ie.recomendaciones, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "    LOWER(CONCAT(COALESCE(usc.nombres, ''), ' ', COALESCE(usc.apellidos, ''))) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
           ") " +
           "ORDER BY " +
           "CONCAT(COALESCE(usc.nombres, ''), ' ', COALESCE(usc.apellidos, '')) DESC")
    Page<InformeEgresoPII> buscarConFiltroOrdenadoPorUsuarioCreacionDesc(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("filtro") String filtro,
        @Param("removido") Boolean removido,
        Pageable pageable
    );
}