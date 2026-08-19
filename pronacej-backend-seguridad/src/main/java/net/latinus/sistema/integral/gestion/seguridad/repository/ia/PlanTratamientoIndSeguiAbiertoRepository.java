package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndSeguiAbierto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanTratamientoIndSeguiAbiertoRepository extends JpaRepository<PlanTratamientoIndSeguiAbierto, Long> {
    List<PlanTratamientoIndSeguiAbierto> findByPlanTratamientoIndIntervTokenIdentificadorAndRemovido(String tokenPtiInterv, Boolean removido);

    PlanTratamientoIndSeguiAbierto findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
