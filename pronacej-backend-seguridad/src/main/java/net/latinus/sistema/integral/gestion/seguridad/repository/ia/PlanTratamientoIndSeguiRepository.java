package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoInd;
import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndSegui;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanTratamientoIndSeguiRepository extends JpaRepository<PlanTratamientoIndSegui, Long> {
    Page<PlanTratamientoIndSegui> findByRemovido(Boolean removido, Pageable pageable);

    List<PlanTratamientoIndSegui> findByIdPlanTratamientoIndSeguiAndRemovido(Long id, Boolean removido);

    PlanTratamientoIndSegui findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    //Page<PlanTratamientoIndSegui> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido, Pageable pageable);

    List<PlanTratamientoIndSegui> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido);


}
