package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ExpedienteMatrizMedida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpedienteMatrizMedidaRepository extends JpaRepository<ExpedienteMatrizMedida, Long> {
    List<ExpedienteMatrizMedida> findByExpedienteDetalleMedidaSocioeducativaTokenIdentificadorAndRemovido(String tokenDetalle, Boolean removido);

    List<ExpedienteMatrizMedida> findByExpedienteDetalleMedidaAccesoriaTokenIdentificadorAndRemovido(String tokenDetalle, Boolean removido);

}
