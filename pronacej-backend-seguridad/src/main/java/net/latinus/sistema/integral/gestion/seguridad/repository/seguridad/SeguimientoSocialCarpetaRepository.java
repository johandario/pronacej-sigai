package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoSocialCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguimientoSocialCarpetaRepository extends JpaRepository<SeguimientoSocialCarpeta, Long> {
    Page<SeguimientoSocialCarpeta> findBySeguimientoSocialTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);
    SeguimientoSocialCarpeta findFirstBySeguimientoSocialTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
