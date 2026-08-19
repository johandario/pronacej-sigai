package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.InformeVisitas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformeVisitasRepository extends JpaRepository<InformeVisitas, Long> {
    
    List<InformeVisitas> findByRemovido(boolean removido);
    
    InformeVisitas findByIdInformeVisitas(Long idInformeVisitas);
    
    InformeVisitas findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<InformeVisitas> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<InformeVisitas> findByPersonaRelacionadaTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
            String tokenIdentificadorPersonaRelacionada, Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<InformeVisitas> findByTokenIdentificadorFichaPrincipalAndEmpresaIdEmpresaAndRemovido(
            String tokenIdentificadorFichaPrincipal, Long idEmpresa, Boolean removido, Pageable pageable);
}
