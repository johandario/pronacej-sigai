package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanTratamientoIndCarpetaRepository extends JpaRepository<PlanTratamientoIndCarpeta, Long> {
    Page<PlanTratamientoIndCarpeta> findByPlanTratamientoIndTokenIdentificadorAndRemovido(String tokenIdentificadorPlan, Boolean removido, Pageable pageable);

    PlanTratamientoIndCarpeta findFirstByPlanTratamientoIndTokenIdentificadorAndRemovido(String tokenIdentificadorPlan, Boolean removido);
}
