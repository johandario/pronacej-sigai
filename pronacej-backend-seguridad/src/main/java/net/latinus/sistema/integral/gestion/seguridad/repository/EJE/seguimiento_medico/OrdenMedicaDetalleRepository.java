package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.OrdenMedicaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para OrdenMedicaDetalle
 */
@Repository
public interface OrdenMedicaDetalleRepository extends JpaRepository<OrdenMedicaDetalle, Long> {
    /**
     * Devuelve un OrdenMedicaDetalle por su token identificador y estado de removido.
     *
     * @param tokenIdentificador el token identificador del detalle de la receta
     * @param removido boolean que especifica si el detalle está removido o no.
     *
     * @return OrdenMedicaDetalle
     */
    OrdenMedicaDetalle findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<OrdenMedicaDetalle> findAllByOrdenMedica_TokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);


}
