package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionConductual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluacionConductualRepository extends JpaRepository<EvaluacionConductual, Long> {
    
    EvaluacionConductual findByIdEvaluacionConductual(Long idEvaluacionConductual);
        
    EvaluacionConductual findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<EvaluacionConductual> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<EvaluacionConductual> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(String tokenIdentificadorFichaIdentificacion, Long idEmpresa, Boolean removido, Pageable pageable);
    
}
