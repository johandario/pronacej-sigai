package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.SituacionActualAdolescente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SituacionActualAdolescenteRepository extends JpaRepository<SituacionActualAdolescente, Long> {
    
    List<SituacionActualAdolescente> findByRemovido(boolean removido);
    
    SituacionActualAdolescente findByIdSituacionActual(Long idSituacionActual);
    
    SituacionActualAdolescente findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    List<SituacionActualAdolescente> findByFichaIdentificacionTokenIdentificadorAndRemovido(
        String tokenIdentificadorFichaIdentificacion, Boolean removido);
    
    Page<SituacionActualAdolescente> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
        String tokenIdentificadorFichaIdentificacion, Long idEmpresa, Boolean removido, Pageable pageable);
}