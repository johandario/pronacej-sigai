package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndSeguiDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanTratamientoIndSeguiDocumentoRepository extends JpaRepository<PlanTratamientoIndSeguiDocumento, Long> {
    Page<PlanTratamientoIndSeguiDocumento> findByPlanTratamientoIndSeguiTokenIdentificadorAndRemovido(String tokenIdentificadorFichaSeguimiento, Boolean removido, Pageable pageable);

    PlanTratamientoIndSeguiDocumento findFirstByPlanTratamientoIndSeguiTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaSeguimiento, String tokenIdentificadorDocumento, Boolean removido);

}
