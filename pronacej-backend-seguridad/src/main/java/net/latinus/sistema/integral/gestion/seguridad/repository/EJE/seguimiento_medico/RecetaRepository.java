package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.Receta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {

    /**
     * Devuelve un objeto Receta por token identificador y removido.
     *
     * @param tokenId  token identificador de la receta
     * @param removido boolean que especifica si esta removida o no.
     * @return Receta
     */
    Receta findByTokenIdentificadorAndRemovido(String tokenId, Boolean removido);


    /**
     * Devuelve una página de objetos Receta por token identificador de la evaluación médica asociada y removido.
     *
     * @param tokenId  token identificador de la evaluación médica a la que pertenece la receta
     * @param removido boolean que especifica si esta removida o no.
     * @param pageable objeto para paginación
     * @return Page<Receta>
     */
    Page<Receta> findByEvaluacionMedica_TokenIdentificadorAndRemovido(String tokenId, Boolean removido, Pageable pageable);

    Receta findByEvaluacionMedica_TokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("SELECT r FROM Receta r WHERE r.evaluacionMedica.tokenIdentificador = :token AND r.removido = false")
    Receta findRecetaSinDetalles(@Param("token") String tokenIdentificador);

    Receta findByConsultaAtencionIntegral_TokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("SELECT r FROM Receta r WHERE r.consultaAtencionIntegral.tokenIdentificador = :token AND r.removido = false")
    Receta findRecetaConsultaSinDetalles(@Param("token") String tokenIdentificador);
}
