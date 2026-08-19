package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.RecomendacionComentarioPorEvalSegu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecomendacionComentarioPorEvalSeguRepository extends JpaRepository<RecomendacionComentarioPorEvalSegu, Long> {
    
    List<RecomendacionComentarioPorEvalSegu> findByRemovido(boolean removido);
    
    RecomendacionComentarioPorEvalSegu findByIdRecomendacionComentario(Long idRecomendacionComentario);
        
    RecomendacionComentarioPorEvalSegu findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<RecomendacionComentarioPorEvalSegu> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<RecomendacionComentarioPorEvalSegu> findByEvaluacionSeguimientoTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(String tokenIdentificadorEvaluacionSeguimiento, Long idEmpresa, Boolean removido, Pageable pageable);
}