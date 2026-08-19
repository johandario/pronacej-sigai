package net.latinus.sistema.integral.gestion.seguridad.repository.institucion;

import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistroInstitucionRepository extends JpaRepository<RegistroInstitucion, Long>{

    Page<RegistroInstitucion> findByRemovido(Boolean removido, Pageable pageable);
    RegistroInstitucion findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    List<RegistroInstitucion> findAllByRemovido(Boolean removido);


    @Query("SELECT r FROM RegistroInstitucion r LEFT JOIN r.centro c " +
            "WHERE r.removido = false AND (c IS NULL OR c.tokenIdentificador = :tokenIdentificador) AND (" +
            "    LOWER(r.nombreOrganizacion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "    OR LOWER(r.nombreDirector) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "    OR LOWER(r.finalidadInstitucion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "    OR LOWER(r.direccion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "    OR LOWER(r.ruc) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "    OR LOWER(r.estado) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            ")")
    Page<RegistroInstitucion> buscarPorFiltroYCentro(
            @Param("filter") String filter,
            @Param("tokenIdentificador") String tokenIdentificador,
            Pageable pageable
    );


    @Query("SELECT r FROM RegistroInstitucion r LEFT JOIN r.centro c " +
            "WHERE r.removido = false AND (c IS NULL OR c.tokenIdentificador = :tokenIdentificador)")
    Page<RegistroInstitucion> findByCentroTokenIdentificador(
            @Param("tokenIdentificador") String tokenIdentificador,
            Pageable pageable
    );

    @Query("SELECT r FROM RegistroInstitucion r " +
            "WHERE r.removido = false " +
            "AND (" +
            "LOWER(r.nombreOrganizacion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.nombreDirector) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.finalidadInstitucion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.direccion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.ruc) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.estado) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            ")")
    Page<RegistroInstitucion> buscarPorFiltro(
            @Param("filter") String filter,
            Pageable pageable
    );

    RegistroInstitucion findByRucAndRemovido(
            String ruc,
            Boolean removido
    );





}
