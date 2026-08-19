package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanAsistenciaPostEgresoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanAsistenciaPostEgresoDetalleRepository extends JpaRepository<PlanAsistenciaPostEgresoDetalle, Long> {
    PlanAsistenciaPostEgresoDetalle findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
