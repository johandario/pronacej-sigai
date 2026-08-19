package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.DireccionPersonaReferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DireccionPersonaReferenciaRepository extends JpaRepository<DireccionPersonaReferencia, Long> {

    @Query("SELECT o FROM DireccionPersonaReferencia o WHERE o.idPersonasRelacionadas.idPersonasRelacionadas = :idPersonaRelacionada and o.removido = false")
    List<DireccionPersonaReferencia> encontrarDireccionesPersonaRelacionada(@Param("idPersonaRelacionada") Long idPersonaRelacionada);

    DireccionPersonaReferencia findByidDireccionPersonaReferenciaAndRemovido(Long idDireccionPersonaReferencia, Boolean removido);
}
