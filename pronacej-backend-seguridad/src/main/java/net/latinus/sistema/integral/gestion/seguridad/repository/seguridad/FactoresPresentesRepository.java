package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.FactoresPresentes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactoresPresentesRepository extends JpaRepository<FactoresPresentes, Long> {
    
    List<FactoresPresentes> findByRemovido(boolean removido);
    
    FactoresPresentes findByIdFactoresPresentesAndRemovido(Long idFactoresPresentes, Boolean removido);
    
    FactoresPresentes findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    List<FactoresPresentes> findByFichaIdentificacionTokenIdentificadorAndRemovido(
        String tokenIdentificadorFichaIdentificacion, Boolean removido);
    
    Page<FactoresPresentes> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
        String tokenIdentificadorFichaIdentificacion, Long idEmpresa, Boolean removido, Pageable pageable);
}