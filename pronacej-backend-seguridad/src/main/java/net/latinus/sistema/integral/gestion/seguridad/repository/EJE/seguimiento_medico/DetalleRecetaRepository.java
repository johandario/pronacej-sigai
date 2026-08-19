package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.DetalleReceta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para DetalleReceta
 */
@Repository
public interface DetalleRecetaRepository extends JpaRepository<DetalleReceta, Long> {

    /**
     * Devuelve una página de DetalleReceta por el token identificador de la receta asociada y el estado de removido.
     *
     * @param tokenId el token identificador de la receta a la que pertenece el detalle
     * @param removido boolean que especifica si el detalle está removido o no.
     * @param pageable objeto Pageable para la paginación
     *
     * @return Page<DetalleReceta>
     */
    Page<DetalleReceta> findByReceta_TokenIdentificadorAndRemovido(String tokenId, Boolean removido, Pageable pageable);

    /**
     * Devuelve un DetalleReceta por su token identificador y estado de removido.
     *
     * @param tokenIdentificador el token identificador del detalle de la receta
     * @param removido boolean que especifica si el detalle está removido o no.
     *
     * @return DetalleReceta
     */
    DetalleReceta findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<DetalleReceta> findAllByReceta_TokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);


}
