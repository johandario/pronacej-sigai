package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoSocialDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguimientoSocialDocumentoRepository extends JpaRepository<SeguimientoSocialDocumento, Long> {
    Page<SeguimientoSocialDocumento> findBySeguimientoSocialTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);

    SeguimientoSocialDocumento findFirstBySeguimientoSocialTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorSeguimiento, String tokenIdentificadorDocumento, Boolean removido);
}
