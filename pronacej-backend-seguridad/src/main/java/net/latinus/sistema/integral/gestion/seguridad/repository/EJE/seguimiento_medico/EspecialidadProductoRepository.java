package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.EspecialidadProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EspecialidadProductoRepository extends JpaRepository<EspecialidadProducto, Long> {
    EspecialidadProducto findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("""
        SELECT m FROM EspecialidadProducto m
        WHERE m.removido = false
            AND (
                (:valor IS NULL OR trim(:valor) = "") OR
                LOWER(m.especialidad) LIKE LOWER(CONCAT('%', :valor, '%')) OR
                LOWER(m.producto) LIKE LOWER(CONCAT('%', :valor, '%'))
            )
        ORDER BY m.especialidad
        LIMIT 100
        """)
    List<EspecialidadProducto> obtenerEspecialidadProductosBusqueda(@Param("valor") String valor);
}
