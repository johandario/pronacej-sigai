package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionSeguimientoEducativoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluacionSeguimientoEducativoDocumentoRepository extends JpaRepository<EvaluacionSeguimientoEducativoDocumento, Long> {
    Page<EvaluacionSeguimientoEducativoDocumento> findByEvaluacionSeguimientoEducativoLaboralTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);
    EvaluacionSeguimientoEducativoDocumento findFirstByEvaluacionSeguimientoEducativoLaboralTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorSeguimiento, String tokenIdentificadorDocumento, Boolean removido);
}
