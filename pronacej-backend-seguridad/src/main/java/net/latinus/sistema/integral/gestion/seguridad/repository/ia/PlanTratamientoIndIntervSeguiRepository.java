package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndIntervSegui;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanTratamientoIndIntervSeguiRepository extends JpaRepository<PlanTratamientoIndIntervSegui, Long> {
    Page<PlanTratamientoIndIntervSegui> findByActividadTokenIdentificadorAndRemovido(String tokenIdentificadorActividad, Boolean removido, Pageable pageable);

    Page<PlanTratamientoIndIntervSegui> findByActividadPlanTratamientoIndDiferenciadaTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);

    List<PlanTratamientoIndIntervSegui> findByIdPlanTratamientoIndIntervSeguiAndRemovido(Long idPlanTratamientoIndIntervSegui, Boolean removido);


    List<PlanTratamientoIndIntervSegui> findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

}
