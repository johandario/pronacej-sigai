package net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica;

import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaMedicaRepository extends JpaRepository<FichaMedica, Long> {
    /**
     * Devuelve una lista de objetos Fichamedica por removido
     *
     * @param removido boolean que especifica si esta removido o no.
     * @param pageable objeto para manejar la paginacion paginación
     *
     * @return List<FichaMedica>
     */
    Page<FichaMedica> findByRemovido(Boolean removido, Pageable pageable);

    /**
     * Devuelve un objeto Fichamedica por token identificador y removido
     *
     * @param tokenId token identificador de la ficha médica
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return FichaMedica
     */
    FichaMedica findByTokenIdentificadorAndRemovido(String tokenId, Boolean removido);


    /**
     * Devuelve un objeto Fichamedica por ficha de identificación y removido
     *
     * @param tokenId token identificador de la ficha de identificacion a la que pertenece la ficha médica
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return FichaMedica
     */
    FichaMedica findByFichaIdentificacion_TokenIdentificadorAndRemovido(String tokenId, Boolean removido);

    /**
     *
     */
    @Query("SELECT fm FROM FichaMedica fm WHERE fm.fichaIdentificacion.tokenIdentificador = :tokenIdentificador and fm.removido = false")
    FichaMedica encontrarFichaMedicaPorFichaIdentificacion(@Param("tokenIdentificador") String tokenIdentificador);
}
