package net.latinus.sistema.integral.gestion.seguridad.repository.tras;

import net.latinus.sistema.integral.gestion.seguridad.entities.tras.Traslado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrasladoRepository extends JpaRepository<Traslado, Long> {
    Page<Traslado> findByRemovido(Boolean removido, Pageable pageable);
    Traslado findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    Page<Traslado> findByTrasladoAdolescentesFichaIdentificacionIdFichaIdentificacionAndRemovido(Long idFichaIdentificacion, Boolean removido, Pageable pageable);
    Traslado findFirstByOrderByIdTrasladoDesc();
    List<Traslado> findByTrasladoAdolescentesFichaIdentificacionIdFichaIdentificacionAndRemovido(Long idFichaIdentificacion, Boolean removido);

    Page<Traslado> findByTrasladoAdolescentesFichaIdentificacionTokenIdentificadorAndRemovido(String idFichaIdentificacion, Boolean removido, Pageable pageable);

    @Query("SELECT t FROM Traslado t " +
            "JOIN t.trasladoAdolescentes ta " +
            "JOIN ta.estadoEvento ee " +
            "WHERE ta.fichaIdentificacion.tokenIdentificador = :idFichaIdentificacion " +
            "AND t.removido = :removido " +
            "AND (:nemonicoEstado IS NULL OR ee.nemonico = :nemonicoEstado)")
    Page<Traslado> buscarTrasladosPorFichaYEstadoEvento(
            @Param("idFichaIdentificacion") String idFichaIdentificacion,
            @Param("removido") Boolean removido,
            @Param("nemonicoEstado") String nemonicoEstado,
            Pageable pageable
    );

    Optional<Traslado> findByTrasladoAdolescentesIdTrasladoAdolescente(Long idTrasladoAdolescente);
}
