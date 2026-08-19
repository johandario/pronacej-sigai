package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.ContactoAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.entities.PlanAsistenciaPostEgreso;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContactoAdolescenteRepository extends JpaRepository<ContactoAdolescente, Long>{
    Page<ContactoAdolescente> findByRemovido(Boolean removido, Pageable pageable);
    ContactoAdolescente findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    List<ContactoAdolescente> findAllByRemovido(Boolean removido);
    @Query("SELECT r FROM ContactoAdolescente r WHERE r.tokenFichaIdentificacion.tokenIdentificador = :tokenIdentificador AND r.removido = :removido")
    List<ContactoAdolescente> findAllByFichaIdentificacionTokenIdentificadorAndRemovido(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("removido") Boolean removido
    );
    @Query("SELECT r FROM ContactoAdolescente r WHERE r.tokenFichaIdentificacion.tokenIdentificador = :tokenIdentificador AND r.removido = :removido")
    Page<ContactoAdolescente> findAllByFichaIdentificacionTokenIdentificadorAndRemovido(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("removido") Boolean removido,
            Pageable pageable
    );

    @Query("SELECT r FROM ContactoAdolescente r " +
            "WHERE r.tokenFichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND r.removido = false " +
            "AND (" +
            "LOWER(r.usuarioResponsable) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.modalidadEntrevista) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.actividades) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.observaciones) LIKE LOWER(CONCAT('%', :filter, '%'))" +
            ")")
    Page<ContactoAdolescente> buscarPorTokenYFiltro(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("filter") String filter,
            Pageable pageable
    );

    @Query("SELECT r FROM ContactoAdolescente r " +
            "WHERE r.removido = false " +
            "AND (" +
            "LOWER(r.usuarioResponsable) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.modalidadEntrevista) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.actividades) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.observaciones) LIKE LOWER(CONCAT('%', :filter, '%'))" +
            ")")
    Page<ContactoAdolescente> buscarPorFiltro(
            @Param("filter") String filter,
            Pageable pageable
    );


}
