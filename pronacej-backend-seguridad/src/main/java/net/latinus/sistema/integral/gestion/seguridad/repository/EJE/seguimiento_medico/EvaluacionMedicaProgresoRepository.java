package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionMedicaProgreso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluacionMedicaProgresoRepository extends JpaRepository<EvaluacionMedicaProgreso, Long> {

    /**
     * Devuelve un objeto EvaluacionMedicaProgreso por token identificador y removido
     *
     * @param tokenId token identificador de la ficha médica
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return EvaluacionMedica
     */
    EvaluacionMedicaProgreso findByTokenIdentificadorAndRemovido(String tokenId, Boolean removido);


    /**
     * Devuelve un objeto EvaluacionMedicaProgreso por ficha de identificación y removido
     *
     * @param tokenId token identificador de la ficha de medica a la que pertenece la evaluacion medica
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return EvaluacionMedica
     */
    Page<EvaluacionMedicaProgreso> findByFichaMedica_TokenIdentificadorAndRemovido(String tokenId, Boolean removido, Pageable pageable);

}
