package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionDomiciliariaDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluacionDomiciliariaDocumentoRepository extends JpaRepository<EvaluacionDomiciliariaDocumento, Long> {
    Page<EvaluacionDomiciliariaDocumento> findByEvaluacionDomiciliariaTokenIdentificadorAndRemovido(String tokenIdentificadorEvaluacionDomiciliaria, Boolean removido, Pageable pageable);
    EvaluacionDomiciliariaDocumento findFirstByEvaluacionDomiciliariaTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorEvaluacionDomiciliaria, String tokenIdentificadorDocumento, Boolean removido);
}