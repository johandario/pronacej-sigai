package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionMedicaProgresoCarpeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluacionMedicaProgresoCarpetaRepository extends JpaRepository<EvaluacionMedicaProgresoCarpeta, Long> {

    EvaluacionMedicaProgresoCarpeta findFirstByEvaluacionMedicaProgresoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIngreso, Boolean removido);

}
