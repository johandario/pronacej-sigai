package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndSeguiCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanTratamientoIndSeguiCarpetaRepository extends JpaRepository<PlanTratamientoIndSeguiCarpeta, Long> {
    Page<PlanTratamientoIndSeguiCarpeta> findByPlanTratamientoIndSeguiTokenIdentificadorAndRemovido(String tokenIdentificadorFichaSeguimiento, Boolean removido, Pageable pageable);

    PlanTratamientoIndSeguiCarpeta findFirstByPlanTratamientoIndSeguiTokenIdentificadorAndRemovido(String tokenIdentificadorFichaSeguimiento, Boolean removido);
}
