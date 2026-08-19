package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ExpedienteMatrizDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpedienteMatrizDetalleRepository extends JpaRepository<ExpedienteMatrizDetalle, Long> {
    List<ExpedienteMatrizDetalle> findByExpedienteMatrizTokenIdentificadorAndRemovido(String tokenExpediente, Boolean removido);

    ExpedienteMatrizDetalle findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("SELECT emd FROM ExpedienteMatrizDetalle emd " +
            "JOIN emd.expedienteMatriz em " +
            "JOIN em.fichaIdentificacion fi " +
            "WHERE fi.tokenIdentificador = :tokenIdentificador " +
            "ORDER BY emd.fechaFinMedida DESC")
    List<ExpedienteMatrizDetalle> findExpedienteMatrizDetalleByFichaIdentificacion(
            @Param("tokenIdentificador") String tokenIdentificador
    );

    @Query("SELECT emd FROM ExpedienteMatrizDetalle emd " +
            "JOIN emd.expedienteMatriz em " +
            "JOIN em.fichaIdentificacion fi " +
            "WHERE fi.tokenIdentificador = :tokenIdentificador " +
            "AND em.removido = :removido " +
            "ORDER BY emd.fechaFinMedida DESC")
    List<ExpedienteMatrizDetalle> findExpedienteMatrizDetalleByFichaIdentificacionAndExpedienteRemovido(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("removido") Boolean removido
    );

    ExpedienteMatrizDetalle findFirstByExpedienteMatrizTokenIdentificadorAndRemovidoOrderByFechaCreacionDesc(String tokenExpediente, Boolean removido);

}
