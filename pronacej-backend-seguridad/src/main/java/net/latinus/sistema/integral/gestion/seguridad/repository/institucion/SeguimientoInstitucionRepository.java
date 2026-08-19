package net.latinus.sistema.integral.gestion.seguridad.repository.institucion;

import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.SeguimientoInstitucion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguimientoInstitucionRepository extends JpaRepository<SeguimientoInstitucion, Long> {

    Page<SeguimientoInstitucion> findByRemovido(Boolean removido, Pageable pageable);

    SeguimientoInstitucion findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    // Buscar todos los seguimientos de una institución específica
    @Query("SELECT s FROM SeguimientoInstitucion s " +
            "WHERE s.registroInstitucion.tokenIdentificador = :tokenIdentificador " +
            "AND s.removido = false")
    Page<SeguimientoInstitucion> findAllByInstitucionTokenIdentificador(
            @Param("tokenIdentificador") String tokenIdentificador,
            Pageable pageable
    );

    // Buscar seguimientos de una institución aplicando filtro adicional
    @Query("SELECT s FROM SeguimientoInstitucion s " +
            "WHERE s.registroInstitucion.tokenIdentificador = :tokenIdentificador " +
            "AND s.removido = false " +
            "AND (" +
            "LOWER(s.numeroDoc) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.estado) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.personaResponsable) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR CAST(s.fechaRegistro AS string) LIKE CONCAT('%', :filter, '%')" +
            ")")
    Page<SeguimientoInstitucion> buscarPorInstitucionYFiltro(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("filter") String filter,
            Pageable pageable
    );

    // Búsqueda general con filtro sin importar institución
    @Query("SELECT s FROM SeguimientoInstitucion s " +
            "WHERE s.removido = false " +
            "AND (" +
            "LOWER(s.numeroDoc) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.estado) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.personaResponsable) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR CAST(s.fechaRegistro AS string) LIKE CONCAT('%', :filter, '%')" +
            ")")
    Page<SeguimientoInstitucion> buscarPorFiltro(@Param("filter") String filter, Pageable pageable);
}

