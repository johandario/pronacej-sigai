package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.SituacionEducativaLaboralOcio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SituacionEducativaLaboralOcioRepository extends JpaRepository<SituacionEducativaLaboralOcio, Long> {
    
    List<SituacionEducativaLaboralOcio> findByRemovido(boolean removido);
    
    SituacionEducativaLaboralOcio findByIdSituacionEducativaLaboralOcio(Long idSituacionEducativaLaboralOcio);
        
    SituacionEducativaLaboralOcio findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<SituacionEducativaLaboralOcio> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<SituacionEducativaLaboralOcio> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(String tokenIdentificadorFichaIdentificacion, Long idEmpresa, Boolean removido, Pageable pageable);
    
}
