package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndSeguiAbiertoCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanTratamientoIndSeguiAbiertoCarpetaRepository extends JpaRepository<PlanTratamientoIndSeguiAbiertoCarpeta, Long> {
    Page<PlanTratamientoIndSeguiAbiertoCarpeta> findByPlanTratamientoIndSeguiAbiertoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaSeguimientoAbierto, Boolean removido, Pageable pageable);

    PlanTratamientoIndSeguiAbiertoCarpeta findFirstByPlanTratamientoIndSeguiAbiertoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaSeguimientoAbierto, Boolean removido);
}
