package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.Diagnostico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Long> {
    /**
     * Devuelve un objeto Diagnostico por token identificador de la evaluacion y removido
     *
     * @param tokenId token identificador de la evaluacion medica a la que pertenece el diagnostico
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Diagnostico antecedente encontrado
     */
    Diagnostico findByTokenIdentificadorAndRemovido(String tokenId, Boolean removido);

    /**
     * Devuelve una Page de objetos Diagnostico por token identificador de le ficha medica y removido
     *
     * @param tokenId token identificador de la evaluacion medica a la que pertenecen los diagnosticos
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Page<Diagnostico> Page de diagnosticos familiares encontrados
     */
    Page<Diagnostico> findByEvaluacionMedica_TokenIdentificadorAndRemovido(String tokenId, Boolean removido, Pageable pageable);

}
