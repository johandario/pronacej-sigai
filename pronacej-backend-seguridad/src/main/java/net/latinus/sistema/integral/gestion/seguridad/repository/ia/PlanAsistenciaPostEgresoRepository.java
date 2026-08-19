package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PlanAsistenciaPostEgreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanAsistenciaPostEgresoRepository extends JpaRepository<PlanAsistenciaPostEgreso, Long> {
    List<PlanAsistenciaPostEgreso> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido);

    PlanAsistenciaPostEgreso findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    PlanAsistenciaPostEgreso findByidPlanAsistenciaPostEgresoAndRemovido(Long idPlanAsistenciaPostEgreso, Boolean removido);

    List<PlanAsistenciaPostEgreso> findByFichaIdentificacionTokenIdentificador(String tokenIdentificadorFichaIdentificacion);

}
