package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ExpedienteMatriz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ExpedienteMatrizRepository extends JpaRepository<ExpedienteMatriz, Long> {
    Page<ExpedienteMatriz> findByRemovido(Boolean removido, Pageable pageable);

    ExpedienteMatriz findByNumExpediente(String numExpediente);

    @Query("SELECT d FROM ExpedienteMatriz d WHERE d.numExpediente LIKE CONCAT(:anioPrefix, '%') ORDER BY d.numExpediente DESC LIMIT 1")
    ExpedienteMatriz findTopByAnio(@Param("anioPrefix") String anioPrefix);

    @Query("SELECT em FROM ExpedienteMatriz em " +
            "INNER JOIN em.fichaIdentificacion fi " +
            "WHERE fi.tokenIdentificador = :tokenIdentificador")
    Page<ExpedienteMatriz> findExpedientesByTokenFicha(@Param("tokenIdentificador") String tokenIdentificador, Pageable pageable);

/*
    Page<ExpedienteMatriz> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido, Pageable pageable);
*/

    List<ExpedienteMatriz> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido);


    Long countByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido);

    ExpedienteMatriz findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("SELECT e " +
            "FROM ExpedienteMatriz e " +
            "JOIN e.fichaIdentificacion f " +
            "WHERE e.removido = false " +
            "AND f.centroIngreso.tokenIdentificador = :centro " +
            "AND e.idExpediente = ( " +
            "    SELECT e2.idExpediente " +
            "    FROM ExpedienteMatriz e2 " +
            "    JOIN e2.expedienteDetalle d2 " +
            "    WHERE e2.fichaIdentificacion.idFichaIdentificacion = e.fichaIdentificacion.idFichaIdentificacion " +
            "    AND d2.fechaFinMedida = ( " +
            "        SELECT MAX(d3.fechaFinMedida) " +
            "        FROM ExpedienteMatrizDetalle d3 " +
            "        JOIN d3.expedienteMatriz e3 " +
            "        WHERE e3.fichaIdentificacion.idFichaIdentificacion = e2.fichaIdentificacion.idFichaIdentificacion " +
            "    ) " +
            "    ORDER BY d2.fechaFinMedida DESC " +
            "    LIMIT 1 " +
            ") " +
            "AND EXISTS ( " +
            "    SELECT 1 FROM ExpedienteMatrizDetalle d4 " +
            "    WHERE d4.expedienteMatriz = e " +
            "    AND d4.fechaFinMedida <= :fechaLimite " +
            ")")
    List<ExpedienteMatriz> findExpedientesConSentenciaPorCumplirse(@Param("fechaLimite") Date fechaLimite, @Param("centro") String tokenCentro);

    ExpedienteMatriz findFirstByFichaIdentificacionTokenIdentificadorAndRemovidoOrderByFechaCreacionDesc(String tokenFicha, Boolean removido);
}
