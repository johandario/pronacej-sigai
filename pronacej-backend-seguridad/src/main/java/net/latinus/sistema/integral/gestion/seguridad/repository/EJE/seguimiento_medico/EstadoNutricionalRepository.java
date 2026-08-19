package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.Diagnostico;
import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.EstadoNutricional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoNutricionalRepository extends JpaRepository<EstadoNutricional, Long> {
    /**
     * Devuelve un objeto EstadoNutricional por token identificador de la evaluacion y removido
     *
     * @param tokenId token identificador de la evaluacion medica a la que pertenece estados nutricionales
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return EstadoNutricional estados nutricional encontrado
     */
    EstadoNutricional findByTokenIdentificadorAndRemovido(String tokenId, Boolean removido);

    /**
     * Devuelve una Page de objetos EstadoNutricional por token identificador de le ficha medica y removido
     *
     * @param tokenId token identificador de la evaluacion medica a la que pertenecen los estados nutricionales
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Page<EstadoNutricional> Page de estados nutricionales familiares encontrados
     */
    Page<EstadoNutricional> findByEvaluacionMedica_TokenIdentificadorAndRemovido(String tokenId, Boolean removido, Pageable pageable);

}
