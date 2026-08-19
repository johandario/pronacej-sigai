package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionSeguimientoEducativoCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluacionSeguimientoEducativoCarpetaRepository extends JpaRepository<EvaluacionSeguimientoEducativoCarpeta, Long> {

    Page<EvaluacionSeguimientoEducativoCarpeta> findByEvaluacionSeguimientoEducativoLaboralTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);
    EvaluacionSeguimientoEducativoCarpeta findFirstByEvaluacionSeguimientoEducativoLaboralTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
