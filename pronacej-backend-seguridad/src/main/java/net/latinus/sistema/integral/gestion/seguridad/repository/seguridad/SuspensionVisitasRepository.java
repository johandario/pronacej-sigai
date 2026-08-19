package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.SuspensionVisitas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspensionVisitasRepository extends JpaRepository<SuspensionVisitas, Long> {
    
    List<SuspensionVisitas> findByRemovido(boolean removido);
    
    SuspensionVisitas findByIdSuspensionVisitas(Long idSuspensionVisitas);
    
    SuspensionVisitas findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<SuspensionVisitas> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<SuspensionVisitas> findByTokenIdentificadorFichaPrincipalAndEmpresaIdEmpresaAndRemovido(
            String tokenIdentificadorFichaPrincipal, Long idEmpresa, Boolean removido, Pageable pageable);
}
