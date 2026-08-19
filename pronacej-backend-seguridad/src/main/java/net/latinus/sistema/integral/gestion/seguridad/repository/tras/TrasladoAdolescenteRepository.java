package net.latinus.sistema.integral.gestion.seguridad.repository.tras;

import net.latinus.sistema.integral.gestion.seguridad.entities.tras.TrasladoAdolescente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrasladoAdolescenteRepository extends JpaRepository<TrasladoAdolescente, Long> {

    Optional<TrasladoAdolescente> findByIdTrasladoAdolescenteAndRemovidoFalse(Long idTrasladoAdolescente);

    @Query("SELECT ta FROM TrasladoAdolescente ta " +
            "JOIN ta.traslado t " +
            "JOIN ta.fichaIdentificacion f " +
            "WHERE t.tokenIdentificador = :tokenTraslado " +
            "AND f.tokenIdentificador = :tokenFichaIdentificacion")
    Optional<TrasladoAdolescente> findByTrasladoTokenAndFichaIdentificacionToken(
            @Param("tokenTraslado") String tokenTraslado,
            @Param("tokenFichaIdentificacion") String tokenFichaIdentificacion);

    List<TrasladoAdolescente> findByTrasladoTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("""
            SELECT ta FROM TrasladoAdolescente ta
            JOIN ta.traslado t
            JOIN ta.fichaIdentificacion f
            JOIN ta.estadoEvento ee
            WHERE ta.completado = true
            AND ta.isComplete = false
            AND t.removido = false
            AND f.removido = false
            AND ta.removido = false
            AND ee.nemonico = 'ESTADO_SALIDA_ACTIVO'
            AND f.tokenIdentificador = :tokenIdentificador
            ORDER BY ta.idTrasladoAdolescente DESC
            """)
    Optional<TrasladoAdolescente> obtenerTrasladoActivo(
            @Param("tokenIdentificador") String tokenIdentificador
    );

}
