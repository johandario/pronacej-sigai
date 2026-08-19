package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.latinus.sistema.integral.gestion.seguridad.entities.CometimientoInfraccion;
import net.latinus.sistema.integral.gestion.seguridad.entities.SuspensionVisitas;

@Repository
public interface CometimientoInfraccionRepository extends JpaRepository<CometimientoInfraccion, Long> {
    
    List<CometimientoInfraccion> findBySuspensionVisitasAndRemovido(SuspensionVisitas suspensionVisitas, Boolean removido);
    
    List<CometimientoInfraccion> findBySuspensionVisitasTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    CometimientoInfraccion findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}