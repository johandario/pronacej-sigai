package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.InstrumentoEvaluacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentoEvaluacionRepository extends JpaRepository<InstrumentoEvaluacion, Long> {
    
    List<InstrumentoEvaluacion> findByRemovido(boolean removido);
    
    InstrumentoEvaluacion findByIdInstrumentoEvaluacion(Long idInstrumentoEvaluacion);
        
    InstrumentoEvaluacion findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<InstrumentoEvaluacion> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<InstrumentoEvaluacion> findByInformeSeguimientoPIITokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
            String tokenIdentificadorInformeSeguimiento, Long idEmpresa, Boolean removido, Pageable pageable);
    
    // Agregar en InstrumentoEvaluacionRepository.java
    List<InstrumentoEvaluacion> findByInformeSeguimientoPIITokenIdentificadorAndRemovido(
        String tokenIdentificadorInformeSeguimiento, Boolean removido);
}