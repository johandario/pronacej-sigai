package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.IntervencionDiferenciadaCarpeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntervencionDiferenciadaCarpetaRepository extends JpaRepository<IntervencionDiferenciadaCarpeta, Long> {

    IntervencionDiferenciadaCarpeta findFirstByPlanTratamientoIndIntervIdPlanTratIndIntervAndRemovido(Long tokenIdentificadorPertenencia, Boolean removido);

}
