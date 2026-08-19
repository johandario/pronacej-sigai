package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ExpedienteMatrizDetalleCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpedienteMatrizDetalleCarpetaRepository extends JpaRepository<ExpedienteMatrizDetalleCarpeta, Long> {
    Page<ExpedienteMatrizDetalleCarpeta> findByExpedienteMatrizDetalleTokenIdentificadorAndRemovido(String tokenIdentificadorExpedienteMatrizDetalle, Boolean removido, Pageable pageable);

    ExpedienteMatrizDetalleCarpeta findFirstByExpedienteMatrizDetalleTokenIdentificadorAndRemovido(String tokenIdentificadorExpedienteMatrizDetalle, Boolean removido);
}
