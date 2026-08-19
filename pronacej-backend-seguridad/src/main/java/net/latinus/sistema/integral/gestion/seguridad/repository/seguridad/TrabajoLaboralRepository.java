package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.TrabajoLaboral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrabajoLaboralRepository extends JpaRepository<TrabajoLaboral, Long> {

    List<TrabajoLaboral> findByFichaIdentificacionTokenIdentificadorAndRemovido(
            String tokenIdentificador,
            Boolean removido
    );

    TrabajoLaboral findByTokenIdentificadorAndRemovido(
            String tokenIdentificador,
            Boolean removido
    );

    @Query("""
    SELECT COUNT(DISTINCT t.fichaIdentificacion.idFichaIdentificacion)
    FROM TrabajoLaboral t
    WHERE t.removido = false
    AND t.fichaIdentificacion.removido = false
    AND (:tokenCentro IS NULL OR t.fichaIdentificacion.centroIngreso.tokenIdentificador = :tokenCentro)
        """)
    Long countAdolescentesConTrabajoActivo(@Param("tokenCentro") String tokenCentro);

    @Query("""
        SELECT 
            ri.nombreOrganizacion,
            ri.ruc,
            COUNT(DISTINCT t.fichaIdentificacion.idFichaIdentificacion)
        FROM TrabajoLaboral t
        INNER JOIN t.registroInstitucion ri
        WHERE t.removido = false
        AND t.fichaIdentificacion.removido = false
        AND (:tokenCentro IS NULL OR t.fichaIdentificacion.centroIngreso.tokenIdentificador = :tokenCentro)
        GROUP BY ri.nombreOrganizacion, ri.ruc
        ORDER BY COUNT(DISTINCT t.fichaIdentificacion.idFichaIdentificacion) DESC
    """)
    List<Object[]> countTrabajoLaboralPorInstitucion(@Param("tokenCentro") String tokenCentro);

}