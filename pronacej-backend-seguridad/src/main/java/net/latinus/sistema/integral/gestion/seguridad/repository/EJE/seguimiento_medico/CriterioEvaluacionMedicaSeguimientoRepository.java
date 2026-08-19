package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.CriterioEvaluacionMedicaSeguimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CriterioEvaluacionMedicaSeguimientoRepository extends JpaRepository<CriterioEvaluacionMedicaSeguimiento, Long> {

    /**
     * Devuelve un objeto CriterioEvaluacionMedicaSeguimiento por ficha de identificación y removido
     *
     * @param tokenId token identificador de la evaluacion medica a la que pertenece el criterio de evaluacion
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Page<CriterioEvaluacionMedicaSeguimiento>
     */
    Page<CriterioEvaluacionMedicaSeguimiento> findByEvaluacionMedica_TokenIdentificadorAndRemovido(String tokenId, Boolean removido, Pageable pageable);

    CriterioEvaluacionMedicaSeguimiento findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
