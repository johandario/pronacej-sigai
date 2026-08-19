package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndInterv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanTratamientoIndIntervRepository extends JpaRepository<PlanTratamientoIndInterv, Long> {
    PlanTratamientoIndInterv findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    PlanTratamientoIndInterv findByIdPlanTratIndIntervAndRemovido(Long idPlanTratamientoIndInterv, Boolean removido);

    Optional<PlanTratamientoIndInterv> findByIdPlanTratIndInterv(Long idPlanTratIndInterv);
}
