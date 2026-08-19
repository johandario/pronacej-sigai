package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.SituPersCaraPers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SituPersCaraPersRepository extends JpaRepository<SituPersCaraPers, Long> {
    
    SituPersCaraPers findByIdSituPersCaraPers(Long idSituPersCaraPers);
        
    SituPersCaraPers findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<SituPersCaraPers> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<SituPersCaraPers> findByEvaluacionConductualTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(String tokenIdentificadorEvaluacionConductual, Long idEmpresa, Boolean removido, Pageable pageable);
    
}
