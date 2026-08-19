package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.Laboral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaboralRepository extends JpaRepository<Laboral, Long> {
List<Laboral> findByRemovido(boolean removido);
    
    Laboral findByIdLaboral(Long idLaboral);
        
    Laboral findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<Laboral> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<Laboral> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(String tokenIdentificadorFichaIdentificacion, Long idEmpresa, Boolean removido, Pageable pageable);
    
}
