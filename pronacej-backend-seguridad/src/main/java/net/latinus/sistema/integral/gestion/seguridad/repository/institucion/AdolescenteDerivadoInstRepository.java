package net.latinus.sistema.integral.gestion.seguridad.repository.institucion;

import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.AdolescenteDerivadoInst;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdolescenteDerivadoInstRepository extends JpaRepository<AdolescenteDerivadoInst, Long>{
    Page<AdolescenteDerivadoInst> findByRemovido(Boolean removido, Pageable pageable);
    AdolescenteDerivadoInst findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    List<AdolescenteDerivadoInst> findAllByRemovido(Boolean removido);
    @Query("SELECT r FROM AdolescenteDerivadoInst r " +
            "WHERE r.tokenFichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND r.removido = :removido")
    Page<AdolescenteDerivadoInst> findAllByFichaIdentificacionTokenIdentificadorAndRemovido(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("removido") Boolean removido,
            Pageable pageable
    );

    @Query("SELECT r FROM AdolescenteDerivadoInst r " +
            "WHERE r.tokenFichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND r.removido = false " +
            "AND (" +
            "LOWER(r.departamento) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.personaResponsable) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.servicio) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.estado) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.tiempoServicio) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.institucion.nombreOrganizacion) LIKE LOWER(CONCAT('%', :filter, '%'))" +
            ")")
    Page<AdolescenteDerivadoInst> buscarPorTokenYFiltro(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("filter") String filter,
            Pageable pageable
    );

    @Query("SELECT r FROM AdolescenteDerivadoInst r " +
            "WHERE r.removido = false " +
            "AND (" +
            "LOWER(r.departamento) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.personaResponsable) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.servicio) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            ")")
    Page<AdolescenteDerivadoInst> buscarPorFiltro(
            @Param("filter") String filter,
            Pageable pageable
    );
}
