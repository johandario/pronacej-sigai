package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.Estudios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EstudiosRepository extends JpaRepository<Estudios, Long> {
    List<Estudios> findByFichaIdentificacionTokenIdentificadorAndRemovido(
            String tokenIdentificador,
            Boolean removido

    );

    Estudios findByTokenIdentificadorAndRemovido(
            String tokenIdentificador,
            Boolean removido
    );


    @Query("""
        SELECT 
            COALESCE(ri.nombreOrganizacion, 'Sin institución'),
            COALESCE(ri.ruc, 'Sin RUC'),
            COUNT(e)
        FROM Estudios e
        LEFT JOIN e.registroInstitucion ri
        WHERE e.removido = false
        AND (:tokenCentro IS NULL OR e.fichaIdentificacion.centroIngreso.tokenIdentificador = :tokenCentro)
        GROUP BY ri.nombreOrganizacion, ri.ruc
        ORDER BY COUNT(e) DESC
    """)
    List<Object[]> countEstudiosPorInstitucion(
            @Param("tokenCentro") String tokenCentro
    );


    @Query("""
        SELECT COUNT(DISTINCT e.fichaIdentificacion.idFichaIdentificacion)
        FROM Estudios e
        WHERE e.removido = false
        AND (:tokenCentro IS NULL OR e.fichaIdentificacion.centroIngreso.tokenIdentificador = :tokenCentro)
    """)
    Long countUsuariosEstudiando(
            @Param("tokenCentro") String tokenCentro
    );



    @Query("""
    SELECT 
        COUNT(CASE WHEN e.convenioPronacej = true THEN 1 END),
        COUNT(e)
    FROM Estudios e
    WHERE e.removido = false
    AND (:tokenCentro IS NULL OR e.fichaIdentificacion.centroIngreso.tokenIdentificador = :tokenCentro)
""")
    List<Object[]> porcentajeConvenioPronacej(
            @Param("tokenCentro") String tokenCentro
    );

}