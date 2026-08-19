package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    Medicamento findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("""
        SELECT m FROM Medicamento m
        WHERE m.removido = false
            AND (
                (:valor IS NULL OR trim(:valor) = "") OR
                LOWER(m.nombre) LIKE LOWER(CONCAT('%', :valor, '%')) OR
                LOWER(m.concentracion) LIKE LOWER(CONCAT('%', :valor, '%')) OR
                LOWER(m.presentacion) LIKE LOWER(CONCAT('%', :valor, '%'))
            )
        ORDER BY m.nombre
        LIMIT 100
        """)
    List<Medicamento> obtenerMedicamentosBusqueda(@Param("valor") String valor);
}
