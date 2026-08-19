package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ActividadIntervencion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadIntervencionRepository extends JpaRepository<ActividadIntervencion, Long> {

    Page<ActividadIntervencion> findByPlanTratamientoIndIntervIdPlanTratIndIntervAndRemovido(Long idPlanTratIndInterv,
                                                                                             Boolean removido, Pageable pageable);

}
