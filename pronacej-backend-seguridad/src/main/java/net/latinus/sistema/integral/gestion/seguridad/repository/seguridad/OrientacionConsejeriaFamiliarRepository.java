package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.OrientacionConsejeriaFamiliar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrientacionConsejeriaFamiliarRepository extends JpaRepository<OrientacionConsejeriaFamiliar, Long> {
    
    List<OrientacionConsejeriaFamiliar> findByRemovido(boolean removido);
    
    OrientacionConsejeriaFamiliar findByIdOrientacionConsejeriaFamiliar(Long idOrientacionConsejeriaFamiliar);
    
    OrientacionConsejeriaFamiliar findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<OrientacionConsejeriaFamiliar> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<OrientacionConsejeriaFamiliar> findByPersonaRelacionadaTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
            String tokenIdentificadorPersonaRelacionada, Long idEmpresa, Boolean removido, Pageable pageable);
}