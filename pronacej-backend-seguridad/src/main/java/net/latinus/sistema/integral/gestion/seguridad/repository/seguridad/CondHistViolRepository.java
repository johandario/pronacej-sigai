package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.CondHistViol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CondHistViolRepository extends JpaRepository<CondHistViol, Long> {
    
    CondHistViol findByIdCondHistViol(Long idCondHistViol);
        
    CondHistViol findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<CondHistViol> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<CondHistViol> findByEvaluacionConductualTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(String tokenIdentificadorEvaluacionConductual, Long idEmpresa, Boolean removido, Pageable pageable);
    
}
