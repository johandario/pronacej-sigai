package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ia.NotificacionDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionDocumentoRepository extends JpaRepository<NotificacionDocumento, Long> {
    Page<NotificacionDocumento> findByNotificacionTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);
}
