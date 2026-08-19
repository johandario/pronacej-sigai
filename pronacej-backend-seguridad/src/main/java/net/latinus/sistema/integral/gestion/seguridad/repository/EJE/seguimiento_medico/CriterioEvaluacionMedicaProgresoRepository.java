package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.CriterioEvaluacionMedicaProgreso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriterioEvaluacionMedicaProgresoRepository extends JpaRepository<CriterioEvaluacionMedicaProgreso, Long> {

    /**
     * Devuelve un objeto CriterioEvaluacionMedicaProgreso por ficha de identificación y removido
     *
     * @param tokenId token identificador de la evaluacion medica a la que pertenece el criterio de evaluacion
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Page<CriterioEvaluacionMedicaSeguimiento>
     */
    Page<CriterioEvaluacionMedicaProgreso> findByEvaluacionMedicaProgreso_TokenIdentificadorAndRemovido(String tokenId, Boolean removido, Pageable pageable);

    CriterioEvaluacionMedicaProgreso findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<CriterioEvaluacionMedicaProgreso> findByEvaluacionMedicaProgreso_TokenIdentificadorAndRemovido(String tokenId, Boolean removido);

}
