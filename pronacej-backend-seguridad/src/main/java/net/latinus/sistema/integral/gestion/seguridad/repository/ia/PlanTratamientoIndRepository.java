package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoInd;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanTratamientoIndRepository extends JpaRepository<PlanTratamientoInd, Long> {
    Page<PlanTratamientoInd> findByRemovido(Boolean removido, Pageable pageable);

    List<PlanTratamientoInd> findByidPlanTratamientoAndRemovido(Long id, Boolean removido);

    PlanTratamientoInd findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

//    Page<PlanTratamientoInd> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido, Pageable pageable);

    List<PlanTratamientoInd> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido);


    List<PlanTratamientoInd> findByFichaIdentificacionTokenIdentificador(String tokenIdentificadorFichaIdentificacion);

    PlanTratamientoInd findByEstadoNemonicoAndFichaIdentificacionTokenIdentificadorAndRemovido(String nemonico, String tokenIdentificadorFichaIdentificacion, Boolean removido);
}
