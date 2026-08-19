package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionMedicaProgresoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluacionMedicaProgresoDocumentoRepository extends JpaRepository<EvaluacionMedicaProgresoDocumento, Long> {

    Page<EvaluacionMedicaProgresoDocumento> findByEvaluacionMedicaProgresoTokenIdentificadorAndRemovido(String tokenIdentificadorEvaluacionMedicaProgreso, Boolean removido, Pageable pageable);

    EvaluacionMedicaProgresoDocumento findFirstByEvaluacionMedicaProgresoTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorEvaluacionMedicaProgreso,
                                                                                                             String tokenIdentificadorDocumento, Boolean removido);

}