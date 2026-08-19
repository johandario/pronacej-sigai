package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndSeguiAbiertoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanTratamientoIndSeguiAbiertoDocumentoRepository extends JpaRepository<PlanTratamientoIndSeguiAbiertoDocumento, Long> {
    Page<PlanTratamientoIndSeguiAbiertoDocumento> findByPlanTratamientoIndSeguiAbiertoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaSeguimientoAbierto, Boolean removido, Pageable pageable);

    PlanTratamientoIndSeguiAbiertoDocumento findFirstByPlanTratamientoIndSeguiAbiertoTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaSeguimientoAbierto, String tokenIdentificadorDocumento, Boolean removido);

}
