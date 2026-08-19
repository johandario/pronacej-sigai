package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.EvaluacionMedica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluacionMedicaRepository extends JpaRepository<EvaluacionMedica, Long> {
    /**
     * Devuelve un objeto EvaluacionMedica por token identificador y removido
     *
     * @param tokenId token identificador de la ficha médica
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return EvaluacionMedica
     */
    EvaluacionMedica findByTokenIdentificadorAndRemovido(String tokenId, Boolean removido);


    /**
     * Devuelve un objeto EvaluacionMedica por ficha de identificación y removido
     *
     * @param tokenId token identificador de la ficha de medica a la que pertenece la evaluacion medica
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return EvaluacionMedica
     */
    Page<EvaluacionMedica> findByFichaMedica_TokenIdentificadorAndRemovido(String tokenId, Boolean removido, Pageable pageable);

}
