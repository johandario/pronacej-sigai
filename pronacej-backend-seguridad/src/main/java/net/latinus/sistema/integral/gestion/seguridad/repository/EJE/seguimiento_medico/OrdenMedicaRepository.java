package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.OrdenMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenMedicaRepository extends JpaRepository<OrdenMedica, Long> {

    /**
     * Devuelve un objeto OrdenMedica por token identificador y removido.
     *
     * @param tokenId  token identificador de la receta
     * @param removido boolean que especifica si esta removida o no.
     * @return OrdenMedica
     */
    OrdenMedica findByTokenIdentificadorAndRemovido(String tokenId, Boolean removido);

    OrdenMedica findByConsultaAtencionIntegral_TokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("SELECT r FROM OrdenMedica r WHERE r.consultaAtencionIntegral.tokenIdentificador = :token AND r.removido = false")
    OrdenMedica findOrdenMedicaConsultaSinDetalles(@Param("token") String tokenIdentificador);
}
