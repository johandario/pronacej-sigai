package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ExpedienteMatrizDetalleDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpedienteMatrizDetalleDocumentoRepository extends JpaRepository<ExpedienteMatrizDetalleDocumento, Long> {
    Page<ExpedienteMatrizDetalleDocumento> findByExpedienteMatrizDetalleTokenIdentificadorAndRemovido(String tokenIdentificadorExpedienteMatrizDetalle, Boolean removido, Pageable pageable);

    ExpedienteMatrizDetalleDocumento findFirstByExpedienteMatrizDetalleTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorExpedienteMatrizDetalle, String tokenIdentificadorDocumento, Boolean removido);
}
