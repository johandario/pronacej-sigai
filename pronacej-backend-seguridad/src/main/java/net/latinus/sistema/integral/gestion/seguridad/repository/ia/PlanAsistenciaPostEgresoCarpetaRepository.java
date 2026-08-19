package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanAsistenciaPostEgresoCarpeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanAsistenciaPostEgresoCarpetaRepository extends JpaRepository<PlanAsistenciaPostEgresoCarpeta, Long> {
    PlanAsistenciaPostEgresoCarpeta findFirstByPlanAsistenciaPostEgresoTokenIdentificadorAndRemovido(String tokenIdentificadorPlanAsistencia, Boolean removido);
}
